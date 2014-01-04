package photosrenamer.gui;


import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.AbstractListModel;

import photosrenamer.photosrenamer.FileComparator;
import photosrenamer.photosrenamer.FileItem;

public class FileItemListModel
  extends AbstractListModel<FileItem>
{

  private static final long serialVersionUID = 4050810987907022589L;

  private final List<FileItem> fileItems;

  private static final Logger logger = Logger.getGlobal();

  private Path workingDir;

  public FileItemListModel()
  {
    fileItems = new ArrayList<>();
  }

  @Override
  public FileItem getElementAt(final int index)
  {
    return fileItems.get(index);
  }

  public List<Path> getFiles()
  {
    final List<Path> files = new ArrayList<>(fileItems.size());
    for (final FileItem fileItem: fileItems)
    {
      files.add(fileItem.getFile());
    }
    return files;
  }

  @Override
  public int getSize()
  {
    return fileItems.size();
  }

  public void load()
  {
    if (workingDir == null && !Files.isDirectory(workingDir))
    {
      return;
    }

    fileItems.clear();

    try (DirectoryStream<Path> dirStream = Files
      .newDirectoryStream(workingDir, "*.{jpeg,jpg,gif,tiff,tif,png}"))
    {
      for (final Path file: dirStream)
      {
        fileItems.add(new FileItem(file));
      }

    }
    catch (final IOException e)
    {
      logger.log(new LogRecord(Level.CONFIG, e.getMessage()));
    }

    sort(FileComparator.BY_NAME);
  }

  public void setWorkingDirectory(final Path workingDirectory)
  {
    if (workingDirectory != null && Files.isDirectory(workingDirectory))
    {
      workingDir = workingDirectory;
      load();
    }
    else
    {
      workingDir = null;
      fileItems.clear();
    }
  }

  /**
   * Sorts items in the list.
   * 
   * @param comparator
   *        Comparator for sorting.
   */
  public void sort(final Comparator<FileItem> comparator)
  {
    Collections.sort(fileItems, comparator);
    fireContentsChanged(this, 0, getSize() - 1);
  }

  public void swap(final int i, final int j)
  {
    if (i == j)
    {
      return;
    }
    Collections.swap(fileItems, i, j);
    fireContentsChanged(this, i, j);
  }

}
