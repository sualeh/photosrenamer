/*
 * Copyright 2004-2012, Sualeh Fatehi <sualeh@hotmail.com>
 * This work is licensed under the Creative Commons Attribution-Noncommercial-No Derivative Works 3.0 License. 
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ 
 * or send a letter to Creative Commons, 543 Howard Street, 5th Floor, San Francisco, California, 94105, USA.
 */
package photosrenamer.photosrenamer;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Renames and orders a list of files, and automatically assigns
 * consecutive numbers to the file names, but preserving extensions.
 * 
 * @author Sualeh Fatehi
 */
public final class FilesRenamer
{

  private final List<Path> files;
  private final String fileStem;
  private List<Path> pass1Files;
  private List<Path> pass2Files;

  public FilesRenamer(final List<Path> files, final String fileStem)
  {
    if (files == null)
    {
      throw new IllegalArgumentException("No files provided");
    }
    this.files = files;
    if (fileStem == null || !Pattern.matches("^[a-zA-Z0-9_.]+$", fileStem))
    {
      throw new IllegalArgumentException("No alphanumeric filestem provided");
    }
    this.fileStem = fileStem;
  }

  public String getFileStem()
  {
    return fileStem;
  }

  public void rename()
    throws IOException
  {
    pass1Files = makeRenamePass1(files);
    pass2Files = makeRenamePass2(pass1Files);
  }

  private List<Path> makeRenamePass1(final List<Path> files)
    throws IOException
  {
    final List<Path> renamedFiles = new ArrayList<>();
    for (final Path file: files)
    {
      final Path renamedFile = Files.createTempFile(file.getParent(),
                                                    "FilesRenamer",
                                                    "image");
      Files.move(file, renamedFile, StandardCopyOption.REPLACE_EXISTING);
      renamedFiles.add(renamedFile);
    }
    return renamedFiles;
  }

  private List<Path> makeRenamePass2(final List<Path> files)
    throws IOException
  {
    final List<Path> renamedFiles = new ArrayList<>();
    for (int i = 0; i < files.size(); i++)
    {
      final String originalFilename = this.files.get(i).toString();
      final String extension = originalFilename
        .substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();

      final Path file = files.get(i);
      final Path renamedFile = file.resolveSibling(String.format("%s_%04d.%s",
                                                                 fileStem,
                                                                 i + 1,
                                                                 extension));
      Files.move(file, renamedFile, StandardCopyOption.ATOMIC_MOVE);
      renamedFiles.add(renamedFile);
    }
    return renamedFiles;
  }

}
