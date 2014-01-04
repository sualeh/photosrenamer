/*
 * Copyright 2004-2014, Sualeh Fatehi <sualeh@hotmail.com>
 * This work is licensed under the Creative Commons Attribution-Noncommercial-No Derivative Works 3.0 License. 
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ 
 * or send a letter to Creative Commons, 543 Howard Street, 5th Floor, San Francisco, California, 94105, USA.
 */
package photosrenamer.photosrenamer;


import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.ExifThumbnailDirectory;
import com.drew.metadata.iptc.IptcDirectory;

/**
 * Represents an image file, and metadata.
 * 
 * @author Sualeh Fatehi
 */
public final class FileItem
  implements Serializable
{

  private final class MetadataLoader
    implements Runnable
  {

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run()
    {
      logger.log(Level.FINEST, FileItem.this + ": Entered MetadataLoader");

      try
      {
        final Metadata metadata = ImageMetadataReader.readMetadata(file
          .toFile());

        final ExifIFD0Directory exifDirectory = metadata
          .getOrCreateDirectory(ExifIFD0Directory.class);
        final IptcDirectory iptcDirectory = metadata
          .getOrCreateDirectory(IptcDirectory.class);
        final ExifSubIFDDirectory exifSubDirectory = metadata
          .getOrCreateDirectory(ExifSubIFDDirectory.class);

        loadImageCommentFromMetadata(iptcDirectory);

        final Date date1 = loadImageDateFromMetadata(exifDirectory,
                                                     ExifIFD0Directory.TAG_DATETIME);
        final Date date2 = loadImageDateFromMetadata(iptcDirectory,
                                                     IptcDirectory.TAG_DATE_CREATED);
        final Date date3 = loadImageDateFromMetadata(exifSubDirectory,
                                                     ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
        final Date date4 = loadCreateDateFromFile();

        date = earliestDate(date1, date);
        date = earliestDate(date2, date);
        date = earliestDate(date3, date);
        date = earliestDate(date4, date);

        final ExifThumbnailDirectory thumbnailDirectory = metadata
          .getOrCreateDirectory(ExifThumbnailDirectory.class);
        final boolean loadedImageThumbnailFromMetadata = loadImageThumbnailFromMetadata(thumbnailDirectory);
        if (!loadedImageThumbnailFromMetadata)
        {
          loadImageThumbnail();
        }
      }
      catch (final Exception e)
      {
        logger.log(Level.FINE, FileItem.this.toString(), e);
      }

    }

    private Date earliestDate(final Date date1, final Date date2)
    {
      if (date1 == null)
      {
        return date2;
      }
      else if (date2 == null)
      {
        return date1;
      }
      else if (date1.before(date2))
      {
        return date1;
      }
      else
      {
        return date2;
      }
    }

    private boolean loadImageCommentFromMetadata(final IptcDirectory directory)
    {
      final boolean loaded = false;
      final int tag = IptcDirectory.TAG_CAPTION;
      if (directory.containsTag(tag))
      {
        comment = directory.getString(tag);
      }
      return loaded;
    }

    private Date loadImageDateFromMetadata(final Directory directory,
                                           final int tag)
    {
      Date date = null;
      try
      {
        if (directory.containsTag(tag))
        {
          date = directory.getDate(tag);
          if (date.before(new Date(70, 1, 1)))
          {
            date = null;
          }
        }
      }
      catch (final Exception e)
      {
        logger.log(Level.FINE, FileItem.this.toString(), e);
      }
      return date;
    }

    private boolean loadImageThumbnail()
    {
      logger.log(Level.FINEST, FileItem.this
                               + ": Entered MetadataLoader:loadImageThumbnail");
      boolean loaded = false;
      try
      {
        final Image image = ImageIO.read(file.toFile());
        if (image != null)
        {
          thumbnail = new ImageIcon(scaleImage(image));
          logger.log(Level.INFO, FileItem.this
                                 + ": Thumbnail created by scaling image");
          loaded = true;
        }
      }
      catch (final IOException e)
      {
        logger.log(Level.FINE, FileItem.this.toString(), e);
      }
      return loaded;
    }

    private boolean loadImageThumbnailFromMetadata(final ExifThumbnailDirectory directory)
    {
      boolean thumbnailLoaded = false;
      try
      {
        if (directory.hasThumbnailData())
        {
          final byte[] bytes = directory.getThumbnailData();
          final Image image = ImageIO.read(new ByteArrayInputStream(bytes));
          if (image != null)
          {
            thumbnail = new ImageIcon(scaleImage(image));
            thumbnailLoaded = true;
          }
        }
      }
      catch (final Exception e)
      {
        logger.log(Level.FINE, FileItem.this.toString(), e);
      }
      return thumbnailLoaded;
    }

    private Image scaleImage(final Image image)
    {
      Image scaledImage = image.getScaledInstance(IMAGE_WIDTH,
                                                  -1,
                                                  Image.SCALE_AREA_AVERAGING);

      // Create a buffered image from the scaled image,
      // with a white background
      final BufferedImage bufferedImage = new BufferedImage(IMAGE_WIDTH,
                                                            IMAGE_HEIGHT,
                                                            BufferedImage.TYPE_INT_RGB);
      final Graphics2D g2d = bufferedImage.createGraphics();
      g2d.setColor(Color.WHITE);
      g2d.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
      g2d.drawImage(scaledImage, null, null);

      // Release resources
      g2d.dispose();
      bufferedImage.flush();

      scaledImage.flush();
      scaledImage = null;
      image.flush();

      // Crop the image to size
      final CropImageFilter filter = new CropImageFilter(0,
                                                         0,
                                                         IMAGE_WIDTH,
                                                         IMAGE_HEIGHT);
      final Image thumbnail = Toolkit
        .getDefaultToolkit()
        .createImage(new FilteredImageSource(bufferedImage.getSource(), filter));
      return thumbnail;
    }
  }

  /** Default icon, until the image is loaded. */
  public static final ImageIcon DEFAULT_IMAGE_ICON = createDefaultImageIcon();

  private static final Logger logger = Logger.getGlobal();

  private static final long serialVersionUID = -4057318666488966541L;

  private static final int IMAGE_WIDTH = 150;

  private static final int IMAGE_HEIGHT = (int) (IMAGE_WIDTH * 2F / 3F);
  private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");

  private static ImageIcon createDefaultImageIcon()
  {

    final BufferedImage image = new BufferedImage(IMAGE_WIDTH,
                                                  IMAGE_HEIGHT,
                                                  BufferedImage.TYPE_INT_ARGB);

    // Create a graphics context on the buffered image
    final Graphics2D g2d = image.createGraphics();
    // Draw background
    g2d.setColor(Color.WHITE);
    g2d.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
    // Draw "?"
    g2d.setColor(Color.ORANGE);
    g2d.setFont(new Font("Serif", Font.BOLD + Font.ITALIC, 72));
    g2d.drawString("?", 8, IMAGE_HEIGHT - 8);
    //
    g2d.dispose();
    return new ImageIcon(image);
  }

  private final Path file;
  private Date date;
  private ImageIcon thumbnail;
  private String comment;
  private boolean metadataLoaded;

  /**
   * Create a file item from a file.
   * 
   * @param file
   *        File item to create.
   */
  public FileItem(final Path file)
  {
    if (file == null)
    {
      throw new IllegalArgumentException();
    }
    this.file = file;

    thumbnail = DEFAULT_IMAGE_ICON;

    date = loadCreateDateFromFile();

    comment = "";
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object o)
  {
    if (this == o)
    {
      return true;
    }
    if (!(o instanceof FileItem))
    {
      return false;
    }
    final FileItem fileItem = (FileItem) o;
    return file.equals(fileItem.file);
  }

  /**
   * Gets the file comment.
   * 
   * @return Comment.
   */
  public String getComment()
  {
    return comment == null? "": comment;
  }

  /**
   * Gets the file date.
   * 
   * @return File date.
   */
  public Date getDate()
  {
    return new Date(date.getTime());
  }

  /**
   * Gets the file.
   * 
   * @return File.
   */
  public Path getFile()
  {
    return file;
  }

  /**
   * Thumbnail.
   * 
   * @return Thumbnail.
   */
  public ImageIcon getThumbnail()
  {
    return thumbnail;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode()
  {
    int result;
    result = file.hashCode();
    result = 29 * result + date.hashCode();
    return result;
  }

  /**
   * Loads image metadata, such as comment and thumbnail.
   */
  public void loadMetadata()
  {
    if (!metadataLoaded)
    {
      final MetadataLoader loader = new MetadataLoader();
      loader.run();
      metadataLoaded = true;
    }
  }

  public String toHtml()
  {
    final String dateString = dateFormat.format(date);
    final String toolTip = "<html>" + "<b>File:</b> "
                           + file.getName(file.getNameCount() - 1)
                           + "<br><b>Date:</b> " + dateString
                           + "<br><b>Comment:</b> " + comment + "</html>";
    return toolTip;
  }

  @Override
  public String toString()
  {
    return "FileItem [file=" + file + ", date=" + date + ", comment=" + comment
           + "]";
  }

  private Date loadCreateDateFromFile()
  {
    try
    {
      final BasicFileAttributes attributes = Files
        .readAttributes(file, BasicFileAttributes.class);
      final FileTime creationTime = attributes.creationTime();

      return new Date(creationTime.toMillis());
    }
    catch (final IOException e)
    {
      logger.log(Level.FINE, FileItem.this.toString(), e);
    }
    return null;
  }

}
