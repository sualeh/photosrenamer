/*
 * Copyright 2004-2012, Sualeh Fatehi <sualeh@hotmail.com>
 * This work is licensed under the Creative Commons Attribution-Noncommercial-No Derivative Works 3.0 License. 
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ 
 * or send a letter to Creative Commons, 543 Howard Street, 5th Floor, San Francisco, California, 94105, USA.
 */
package photosrenamer.photosrenamer;


import java.util.Comparator;

/**
 * Comparator for various ways to sort files.
 * 
 * @author sfatehi
 */
public enum FileComparator
  implements Comparator<FileItem>
{

  /** Compare files by date. */
  BY_DATE,
  /** Compare files by name. */
  BY_NAME;

  private final Comparator<String> nameComparator = new AlphanumComparator();

  /**
   * {@inheritDoc}
   */
  @Override
  public int compare(final FileItem file1, final FileItem file2)
  {
    switch (this)
    {
      case BY_NAME:
        final String filename1 = file1.getFile().toString();
        final String filename2 = file2.getFile().toString();
        return nameComparator.compare(filename1, filename2);
      case BY_DATE:
        final long filetime1 = file1.getDate().getTime();
        final long filetime2 = file2.getDate().getTime();
        return (int) Math.signum((double) (filetime1 - filetime2));
      default:
        return 0;
    }
  }

}
