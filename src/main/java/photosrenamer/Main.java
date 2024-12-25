/*
 * Copyright (c) 2004-2025, Sualeh Fatehi <sualeh@hotmail.com>
 * This work is licensed under the Creative Commons Attribution-Noncommercial-No Derivative Works 3.0 License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/
 * or send a letter to Creative Commons, 543 Howard Street, 5th Floor, San Francisco, California, 94105, USA.
 */
package photosrenamer;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.UIManager;

import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.LightGray;

import photosrenamer.gui.PhotosRenamerWindow;

/**
 * Shows the Files Renamer window.
 *
 * @author Sualeh Fatehi
 */
public class Main {

  private static final Logger logger = Logger.getGlobal();

  /**
   * Shows the Files Renamer window.
   *
   * @param args Command line arguments.
   */
  public static void main(final String[] args) {
    try {
      logger.setLevel(Level.ALL);

      PlasticLookAndFeel.setPlasticTheme(new LightGray());
      UIManager.setLookAndFeel(new PlasticLookAndFeel());
    } catch (final Exception e) {
      logger.log(Level.WARNING, e.getMessage(), e);
    }

    new PhotosRenamerWindow().setVisible(true);
  }
}
