/*
 * Copyright 2004-2014, Sualeh Fatehi <sualeh@hotmail.com>
 * This work is licensed under the Creative Commons Attribution-Noncommercial-No Derivative Works 3.0 License. 
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ 
 * or send a letter to Creative Commons, 543 Howard Street, 5th Floor, San Francisco, California, 94105, USA.
 */
package photosrenamer.gui;


import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import photosrenamer.Version;
import photosrenamer.photosrenamer.FileComparator;
import photosrenamer.photosrenamer.FilesRenamer;
import sf.util.ui.ExitAction;
import sf.util.ui.GuiAction;

/**
 * Provides an editor and debugger for formatting SQL statements.
 * 
 * @author Sualeh Fatehi
 */
public final class PhotosRenamerWindow
  extends JFrame
{

  private static final Logger logger = Logger.getGlobal();
  private static final long serialVersionUID = -1635298990546339443L;

  private static final Preferences preferences = Preferences
    .userNodeForPackage(PhotosRenamerWindow.class);

  /**
   * Get the default directory for data files.
   * 
   * @return Directory for data files
   */
  private static Path loadWorkingDirectory()
  {
    final String directory = preferences.get("WorkingDirectory", ".");
    return Paths.get(directory).toAbsolutePath();
  }

  private Path workingDir;

  private final FileItemSortList fileItemSortList;
  private final JLabel directoryBar;

  /**
   * Creates a new instance of the Files Renamer main window.
   */
  public PhotosRenamerWindow()
  {

    setTitle(Version.getProductName());
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    setIconImage(new ImageIcon(PhotosRenamerWindow.class.getResource("/sf.png")) 
      .getImage());

    workingDir = loadWorkingDirectory();

    final Container panel = getContentPane();
    panel.setLayout(new BorderLayout());

    directoryBar = new JLabel();
    panel.add(directoryBar, BorderLayout.SOUTH);

    fileItemSortList = new FileItemSortList();
    fileItemSortList.setPreferredSize(new Dimension(400, 600));
    panel.add(fileItemSortList, BorderLayout.CENTER);

    final JMenuBar menuBar = new JMenuBar();
    setJMenuBar(menuBar);

    final JToolBar toolBar = new JToolBar();
    toolBar.setRollover(true);
    add(toolBar, BorderLayout.NORTH);

    createFileMenu(menuBar, toolBar);
    createActionMenu(menuBar, toolBar);
    createHelpMenu(menuBar, toolBar);

    pack();
  }

  private void close()
  {
    setWorkingDirectory(null);
  }

  private void createActionMenu(final JMenuBar menuBar, final JToolBar toolBar)
  {
    final JMenu menuActions = new JMenu("Actions");

    final GuiAction up = new GuiAction("Move up", "/icons/up.gif");
    up.setShortcutKey(KeyStroke.getKeyStroke("alt UP"));
    up.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(final ActionEvent e)
      {
        fileItemSortList.move(FileItemSortList.Direction.UP);
      }
    });

    final GuiAction down = new GuiAction("Move down", "/icons/down.gif");
    down.setShortcutKey(KeyStroke.getKeyStroke("alt DOWN"));
    down.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(final ActionEvent e)
      {
        fileItemSortList.move(FileItemSortList.Direction.DOWN);
      }
    });

    menuActions.add(up);
    toolBar.add(up);

    menuActions.add(down);
    toolBar.add(down);

    menuActions.addSeparator();
    toolBar.addSeparator();

    final ButtonGroup sortingMenuItems = new ButtonGroup();

    final JRadioButtonMenuItem sortDates = new JRadioButtonMenuItem("Sort Files by Date");
    sortDates.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(final ActionEvent actionevent)
      {
        fileItemSortList.sort(FileComparator.BY_DATE);
      }
    });
    sortingMenuItems.add(sortDates);
    menuActions.add(sortDates);

    final JRadioButtonMenuItem sortNames = new JRadioButtonMenuItem("Sort Files by Name",
                                                                    true);
    sortNames.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(final ActionEvent actionevent)
      {
        fileItemSortList.sort(FileComparator.BY_NAME);
      }
    });
    sortingMenuItems.add(sortNames);
    menuActions.add(sortNames);

    menuActions.addSeparator();
    toolBar.addSeparator();

    toolBar.add(new JLabel("File stem:"));
    final JTextField stemField = new JTextField("filestem", 10);
    toolBar.add(stemField);
    final GuiAction renameAction = new GuiAction("Rename", "/icons/rename.gif");
    renameAction.setShortcutKey(KeyStroke.getKeyStroke("control R"));
    renameAction.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(final ActionEvent event)
      {
        try
        {
          final String fileStem = stemField.getText();

          final FilesRenamer filesRenamer = new FilesRenamer(fileItemSortList
            .getFiles(), fileStem);
          filesRenamer.rename();
        }
        catch (Exception e)
        {
          logger.log(Level.SEVERE, e.getMessage(), e);
        }

        fileItemSortList.reload();
      }
    });
    menuActions.add(renameAction);
    toolBar.add(renameAction);

    menuBar.add(menuActions);
  }

  private void createFileMenu(final JMenuBar menuBar, final JToolBar toolBar)
  {

    final JMenu menuFile = new JMenu("File");

    final GuiAction open = new GuiAction("Open", "/icons/open.gif");
    open.setShortcutKey(KeyStroke.getKeyStroke("control O"));
    open.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(final ActionEvent actionevent)
      {
        open();
      }
    });
    menuFile.add(open);
    toolBar.add(open);

    final GuiAction close = new GuiAction("Close", "/icons/close.gif");
    close.setShortcutKey(KeyStroke.getKeyStroke("control W"));
    close.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(final ActionEvent actionevent)
      {
        close();
      }
    });
    menuFile.add(close);
    toolBar.add(close);

    menuFile.addSeparator();
    toolBar.addSeparator();

    final ExitAction exit = new ExitAction(this, "Exit");
    menuFile.add(exit);

    menuBar.add(menuFile);

  }

  private void createHelpMenu(final JMenuBar menuBar, final JToolBar toolBar)
  {

    final JMenu menuHelp = new JMenu("Help");

    final GuiAction about = new GuiAction("About", "/icons/help.gif");
    about.setShortcutKey(KeyStroke.getKeyStroke("control H"));
    about.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(final ActionEvent actionevent)
      {
        JOptionPane.showMessageDialog(PhotosRenamerWindow.this,
                                      Version.about(),
                                      Version.getProductName(),
                                      JOptionPane.PLAIN_MESSAGE);
      }
    });
    menuHelp.add(about);

    menuBar.add(menuHelp);

  }

  private void open()
  {

    final JFileChooser openDialog = new JFileChooser();
    openDialog.setDialogType(JFileChooser.OPEN_DIALOG);
    openDialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    openDialog.setDialogTitle("Choose Directory");
    openDialog.setCurrentDirectory(workingDir.toFile());
    openDialog.showDialog(this, "Choose");

    final File selectedWorkingDir = openDialog.getSelectedFile();
    if (selectedWorkingDir != null)
    {
      setWorkingDirectory(selectedWorkingDir.toPath());
    }
  }

  /**
   * Set working directory, or null to close.
   * 
   * @param workingDir
   *        Working directory
   */
  private void setWorkingDirectory(final Path workingDir)
  {
    fileItemSortList.setWorkingDirectory(workingDir);

    if (workingDir != null)
    {
      this.workingDir = workingDir;
      preferences.put("WorkingDirectory", workingDir.toAbsolutePath()
        .toString());
      directoryBar.setText(workingDir.toString());
    }
    else
    {
      directoryBar.setText("");
    }
  }

}
