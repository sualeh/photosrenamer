/*
 * Copyright (c) 2004-2023, Sualeh Fatehi <sualeh@hotmail.com>
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
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Pattern;

/**
 * Renames and orders a list of files, and automatically assigns consecutive numbers to the file
 * names, but preserving extensions.
 *
 * @author Sualeh Fatehi
 */
public final class FilesRenamer {

  private static final Logger logger = Logger.getGlobal();

  private static final boolean DEBUG_FORCE_EXCEPTION = false;
  private static final boolean DEBUG_FORCE_SLOWDOWN = false;

  private final List<Path> files;
  private final String fileStem;

  public FilesRenamer(final List<Path> files, final String fileStem) {
    if (files == null || files.isEmpty()) {
      throw new IllegalArgumentException("No files provided");
    }
    this.files = files;

    if (fileStem == null || !Pattern.matches("^[a-zA-Z0-9_.]+$", fileStem)) {
      throw new IllegalArgumentException("No alphanumeric filestem provided");
    }
    this.fileStem = fileStem;
  }

  private void closeFileLogger(final FileHandler handler) {
    handler.close();
    logger.removeHandler(handler);
  }

  public String getFileStem() {
    return fileStem;
  }

  @SafeVarargs
  private final void logFiles(final List<Path>... files) {
    final StringBuilder buffer = new StringBuilder();
    buffer.append("\n");
    for (int i = 0; i < files[0].size(); i++) {
      for (int j = 0; j < files.length; j++) {
        if (i >= files[j].size()) {
          continue;
        }

        if (j > 0) {
          buffer.append(" -> ");
        }
        buffer.append(String.format("%s", files[j].get(i).toFile().getName()));
      }
      buffer.append("\n");
    }
    logger.log(Level.INFO, buffer.toString());
  }

  private List<Path> makeRenamePass(final List<Path> files, final String fileStem) {
    final List<Path> renamedFiles = new ArrayList<>();
    int i = 0;
    for (final Path file : files) {
      try {
        if (DEBUG_FORCE_EXCEPTION && i == 1) {
          throw new RuntimeException("Simulated exception");
        }
        if (DEBUG_FORCE_SLOWDOWN) {
          TimeUnit.SECONDS.sleep(2);
        }

        final String originalFilename = file.toString();
        final String extension =
            originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();

        i = i + 1;
        final Path renamedFile =
            file.resolveSibling(String.format("%s_%04d.%s", fileStem, i, extension));

        Files.move(file, renamedFile, StandardCopyOption.ATOMIC_MOVE);
        renamedFiles.add(renamedFile);
      } catch (final Exception e) {
        final FileHandler handler = openFileLogger();

        logger.log(Level.SEVERE, String.format("Error while renaming %s\n", file.toString()));

        logger.log(
            Level.INFO,
            String.format(
                "Final file stem: \"%s\"; Current pass file stem: \"%s\"\n",
                this.fileStem, fileStem));
        logFiles(files);
        logFiles(files, renamedFiles);
        logger.log(Level.SEVERE, e.getMessage(), e);

        closeFileLogger(handler);

        throw new RuntimeException(e);
      }
    }
    return renamedFiles;
  }

  private FileHandler openFileLogger() {
    try {
      final String logFile = files.get(0).getParent().resolve("photos_renamer.log").toString();
      final FileHandler handler = new FileHandler(logFile, true);
      handler.setFormatter(new SimpleFormatter());
      logger.addHandler(handler);

      return handler;
    } catch (SecurityException | IOException e) {
      logger.log(Level.SEVERE, e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  public void rename() {
    final String pass1FileStem = UUID.randomUUID().toString();

    final List<Path> pass1Files = makeRenamePass(files, pass1FileStem);
    makeRenamePass(pass1Files, fileStem);
  }
}
