/*
 * Copyright (c) 2004-2020, Sualeh Fatehi <sualeh@hotmail.com>
 * This work is licensed under the Creative Commons Attribution-Noncommercial-No Derivative Works 3.0 License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/
 * or send a letter to Creative Commons, 543 Howard Street, 5th Floor, San Francisco, California, 94105, USA.
 */
package photosrenamer.gui;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.SwingWorker;

import photosrenamer.photosrenamer.FileItem;

/**
 * Renders a file item, including the thumbnail.
 *
 * @author Sualeh Fatehi
 */
class FileItemThumbnailRenderer extends DefaultListCellRenderer {

  private static final long serialVersionUID = -8276388250252139874L;

  /**
   * @see javax.swing.DefaultListCellRenderer#getListCellRendererComponent(javax.swing.JList,
   *     java.lang.Object, int, boolean, boolean)
   */
  @Override
  public Component getListCellRendererComponent(
      final JList<?> list,
      final Object value,
      final int index,
      final boolean isSelected,
      final boolean cellHasFocus) {

    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

    final FileItem item = (FileItem) value;
    setText(item.toHtml());
    setIcon(item.getThumbnail());

    final SwingWorker<Integer, Void> worker =
        new SwingWorker<Integer, Void>() {

          @Override
          protected Integer doInBackground() throws Exception {
            item.loadMetadata();
            return 1;
          }

          @Override
          protected void done() {
            setText(item.toHtml());
            setIcon(item.getThumbnail());
            list.repaint();
          }
        };

    worker.execute();

    return this;
  }
}
