/*
 * Copyright (C) 2018 Sudipto Chandra
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.dpulab.hideaway.view;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.openssl.PasswordException;
import org.dpulab.hideaway.Program;
import org.dpulab.hideaway.models.DashboardPage;
import org.dpulab.hideaway.models.IndexEntry;
import org.dpulab.hideaway.models.IndexEntryModel;
import org.dpulab.hideaway.models.ObjectTableModel;
import org.dpulab.hideaway.utils.CipherIO;
import org.dpulab.hideaway.utils.CryptoService;
import org.dpulab.hideaway.utils.FileIO;
import org.dpulab.hideaway.utils.GeneralUtils;
import org.dpulab.hideaway.utils.Reporter;
import org.dpulab.hideaway.utils.Settings;

/**
 *
 * @author dipu
 */
public class Dashboard extends javax.swing.JFrame {

    private DashboardPage selectedPage = DashboardPage.UNDEFINED;

    /**
     * Creates new form Dashboard
     */
    public Dashboard() {
        initComponents();

        this.selectPage(DashboardPage.BROWSER);
    }

    /**
     * Reload the current page
     */
    public final void reloadSelectedPage() {
        this.selectPage(this.selectedPage);
    }

    /**
     * Select the current page
     *
     * @param page the page to select
     */
    public final void selectPage(DashboardPage page) {
        this.selectedPage = page;
        this.dataViewer.setModel(new DefaultTableModel());
        try {
            String icon = null;
            switch (this.selectedPage) {
                case BROWSER:
                    this.loadBrowser();
                    icon = this.homeButton.getText();
                    this.dataViewer.setComponentPopupMenu(this.browserPopup);
                    break;
                case FAVORITES:
                    this.loadFavorites();
                    icon = this.favoriteButton.getText();
                    this.dataViewer.setComponentPopupMenu(this.favoritesPopup);
                    break;
                case RECENT_ITEMS:
                    this.loadRecentItems();
                    icon = this.recentsButton.getText();
                    this.dataViewer.setComponentPopupMenu(this.recentItemsPopup);
                    break;
                case UNDEFINED:
                    this.dataViewer.setModel(new DefaultTableModel());
                    this.dataViewer.setComponentPopupMenu(null);
                    break;
            }
            if (icon == null || StringUtils.containsNone(icon, " ")) {
                icon = "<html><span color=\"red\">!</span></html>";
            } else {
                icon = icon.split(" ")[0] + "</html>";
            }
            this.rootButton.setText(icon);
        } catch (Exception ex) {
            Reporter.put(Dashboard.class, ex);
        }
    }

    /*------------------------------------------------------------------------*\
                        FAVORITES Controller Methods
    \*------------------------------------------------------------------------*/
    void loadFavorites() {

    }

    /*------------------------------------------------------------------------*\
                        RECENT_ITEMS Controller Methods
    \*------------------------------------------------------------------------*/
    void loadRecentItems() {

    }

    /*------------------------------------------------------------------------*\
                        BROWSER Controller Methods
    \*------------------------------------------------------------------------*/
    private IndexEntry selectedEntry = null;

    void refreshBrowser() {
        try {
            CipherIO.instance().loadIndex();
            this.loadBrowser(this.selectedEntry);
        } catch (GeneralSecurityException | IOException | UnsupportedClassVersionError ex) {
            Reporter.put(getClass(), (Exception) ex);
        }
    }

    void loadBrowser() {
        this.loadBrowser(null);
    }

    void loadBrowser(IndexEntry parent) {
        ObjectTableModel<IndexEntry> model = new ObjectTableModel<>();
        model.
                .addColumn("#", "<b style=\"color: #6e6e6e\">%s</b>", 20, 25)
                .addColumn("Name", "<b>%s</b>", 180, 350)
                .addColumn("Size", "<code>%s</code>", 85, 90)
                .addColumn("Type", "", 85, 90)
                .addColumn("Last Modified", "<span style=\"color: #777\">%s</span>", 135, 145)
                .addColumn("Checksum", "<code style=\"color: gray\">%s</code>", 300);

        try {
            if (parent == null) {
                parent = CipherIO.instance().getRootIndex();
            }
            this.selectedEntry = parent;

            int index = 1;
            for (IndexEntry entry : this.selectedEntry.getChildren()) {
                String lastModifyDate = "-";
                if (entry.isFile()) {
                    File cipherFile = entry.getCipherFile();
                    lastModifyDate = GeneralUtils.formatDate(cipherFile.lastModified());
                }
                model.addData(
                        entry,
                        index++,
                        entry.getFileName(),
                        entry.getFileSizeReadable(),
                        entry.isFile() ? "File" : "Directory",
                        lastModifyDate,
                        entry.getChecksum()
                );
            }

            SwingUtilities.invokeLater(() -> {
                model.attachTo(this.dataViewer);
                this.dataViewer.setRowHeight(22);
                this.dataViewer.setRowMargin(0);

                // Customize the UI
                this.dataViewer.setCellSelectionEnabled(false);
                this.pathInput.setText(this.selectedEntry.getPath() + CipherIO.SEPARATOR);
            });
        } catch (IOException | GeneralSecurityException ex) {
            Reporter.put(getClass(), ex);
        }
    }

    private String validateFilename(IndexEntry entry, String fileName) {
        while (entry.hasChild(fileName)) {
            // get the new fileName
            RenameDialog dialog = new RenameDialog(this);
            dialog.setRemember(false);
            dialog.setSkipButton("Cancel");
            dialog.setVisible(true);
            switch (dialog.getDialogResult()) {
                case RENAME:
                    fileName = dialog.getInputText();
                    break;
                case REPLACE:
                    return fileName;
                default:
                    return null;
            }
        }
        return fileName;
    }

    private void importExternalFile() {
        // check if parent folder is available
        if (this.selectedPage != DashboardPage.BROWSER) {
            this.selectPage(DashboardPage.BROWSER);
        }
        if (this.selectedEntry == null) {
            Reporter.dialog("You should be in Browser page to import files");
            return;
        }
        // choose and validate a file path
        String filePath = FileIO.chooseOpenFile(this);
        if (filePath == null) {
            return;
        }
        // gets the file and the name
        File file = new File(filePath);
        String fileName = file.getName();
        // check if an entry of similar name already exists
        fileName = this.validateFilename(this.selectedEntry, fileName);
        if (StringUtils.isEmpty(fileName)) {
            return;
        }
        // get the possible full path of the file in the index
        String path = IndexEntry.join(this.selectedEntry.getPath(), fileName);
        try {
            // get the unique checksum of the file
            String checksum = CryptoService.getDefault().getChecksum(path, file);
            // create an file entry and save index
            IndexEntry entry = this.selectedEntry.createNewFile(fileName, file.length(), checksum);
            CipherIO.instance().saveIndex();
            // encrypt and copy plain text to the file
            CipherIO.instance().copyFileEncrypted(file, entry);
            // reload the viewer
            this.loadBrowser(this.selectedEntry);
        } catch (IOException | GeneralSecurityException ex) {
            Reporter.put(Dashboard.class, ex);
        }
    }

    private void deleteIndexEntry() {
        int row = this.dataViewer.getSelectedRow();
        if (row == -1) {
            return;
        }
        IndexEntryModel model = (IndexEntryModel) this.dataViewer.getModel();
        IndexEntry entry = (IndexEntry) model.getTag(row);

        int result = JOptionPane.showConfirmDialog(
                this,
                String.format("Are you sure to remove: %s?", entry.getFileName()),
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            try {
                entry.remove();
                CipherIO.instance().saveIndex();
                Reporter.dialog("Deleted file: %s", entry.getFileName());
            } catch (IOException | GeneralSecurityException ex) {
                Reporter.put(getClass(), ex);
                Reporter.dialog(Level.SEVERE, "Failed to delete file: %s", entry.getFileName());
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        browserPopup = new javax.swing.JPopupMenu();
        exportFileMenuItem = new javax.swing.JMenuItem();
        deleteEntryMenuItem = new javax.swing.JMenuItem();
        browserPopupSeparator1 = new javax.swing.JPopupMenu.Separator();
        importFileMenuItem = new javax.swing.JMenuItem();
        browserPopupSeparator2 = new javax.swing.JPopupMenu.Separator();
        refreshBrowserMenuItem = new javax.swing.JMenuItem();
        favoritesPopup = new javax.swing.JPopupMenu();
        recentItemsPopup = new javax.swing.JPopupMenu();
        topPanel = new javax.swing.JPanel();
        navigationToolbar = new javax.swing.JToolBar();
        goBackButton = new javax.swing.JButton();
        goForwardButton = new javax.swing.JButton();
        verticalSeparator1 = new javax.swing.JSeparator();
        pathSelectorPanel = new javax.swing.JPanel();
        rootButton = new javax.swing.JButton();
        pathInput = new javax.swing.JTextField();
        verticalSeparator2 = new javax.swing.JSeparator();
        actionToolbar = new javax.swing.JToolBar();
        importFileToolButton = new javax.swing.JButton();
        importFolderToolButton = new javax.swing.JButton();
        addBookmarkButton = new javax.swing.JButton();
        logoutButton = new javax.swing.JButton();
        horizontalSeparator1 = new javax.swing.JSeparator();
        sidePanel = new javax.swing.JPanel();
        homeButton = new javax.swing.JButton();
        recentsButton = new javax.swing.JButton();
        favoriteButton = new javax.swing.JButton();
        horizontalSeparator2 = new javax.swing.JSeparator();
        importFileButton = new javax.swing.JButton();
        importFolderButton = new javax.swing.JButton();
        verticalSeparator3 = new javax.swing.JSeparator();
        mainPanel = new javax.swing.JPanel();
        dataViewerScrollPane = new javax.swing.JScrollPane();
        dataViewer = new javax.swing.JTable();

        exportFileMenuItem.setText("Export");
        browserPopup.add(exportFileMenuItem);

        deleteEntryMenuItem.setText("Delete");
        deleteEntryMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteEntryMenuItemActionPerformed(evt);
            }
        });
        browserPopup.add(deleteEntryMenuItem);
        browserPopup.add(browserPopupSeparator1);

        importFileMenuItem.setText("Import File");
        browserPopup.add(importFileMenuItem);
        browserPopup.add(browserPopupSeparator2);

        refreshBrowserMenuItem.setText("Refresh");
        refreshBrowserMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshBrowserMenuItemActionPerformed(evt);
            }
        });
        browserPopup.add(refreshBrowserMenuItem);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Hideaway");
        setName("hideawayDashboard"); // NOI18N
        setPreferredSize(new java.awt.Dimension(1024, 600));

        topPanel.setBackground(new java.awt.Color(204, 207, 213));

        navigationToolbar.setFloatable(false);
        navigationToolbar.setRollover(true);
        navigationToolbar.setBorderPainted(false);
        navigationToolbar.setMargin(new java.awt.Insets(3, 10, 3, 10));

        goBackButton.setFont(goBackButton.getFont().deriveFont(goBackButton.getFont().getSize()+4f));
        goBackButton.setText("<html>&#10094;</html>");
        goBackButton.setToolTipText("Backward");
        goBackButton.setFocusPainted(false);
        goBackButton.setPreferredSize(new java.awt.Dimension(40, 40));
        goBackButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                goBackButtonActionPerformed(evt);
            }
        });
        navigationToolbar.add(goBackButton);

        goForwardButton.setFont(goForwardButton.getFont().deriveFont(goForwardButton.getFont().getSize()+4f));
        goForwardButton.setText("<html>&#10095;</html>");
        goForwardButton.setToolTipText("Forward");
        goForwardButton.setFocusPainted(false);
        goForwardButton.setPreferredSize(new java.awt.Dimension(40, 40));
        goForwardButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                goForwardButtonActionPerformed(evt);
            }
        });
        navigationToolbar.add(goForwardButton);

        verticalSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        verticalSeparator1.setToolTipText("");

        pathSelectorPanel.setOpaque(false);

        rootButton.setFont(rootButton.getFont().deriveFont(rootButton.getFont().getStyle() | java.awt.Font.BOLD, rootButton.getFont().getSize()+16));
        rootButton.setForeground(new java.awt.Color(102, 0, 51));
        rootButton.setText("<html><span color=\"red\">!</span></html>");
        rootButton.setToolTipText("Root Folder");
        rootButton.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 1, 1, 1, new java.awt.Color(192, 197, 203)));
        rootButton.setContentAreaFilled(false);
        rootButton.setFocusPainted(false);
        rootButton.setOpaque(true);
        rootButton.setPreferredSize(new java.awt.Dimension(50, 40));
        rootButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rootButtonActionPerformed(evt);
            }
        });

        pathInput.setEditable(false);
        pathInput.setBackground(new java.awt.Color(214, 217, 223));
        pathInput.setFont(new java.awt.Font("Monospaced", 1, 16)); // NOI18N
        pathInput.setForeground(new java.awt.Color(32, 78, 78));
        pathInput.setText("/");
        pathInput.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(182, 187, 193)), javax.swing.BorderFactory.createEmptyBorder(3, 8, 3, 8)));
        pathInput.setMargin(new java.awt.Insets(5, 10, 5, 10));
        pathInput.setOpaque(true);

        javax.swing.GroupLayout pathSelectorPanelLayout = new javax.swing.GroupLayout(pathSelectorPanel);
        pathSelectorPanel.setLayout(pathSelectorPanelLayout);
        pathSelectorPanelLayout.setHorizontalGroup(
            pathSelectorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pathSelectorPanelLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(rootButton, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(3, 3, 3)
                .addComponent(pathInput, javax.swing.GroupLayout.DEFAULT_SIZE, 623, Short.MAX_VALUE)
                .addGap(10, 10, 10))
        );
        pathSelectorPanelLayout.setVerticalGroup(
            pathSelectorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pathSelectorPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pathSelectorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pathInput)
                    .addComponent(rootButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(5, 5, 5))
        );

        verticalSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);
        verticalSeparator2.setToolTipText("");

        actionToolbar.setFloatable(false);
        actionToolbar.setRollover(true);
        actionToolbar.setBorderPainted(false);
        actionToolbar.setDoubleBuffered(true);
        actionToolbar.setMargin(new java.awt.Insets(3, 10, 3, 10));

        importFileToolButton.setFont(importFileToolButton.getFont().deriveFont(importFileToolButton.getFont().getSize()+12f));
        importFileToolButton.setText("<html>&#x1f5ba;</html>");
        importFileToolButton.setToolTipText("New file");
        importFileToolButton.setActionCommand("File");
        importFileToolButton.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), javax.swing.BorderFactory.createEmptyBorder(3, 10, 3, 10)));
        importFileToolButton.setFocusPainted(false);
        importFileToolButton.setFocusable(false);
        importFileToolButton.setMaximumSize(new java.awt.Dimension(2147483647, 40));
        importFileToolButton.setMinimumSize(new java.awt.Dimension(46, 40));
        importFileToolButton.setOpaque(true);
        importFileToolButton.setPreferredSize(new java.awt.Dimension(46, 40));
        importFileToolButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importFileToolButtonActionPerformed(evt);
            }
        });
        actionToolbar.add(importFileToolButton);

        importFolderToolButton.setFont(importFolderToolButton.getFont().deriveFont(importFolderToolButton.getFont().getSize()+12f));
        importFolderToolButton.setText("<html>&#x1f5bf;</html>");
        importFolderToolButton.setToolTipText("New folder");
        importFolderToolButton.setActionCommand("Folder");
        importFolderToolButton.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), javax.swing.BorderFactory.createEmptyBorder(3, 10, 3, 10)));
        importFolderToolButton.setFocusPainted(false);
        importFolderToolButton.setFocusable(false);
        importFolderToolButton.setMaximumSize(new java.awt.Dimension(2147483647, 40));
        importFolderToolButton.setMinimumSize(new java.awt.Dimension(46, 40));
        importFolderToolButton.setOpaque(true);
        importFolderToolButton.setPreferredSize(new java.awt.Dimension(46, 40));
        actionToolbar.add(importFolderToolButton);

        addBookmarkButton.setFont(addBookmarkButton.getFont().deriveFont(addBookmarkButton.getFont().getSize()+12f));
        addBookmarkButton.setText("<html>&#x2605;</html>");
        addBookmarkButton.setToolTipText("Bookmark folder");
        addBookmarkButton.setActionCommand("Favorite");
        addBookmarkButton.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), javax.swing.BorderFactory.createEmptyBorder(3, 10, 3, 10)));
        addBookmarkButton.setFocusPainted(false);
        addBookmarkButton.setFocusable(false);
        addBookmarkButton.setMaximumSize(new java.awt.Dimension(2147483647, 40));
        addBookmarkButton.setMinimumSize(new java.awt.Dimension(46, 40));
        addBookmarkButton.setOpaque(true);
        addBookmarkButton.setPreferredSize(new java.awt.Dimension(46, 40));
        actionToolbar.add(addBookmarkButton);

        logoutButton.setBackground(new java.awt.Color(255, 195, 195));
        logoutButton.setFont(logoutButton.getFont().deriveFont(logoutButton.getFont().getStyle() | java.awt.Font.BOLD, logoutButton.getFont().getSize()+12));
        logoutButton.setForeground(new java.awt.Color(153, 51, 0));
        logoutButton.setText("<html>&#x2b93;</html>");
        logoutButton.setToolTipText("Bookmark folder");
        logoutButton.setActionCommand("Favorite");
        logoutButton.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), javax.swing.BorderFactory.createEmptyBorder(3, 10, 3, 10)));
        logoutButton.setContentAreaFilled(false);
        logoutButton.setFocusPainted(false);
        logoutButton.setMaximumSize(new java.awt.Dimension(2147483647, 40));
        logoutButton.setMinimumSize(new java.awt.Dimension(46, 40));
        logoutButton.setOpaque(true);
        logoutButton.setPreferredSize(new java.awt.Dimension(46, 40));
        logoutButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                logoutButtonMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                logoutButtonMouseReleased(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                logoutButtonMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                logoutButtonMouseEntered(evt);
            }
        });
        logoutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logoutButtonActionPerformed(evt);
            }
        });
        actionToolbar.add(logoutButton);

        javax.swing.GroupLayout topPanelLayout = new javax.swing.GroupLayout(topPanel);
        topPanel.setLayout(topPanelLayout);
        topPanelLayout.setHorizontalGroup(
            topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(topPanelLayout.createSequentialGroup()
                .addComponent(navigationToolbar, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(verticalSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(pathSelectorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(verticalSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(actionToolbar, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );
        topPanelLayout.setVerticalGroup(
            topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(verticalSeparator2)
            .addComponent(verticalSeparator1)
            .addComponent(actionToolbar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(navigationToolbar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pathSelectorPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        sidePanel.setBackground(new java.awt.Color(224, 227, 233));
        sidePanel.setPreferredSize(new java.awt.Dimension(200, 361));

        homeButton.setFont(homeButton.getFont().deriveFont(homeButton.getFont().getSize()+5f));
        homeButton.setText("<html>&#x1f5b4; Browser</html>");
        homeButton.setBorderPainted(false);
        homeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                homeButtonActionPerformed(evt);
            }
        });

        recentsButton.setFont(recentsButton.getFont().deriveFont(recentsButton.getFont().getSize()+5f));
        recentsButton.setText("<html>&#x23f2; Recents</html>");
        recentsButton.setBorderPainted(false);
        recentsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                recentsButtonActionPerformed(evt);
            }
        });

        favoriteButton.setFont(favoriteButton.getFont().deriveFont(favoriteButton.getFont().getSize()+5f));
        favoriteButton.setText("<html>&#x2605; Favorites</html>");
        favoriteButton.setBorderPainted(false);
        favoriteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                favoriteButtonActionPerformed(evt);
            }
        });

        importFileButton.setBackground(new java.awt.Color(192, 190, 196));
        importFileButton.setFont(importFileButton.getFont().deriveFont(importFileButton.getFont().getSize()+2f));
        importFileButton.setText("<html><span style=\"font-size: 1.3em\">&#x1f5ba;</span> Import File</html>");
        importFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importFileButtonActionPerformed(evt);
            }
        });

        importFolderButton.setBackground(new java.awt.Color(192, 190, 196));
        importFolderButton.setFont(importFolderButton.getFont().deriveFont(importFolderButton.getFont().getSize()+2f));
        importFolderButton.setText("<html><span style=\"font-size: 1.3em\">&#x1f5bf;</span> Import Folder</html>");

        javax.swing.GroupLayout sidePanelLayout = new javax.swing.GroupLayout(sidePanel);
        sidePanel.setLayout(sidePanelLayout);
        sidePanelLayout.setHorizontalGroup(
            sidePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(horizontalSeparator2)
            .addGroup(sidePanelLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(sidePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(importFileButton)
                    .addComponent(favoriteButton, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(homeButton, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(recentsButton)
                    .addComponent(importFolderButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE))
                .addGap(10, 10, 10))
        );
        sidePanelLayout.setVerticalGroup(
            sidePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sidePanelLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(homeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(recentsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(favoriteButton, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(horizontalSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(importFileButton, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(importFolderButton, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        verticalSeparator3.setOrientation(javax.swing.SwingConstants.VERTICAL);
        verticalSeparator3.setToolTipText("");

        dataViewer.setAutoCreateRowSorter(true);
        dataViewer.setBackground(new java.awt.Color(246, 248, 255));
        dataViewer.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        dataViewer.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                true, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        dataViewer.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_LAST_COLUMN);
        dataViewer.setDoubleBuffered(true);
        dataViewer.setFillsViewportHeight(true);
        dataViewer.setGridColor(new java.awt.Color(225, 231, 240));
        dataViewer.setRowHeight(24);
        dataViewer.setSelectionBackground(new java.awt.Color(0, 204, 204));
        dataViewer.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        dataViewer.getTableHeader().setReorderingAllowed(false);
        dataViewer.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                dataViewerMouseReleased(evt);
            }
        });
        dataViewerScrollPane.setViewportView(dataViewer);

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(dataViewerScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 822, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(dataViewerScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 547, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(sidePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(verticalSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(topPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(horizontalSeparator1, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(topPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(horizontalSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(sidePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 546, Short.MAX_VALUE))
                    .addComponent(verticalSeparator3)))
        );

        getAccessibleContext().setAccessibleDescription("Hideaway - Secure yourself with confidence");

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void goBackButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_goBackButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_goBackButtonActionPerformed

    private void goForwardButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_goForwardButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_goForwardButtonActionPerformed

    private void rootButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rootButtonActionPerformed
        this.reloadSelectedPage();
    }//GEN-LAST:event_rootButtonActionPerformed

    private void homeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_homeButtonActionPerformed
        this.selectPage(DashboardPage.BROWSER);
    }//GEN-LAST:event_homeButtonActionPerformed

    private void recentsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recentsButtonActionPerformed
        this.selectPage(DashboardPage.RECENT_ITEMS);
    }//GEN-LAST:event_recentsButtonActionPerformed

    private void favoriteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_favoriteButtonActionPerformed
        this.selectPage(DashboardPage.FAVORITES);
    }//GEN-LAST:event_favoriteButtonActionPerformed

    private void logoutButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_logoutButtonMouseEntered
        this.logoutButton.setBackground(new Color(255, 51, 51));
        this.logoutButton.setForeground(Color.white);
    }//GEN-LAST:event_logoutButtonMouseEntered

    private void logoutButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_logoutButtonMouseExited
        this.logoutButton.setBackground(new Color(255, 195, 195));
        this.logoutButton.setForeground(new Color(153, 51, 0));
    }//GEN-LAST:event_logoutButtonMouseExited

    private void logoutButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_logoutButtonMousePressed
        this.logoutButton.setBackground(new Color(211, 51, 51));
        this.logoutButton.setForeground(Color.white);
    }//GEN-LAST:event_logoutButtonMousePressed

    private void logoutButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_logoutButtonMouseReleased
        this.logoutButton.setBackground(new Color(255, 51, 51));
        this.logoutButton.setForeground(Color.white);
    }//GEN-LAST:event_logoutButtonMouseReleased

    private void logoutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logoutButtonActionPerformed
        Settings.getDefault().setSession(Settings.PASSWORD, null);
        SwingUtilities.invokeLater(Program::start);
        this.dispose();
    }//GEN-LAST:event_logoutButtonActionPerformed

    private void dataViewerMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_dataViewerMouseReleased
        JTable table = (JTable) evt.getSource();
        int row = table.rowAtPoint(evt.getPoint());
        if (row >= 0 && row < table.getRowCount()) {
            table.setRowSelectionInterval(row, row);
        } else {
            table.clearSelection();
        }
    }//GEN-LAST:event_dataViewerMouseReleased

    private void importFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importFileButtonActionPerformed
        this.importExternalFile();
    }//GEN-LAST:event_importFileButtonActionPerformed

    private void importFileToolButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importFileToolButtonActionPerformed
        this.importExternalFile();
    }//GEN-LAST:event_importFileToolButtonActionPerformed

    private void refreshBrowserMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshBrowserMenuItemActionPerformed
        this.refreshBrowser();
    }//GEN-LAST:event_refreshBrowserMenuItemActionPerformed

    private void deleteEntryMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteEntryMenuItemActionPerformed
        this.deleteIndexEntry();
    }//GEN-LAST:event_deleteEntryMenuItemActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToolBar actionToolbar;
    private javax.swing.JButton addBookmarkButton;
    private javax.swing.JPopupMenu browserPopup;
    private javax.swing.JPopupMenu.Separator browserPopupSeparator1;
    private javax.swing.JPopupMenu.Separator browserPopupSeparator2;
    private javax.swing.JTable dataViewer;
    private javax.swing.JScrollPane dataViewerScrollPane;
    private javax.swing.JMenuItem deleteEntryMenuItem;
    private javax.swing.JMenuItem exportFileMenuItem;
    private javax.swing.JButton favoriteButton;
    private javax.swing.JPopupMenu favoritesPopup;
    private javax.swing.JButton goBackButton;
    private javax.swing.JButton goForwardButton;
    private javax.swing.JButton homeButton;
    private javax.swing.JSeparator horizontalSeparator1;
    private javax.swing.JSeparator horizontalSeparator2;
    private javax.swing.JButton importFileButton;
    private javax.swing.JMenuItem importFileMenuItem;
    private javax.swing.JButton importFileToolButton;
    private javax.swing.JButton importFolderButton;
    private javax.swing.JButton importFolderToolButton;
    private javax.swing.JButton logoutButton;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JToolBar navigationToolbar;
    private javax.swing.JTextField pathInput;
    private javax.swing.JPanel pathSelectorPanel;
    private javax.swing.JPopupMenu recentItemsPopup;
    private javax.swing.JButton recentsButton;
    private javax.swing.JMenuItem refreshBrowserMenuItem;
    private javax.swing.JButton rootButton;
    private javax.swing.JPanel sidePanel;
    private javax.swing.JPanel topPanel;
    private javax.swing.JSeparator verticalSeparator1;
    private javax.swing.JSeparator verticalSeparator2;
    private javax.swing.JSeparator verticalSeparator3;
    // End of variables declaration//GEN-END:variables

}
