/*
 * Copyright (c) 2004-2022, Sualeh Fatehi <sualeh@hotmail.com>
 * This work is licensed under the Creative Commons Attribution-Noncommercial-No Derivative Works 3.0 License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/
 * or send a letter to Creative Commons, 543 Howard Street, 5th Floor, San Francisco, California, 94105, USA.
 */
package sf.util.ui;

import javax.swing.JFrame;
import javax.swing.KeyStroke;

/**
 * Exits an application.
 *
 * @author sfatehi
 */
public final class ExitAction extends GuiAction {

  private static final long serialVersionUID = 5749903957626188378L;

  /**
   * Exits an application
   *
   * @param frame Main window
   * @param text Text for the action
   */
  public ExitAction(final JFrame frame, final String text) {
    super(text, "/icons/exit.gif");
    setShortcutKey(KeyStroke.getKeyStroke("control Q"));
    addActionListener(
        actionevent -> {
          frame.dispose();
          System.exit(0);
        });
  }
}
