/*
 * Copyright (c) 2004-2023, Sualeh Fatehi <sualeh@hotmail.com>
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
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.iptc.IptcDirectory;

/**
 * Represents an image file, and metadata.
 *
 * @author Sualeh Fatehi
 */
public final class FileItem implements Serializable {

  private final class MetadataLoader implements Runnable {

    private Instant earliestInstant(final Instant instant1, final Instant instant2) {
      if ((instant1 == null) || !(instant2 == null || instant1.isBefore(instant2))) {
        return instant2;
      }else {
      return instant1;
    }
    }

    private boolean loadImageCommentFromMetadata(final IptcDirectory directory) {
      final boolean loaded = false;
      final int tag = IptcDirectory.TAG_CAPTION;
      if (directory != null && directory.containsTag(tag)) {
        comment = directory.getString(tag);
      }
      return loaded;
    }

    private Instant loadImageCreationInstantFromMetadata(final Directory directory, final int tag) {
      Instant instant = null;
      try {
        if (directory != null && directory.containsTag(tag)) {
          instant = directory.getDate(tag).toInstant();
          final LocalDate localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();
          if (localDate.isBefore(LocalDate.of(1970, 1, 1))) {
            instant = null;
          }
        }
      } catch (final Exception e) {
        logger.log(Level.FINE, FileItem.this.toString(), e);
      }
      return instant;
    }

    private boolean loadImageThumbnail() {
      logger.log(Level.FINEST, FileItem.this + ": Entered MetadataLoader:loadImageThumbnail");
      boolean loaded = false;
      try {
        final Image image = ImageIO.read(file.toFile());
        if (image != null) {
          thumbnail = new ImageIcon(scaleImage(image));
          logger.log(Level.INFO, FileItem.this + ": Thumbnail created by scaling image");
          loaded = true;
        }
      } catch (final IOException e) {
        logger.log(Level.FINE, FileItem.this.toString(), e);
      }
      return loaded;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
      logger.log(Level.FINEST, FileItem.this + ": Entered MetadataLoader");

      try {
        final Metadata metadata = ImageMetadataReader.readMetadata(file.toFile());

        final ExifIFD0Directory exifDirectory =
            metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
        final IptcDirectory iptcDirectory = metadata.getFirstDirectoryOfType(IptcDirectory.class);
        final ExifSubIFDDirectory exifSubDirectory =
            metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);

        loadImageCommentFromMetadata(iptcDirectory);

        final Instant instant1 =
            loadImageCreationInstantFromMetadata(exifDirectory, ExifDirectoryBase.TAG_DATETIME);
        final Instant instant2 =
            loadImageCreationInstantFromMetadata(iptcDirectory, IptcDirectory.TAG_DATE_CREATED);
        final Instant instant3 =
            loadImageCreationInstantFromMetadata(
                exifSubDirectory, ExifDirectoryBase.TAG_DATETIME_ORIGINAL);
        final Instant instant4 = loadCreationInstantFromFile();

        creationInstant = earliestInstant(instant1, creationInstant);
        creationInstant = earliestInstant(instant2, creationInstant);
        creationInstant = earliestInstant(instant3, creationInstant);
        creationInstant = earliestInstant(instant4, creationInstant);

        loadImageThumbnail();

      } catch (final Exception e) {
        logger.log(Level.FINE, FileItem.this.toString(), e);
      }
    }

    private Image scaleImage(final Image image) {
      Image scaledImage = image.getScaledInstance(IMAGE_WIDTH, -1, Image.SCALE_AREA_AVERAGING);

      // Create a buffered image from the scaled image,
      // with a white background
      final BufferedImage bufferedImage =
          new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
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
      final CropImageFilter filter = new CropImageFilter(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
      final Image thumbnail =
          Toolkit.getDefaultToolkit()
              .createImage(new FilteredImageSource(bufferedImage.getSource(), filter));
      return thumbnail;
    }
  }

  private static final DateTimeFormatter dateTimeFormatter =
      DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.from(ZoneOffset.UTC));

  /** Default icon, until the image is loaded. */
  public static final ImageIcon DEFAULT_IMAGE_ICON = createDefaultImageIcon();

  private static final Logger logger = Logger.getGlobal();

  private static final long serialVersionUID = -4057318666488966541L;

  private static final int IMAGE_WIDTH = 180;
  private static final int IMAGE_HEIGHT = (int) (IMAGE_WIDTH * 2F / 3F);

  private static ImageIcon createDefaultImageIcon() {

    final BufferedImage image =
        new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_ARGB);

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
  private Instant creationInstant;
  private ImageIcon thumbnail;
  private String comment;
  private boolean metadataLoaded;

  /**
   * Create a file item from a file.
   *
   * @param file File item to create.
   */
  public FileItem(final Path file) {
    if (file == null) {
      throw new IllegalArgumentException();
    }
    this.file = file;

    thumbnail = DEFAULT_IMAGE_ICON;

    creationInstant = loadCreationInstantFromFile();

    comment = "";
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof FileItem)) {
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
  public String getComment() {
    return comment == null ? "" : comment;
  }

  /**
   * Gets the file creation instant.
   *
   * @return File creation instant.
   */
  public Instant getCreationInstant() {
    return creationInstant;
  }

  /**
   * Gets the file.
   *
   * @return File.
   */
  public Path getFile() {
    return file;
  }

  /**
   * Thumbnail.
   *
   * @return Thumbnail.
   */
  public ImageIcon getThumbnail() {
    return thumbnail;
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    int result;
    result = file.hashCode();
    result = 29 * result + creationInstant.hashCode();
    return result;
  }

  private Instant loadCreationInstantFromFile() {
    try {
      final BasicFileAttributes attributes = Files.readAttributes(file, BasicFileAttributes.class);
      final FileTime creationTime = attributes.creationTime();

      return creationTime.toInstant();
    } catch (final IOException e) {
      logger.log(Level.FINE, FileItem.this.toString(), e);
    }
    return null;
  }

  /** Loads image metadata, such as comment and thumbnail. */
  public void loadMetadata() {
    if (!metadataLoaded) {
      final MetadataLoader loader = new MetadataLoader();
      loader.run();
      metadataLoaded = true;
    }
  }

  public String toHtml() {
    final String dateString = dateTimeFormatter.format(creationInstant);
    final String toolTip =
        "<html>"
            + "<b>File:</b> "
            + file.getName(file.getNameCount() - 1)
            + "<br><b>Date:</b> "
            + dateString
            + "<br><b>Comment:</b> "
            + comment
            + "</html>";
    return toolTip;
  }

  @Override
  public String toString() {
    return "FileItem [file=" + file + ", created=" + creationInstant + ", comment=" + comment + "]";
  }
}
