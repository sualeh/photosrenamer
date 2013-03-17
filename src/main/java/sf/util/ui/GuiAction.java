/*
 * Copyright 2004-2012, Sualeh Fatehi <sualeh@hotmail.com>
 * This work is licensed under the Creative Commons Attribution-Noncommercial-No Derivative Works 3.0 License. 
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ 
 * or send a letter to Creative Commons, 543 Howard Street, 5th Floor, San Francisco, California, 94105, USA.
 */
package sf.util.ui;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import javax.swing.event.EventListenerList;

/**
 * An abstract action, which takes listeners.
 * 
 * @author sfatehi
 */
public class GuiAction
  extends AbstractAction
{

  private static final long serialVersionUID = -5319269508462388520L;

  private static final Logger LOGGER = Logger.getLogger(GuiAction.class
    .getName());

  private final EventListenerList listeners = new EventListenerList();

  /**
   * Creates a new action.
   * 
   * @param text
   *        Text of the action
   */
  public GuiAction(final String text)
  {
    super(text);
    setDescription(text);
  }

  /**
   * Creates a new action.
   * 
   * @param text
   *        Text of the action
   * @param iconResource
   *        Icon
   */
  public GuiAction(final String text, final String iconResource)
  {
    this(text);
    setIcon(new ImageIcon(GuiAction.class.getResource(iconResource)));
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(final ActionEvent e)
  {
    try
    {
      final ActionListener[] actionListeners = listeners
        .getListeners(ActionListener.class);
      for (final ActionListener actionListener: actionListeners)
      {
        actionListener.actionPerformed(e);
      }
    }
    catch (final Exception ex)
    {
      LOGGER.log(Level.WARNING, "Cannot perform action - "
                                + getValue(SHORT_DESCRIPTION), ex);
    }
  }

  /**
   * Adds an action listener
   * 
   * @param l
   *        Listener
   */
  public void addActionListener(final ActionListener l)
  {
    listeners.add(ActionListener.class, l);
  }

  /**
   * Gets the descripion.
   * 
   * @return Description
   */
  public String getDescription()
  {
    return getStringValue(NAME);
  }

  /**
   * Gets the icon.
   * 
   * @return Icon
   */
  public ImageIcon getIcon()
  {
    final Object value = getValue(SMALL_ICON);
    if (value == null || !(value instanceof ImageIcon))
    {
      return null;
    }
    else
    {
      return (ImageIcon) value;
    }
  }

  /**
   * Gets the shortcut key.
   * 
   * @return Shortcut key
   */
  public KeyStroke getShortcutKey()
  {
    final Object value = getValue(ACCELERATOR_KEY);
    if (value == null || !(value instanceof KeyStroke))
    {
      return null;
    }
    else
    {
      return (KeyStroke) value;
    }
  }

  /**
   * Gets the text.
   * 
   * @return text
   */
  public String getText()
  {
    return getStringValue(NAME);
  }

  /**
   * Gets the description.
   * 
   * @param description
   *        Description
   */
  public void setDescription(final String description)
  {
    putValue(SHORT_DESCRIPTION, description);
  }

  /**
   * Gets the icon.
   * 
   * @param icon
   *        Icon
   */
  public void setIcon(final ImageIcon icon)
  {
    putValue(SMALL_ICON, icon);
  }

  /**
   * Gets the shortcut key.
   * 
   * @param keyStroke
   *        Shortcut key
   */
  public void setShortcutKey(final KeyStroke keyStroke)
  {
    putValue(ACCELERATOR_KEY, keyStroke);
  }

  /**
   * Gets the text.
   * 
   * @param text
   *        Text
   */
  public void setText(final String text)
  {
    putValue(NAME, text);
  }

  private String getStringValue(final String key)
  {
    final Object value = getValue(key);
    if (value != null)
    {
      return value.toString();
    }
    else
    {
      return "";
    }
  }

}
