/*
 * Copyright (c) 2004-2026, Sualeh Fatehi <sualeh@hotmail.com>
 * This work is licensed under the Creative Commons Attribution-Noncommercial-No Derivative Works 3.0 License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/
 * or send a letter to Creative Commons, 543 Howard Street, 5th Floor, San Francisco, California, 94105, USA.
 */
package photosrenamer.gui;

import java.awt.BorderLayout;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import photosrenamer.photosrenamer.FileItem;

/**
 * Panel for the file items sort list.
 *
 * @author Sualeh Fatehi
 */
public class FileItemSortList extends JPanel {
  public enum Direction {
    UP,
    DOWN,
  }

  private static final long serialVersionUID = -584520624711792397L;

  private final JList<FileItem> list;
  private final FileItemListModel listModel;

  /** Creates a new file items sort list panel. */
  public FileItemSortList() {

    super(new BorderLayout());

    // Create and populate the list model.
    listModel = new FileItemListModel();

    // Create the list and put it in a scroll pane.
    list = new JList<>(listModel);
    list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    list.setSelectedIndex(0);
    list.setCellRenderer(new FileItemThumbnailRenderer());
    final JScrollPane listScrollPane = new JScrollPane(list);

    add(listScrollPane, BorderLayout.CENTER);
    list.doLayout();
  }

  public List<Path> getFiles() {
    return listModel.getFiles();
  }

  private boolean isContiguous(final int[] selectedIndices) {

    boolean isContiguous = true;
    for (int i = 1; i < selectedIndices.length; i++) {
      if (selectedIndices[i] - selectedIndices[i - 1] != 1) {
        isContiguous = false;
        break;
      }
    }

    return isContiguous;
  }

  private void makeContiguous(final int[] selectedIndices) {

    for (int i = 1; i < selectedIndices.length; i++) {
      final int fromPosition = selectedIndices[i];
      final int toPosition = selectedIndices[i - 1] + 1;
      swap(fromPosition, toPosition);
      selectedIndices[i] = selectedIndices[i - 1] + 1;
    }
  }

  public void move(final Direction direction) {

    final int[] selectedIndices = list.getSelectedIndices();
    if (selectedIndices.length == 0) {
      return;
    }
    Arrays.sort(selectedIndices);

    if (!isContiguous(selectedIndices)) {
      makeContiguous(selectedIndices);
    } else {
      final int initialPosition = selectedIndices[0];
      final int finalPosition = selectedIndices[selectedIndices.length - 1];
      if (direction == Direction.UP) {
        if (initialPosition > 0) {
          moveBlockUp(selectedIndices);
        }
      } else if (direction == Direction.DOWN) {
        if (finalPosition < listModel.getSize() - 1) {
          moveBlockDown(selectedIndices);
        }
      }
    }

    list.setSelectedIndices(selectedIndices);
    list.ensureIndexIsVisible(selectedIndices[0]);
  }

  private void moveBlockDown(final int[] selectedIndices) {

    final int initialPosition = selectedIndices[0];

    for (int i = selectedIndices.length - 1; i >= 0; i--) {
      swap(selectedIndices[i], initialPosition + i + 1);
      selectedIndices[i] = initialPosition + i + 1;
    }
  }

  private void moveBlockUp(final int[] selectedIndices) {

    final int initialPosition = selectedIndices[0];

    for (int i = 0; i < selectedIndices.length; i++) {
      swap(selectedIndices[i], initialPosition + i - 1);
      selectedIndices[i] = initialPosition + i - 1;
    }
  }

  public void reload() {
    listModel.load();
  }

  public void setWorkingDirectory(final Path workingDirectory) {
    listModel.setWorkingDirectory(workingDirectory);
    list.setSelectedIndex(0);
  }

  /**
   * Sorts items in the list.
   *
   * @param comparator Comparator for sorting.
   */
  public void sort(final Comparator<FileItem> comparator) {
    listModel.sort(comparator);
  }

  private void swap(final int fromPosition, final int toPosition) {
    listModel.swap(fromPosition, toPosition);
  }
}
