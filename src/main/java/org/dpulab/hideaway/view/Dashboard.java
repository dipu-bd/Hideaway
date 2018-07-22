/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dpulab.hideaway.view;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import org.apache.commons.io.FileUtils;
import org.bouncycastle.openssl.PasswordException;
import org.dpulab.hideaway.Program;
import org.dpulab.hideaway.models.DashboardPage;
import org.dpulab.hideaway.models.IndexEntry;
import org.dpulab.hideaway.models.TableModelBuilder;
import org.dpulab.hideaway.utils.CipherIO;
import org.dpulab.hideaway.utils.CryptoService;
import org.dpulab.hideaway.utils.FileIO;
import org.dpulab.hideaway.utils.Reporter;
import org.dpulab.hideaway.utils.Settings;

/**
 *
 * @author dipu
 */
public class Dashboard extends javax.swing.JFrame {

    private DashboardPage selectedPage = DashboardPage.KEY_STORE;

    /**
     * Creates new form Dashboard
     */
    public Dashboard() {
        initComponents();

        this.selectPage(DashboardPage.KEY_STORE);
    }

    public final void selectPage(DashboardPage page) {
        this.selectedPage = page;
        this.dataViewer.setModel(new DefaultTableModel());
        try {
            String icon = "<html> </html>";
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
                case KEY_STORE:
                case UNDEFINED:
                    this.loadKeyStore();
                    icon = this.keystoreButton.getText();
                    this.dataViewer.setComponentPopupMenu(this.keystorePopup);
                    break;
            }
            icon = icon.split(" ")[0] + "</html>";
            this.rootButton.setText(icon);
        } catch (Exception ex) {
            Reporter.put(Dashboard.class, ex);
        }
    }

    void loadFavorites() {

    }

    void loadRecentItems() {

    }

    /*------------------------------------------------------------------------*\
                        KEYSTORE Controller Methods
    \*------------------------------------------------------------------------*/
    void loadKeyStore() throws Exception {
        // Create new table model builder
        TableModelBuilder builder = new TableModelBuilder();
        builder.addColumn("#", "<b style=\"color: #6e6e6e\">%s</b>", 20, 25)
                .addColumn("Alias", "<b>%s</b>", 130, 250)
                .addColumn("Key Type", "<span style=\"color: blue\">%s</span>", 85, 90)
                .addColumn("Algorithm", "<span style=\"color: red\">%s</span>", 85, 90)
                .addColumn("Key Format", "<code>%s</code>", 85, 90)
                .addColumn("Length (B)", "<code style=\"color: navy\">%s</code>", 80, 85)
                .addColumn("Created At", "<span style=\"color: gray\">%s</span>", 135, 145)
                .addColumn("First few bytes of the key", "<code style=\"color: orange\">%s</code>", 300);

        // Load data to builder
        int index = 1;
        KeyStore store = CipherIO.getDefault().getKeyStore();
        for (String alias : Collections.list(store.aliases())) {
            Key key = null;
            String keyType = "";
            Date createdAt = store.getCreationDate(alias);
            if (store.isKeyEntry(alias)) {
                key = store.getKey(alias, CipherIO.getDefault().getKeystorePass());
                keyType = "Private";
            } else if (store.isCertificateEntry(alias)) {
                Certificate cert = store.getCertificate(alias);
                key = (Key) cert.getPublicKey();
                keyType = "Public";
            }
            if (key != null) {
                builder.addData(
                        index++,
                        alias,
                        keyType,
                        key.getAlgorithm(),
                        key.getFormat(),
                        key.getEncoded().length,
                        new SimpleDateFormat("HH:mm:ss dd/MM/yy").format(createdAt),
                        CryptoService.getBytePreview(key.getEncoded(), 20)
                );
            }
        }

        SwingUtilities.invokeLater(() -> {
            // Set data to viewer
            builder.build(this.dataViewer);
            this.dataViewer.setRowHeight(22);
            this.dataViewer.setRowMargin(0);

            // Customize the UI
            this.dataViewer.setRowSelectionAllowed(true);
            this.dataViewer.setCellSelectionEnabled(false);
            this.pathInput.setText("Keystore");
        });
    }

    private void displayKeyGen() {
        SwingUtilities.invokeLater(() -> {
            KeyPairGenerator kpGen = new KeyPairGenerator(this);
            kpGen.setVisible(true);
            kpGen.dispose();
            this.selectPage(DashboardPage.KEY_STORE);
        });
    }

    private void exportSelectedKey() {
        int row = this.dataViewer.getSelectedRow();
        if (row == -1) {
            return;
        }
        String alias = (String) this.dataViewer.getModel().getValueAt(row, 1);
        alias = alias.replaceAll("<[^>]+>", "");

        try {
            Key key = CipherIO.getDefault().getKeyEntry(alias);
            String output = CryptoService.getKeyAsString(key);
            FileIO.saveToFile(this, output);
        } catch (IOException | GeneralSecurityException ex) {
            Reporter.put(getClass(), ex);
            Reporter.dialog(Level.SEVERE, "Failed to save key: %s", alias);
        }
    }

    private void removeSelectedKey() {
        int row = this.dataViewer.getSelectedRow();
        if (row == -1) {
            return;
        }
        String alias = (String) this.dataViewer.getModel().getValueAt(row, 1);
        alias = alias.replaceAll("<[^>]+>", "");

        if (alias.endsWith("_cert") || alias.endsWith("_key")) {
            alias = alias.substring(0, alias.lastIndexOf("_"));
        } else {
            Reporter.dialog(Level.SEVERE, "You are only allowed to delete RSA key pairs");
            return;
        }

        int result = JOptionPane.showConfirmDialog(
                this,
                String.format("Are you sure to remove these keys: %s_cert, %s_key?", alias, alias),
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            try {
                CipherIO.getDefault().deleteKeyEntry(alias);
                Reporter.dialog("Deleted keys: %s_cert and %s_key", alias, alias);
                this.selectPage(DashboardPage.KEY_STORE);
            } catch (IOException | GeneralSecurityException ex) {
                Reporter.put(getClass(), ex);
                Reporter.dialog(Level.SEVERE, "Failed to delete keypair: %s", alias);
            }
        }
    }

    /*------------------------------------------------------------------------*\
                        BROWSER Controller Methods
    \*------------------------------------------------------------------------*/
    private IndexEntry selectedEntry = null;

    void loadBrowser() {
        try {
            this.selectedEntry = CipherIO.getDefault().getRootIndex();

            SwingUtilities.invokeLater(() -> {
                // Customize the UI
                this.pathInput.setText(CipherIO.SEPARATOR);
            });
        } catch (UnsupportedEncodingException | KeyStoreException | NoSuchAlgorithmException | PasswordException ex) {
            Reporter.put(getClass(), ex);
        }
    }

    private void importExternalFile() {
        if (this.selectedPage != DashboardPage.BROWSER) {
            Reporter.dialog("You should be in Browser page to import files");
            return;
        }

        String filePath = FileIO.chooseOpenFile(this);
        if (filePath == null) {
            return;
        }

        File file = new File(filePath);
        String keyAlias = "test";

        try {
            // read data
            byte[] data = FileUtils.readFileToByteArray(file);
            String checksum = CryptoService.getDefault().getChecksum(data);

            // create a index entry
            IndexEntry entry = this.selectedEntry.createNewFile(
                    file.getName(), data.length, checksum, keyAlias);
            CipherIO.getDefault().saveIndex();

            // write texts
            CipherIO.getDefault().writeToCipherFile(entry, data);
        } catch (IOException | GeneralSecurityException ex) {
            Logger.getLogger(Dashboard.class.getName()).log(Level.SEVERE, null, ex);
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

        keystorePopup = new javax.swing.JPopupMenu();
        createKeyMenu = new javax.swing.JMenuItem();
        removeKeyMenu = new javax.swing.JMenuItem();
        keystorePopupSeparator1 = new javax.swing.JPopupMenu.Separator();
        exportKeyButton = new javax.swing.JMenuItem();
        importKeyButton = new javax.swing.JMenuItem();
        keystorePopupSeparator2 = new javax.swing.JPopupMenu.Separator();
        refreshButton = new javax.swing.JMenuItem();
        browserPopup = new javax.swing.JPopupMenu();
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
        generateKeyPairButton = new javax.swing.JButton();
        horizontalSeparator2 = new javax.swing.JSeparator();
        importFileButton = new javax.swing.JButton();
        importFolderButton = new javax.swing.JButton();
        horizontalSeparator3 = new javax.swing.JSeparator();
        keystoreButton = new javax.swing.JButton();
        verticalSeparator3 = new javax.swing.JSeparator();
        mainPanel = new javax.swing.JPanel();
        dataViewerScrollPane = new javax.swing.JScrollPane();
        dataViewer = new javax.swing.JTable();

        createKeyMenu.setText("Create New");
        createKeyMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createKeyMenuActionPerformed(evt);
            }
        });
        keystorePopup.add(createKeyMenu);

        removeKeyMenu.setText("Remove");
        removeKeyMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeKeyMenuActionPerformed(evt);
            }
        });
        keystorePopup.add(removeKeyMenu);
        keystorePopup.add(keystorePopupSeparator1);

        exportKeyButton.setText("Export Key");
        exportKeyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportKeyButtonActionPerformed(evt);
            }
        });
        keystorePopup.add(exportKeyButton);

        importKeyButton.setText("Import Key");
        keystorePopup.add(importKeyButton);
        keystorePopup.add(keystorePopupSeparator2);

        refreshButton.setText("Refresh");
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });
        keystorePopup.add(refreshButton);

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
        rootButton.setText("<html>&#x26d3;</html>");
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
        pathInput.setText("Keystore");
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

        generateKeyPairButton.setBackground(new java.awt.Color(186, 182, 180));
        generateKeyPairButton.setFont(generateKeyPairButton.getFont().deriveFont(generateKeyPairButton.getFont().getSize()+2f));
        generateKeyPairButton.setText("<html>&#x26cf; Generate Key Pair</html>");
        generateKeyPairButton.setToolTipText("");
        generateKeyPairButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateKeyPairButtonActionPerformed(evt);
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

        keystoreButton.setFont(keystoreButton.getFont().deriveFont(keystoreButton.getFont().getSize()+5f));
        keystoreButton.setText("<html>&#x26d3; Keystore</html>");
        keystoreButton.setBorderPainted(false);
        keystoreButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keystoreButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout sidePanelLayout = new javax.swing.GroupLayout(sidePanel);
        sidePanel.setLayout(sidePanelLayout);
        sidePanelLayout.setHorizontalGroup(
            sidePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(horizontalSeparator2)
            .addComponent(horizontalSeparator3)
            .addGroup(sidePanelLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(sidePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(importFileButton)
                    .addComponent(favoriteButton, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(homeButton, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(keystoreButton)
                    .addComponent(recentsButton)
                    .addComponent(generateKeyPairButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
                    .addComponent(importFolderButton, javax.swing.GroupLayout.Alignment.LEADING))
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 191, Short.MAX_VALUE)
                .addComponent(horizontalSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(generateKeyPairButton, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(keystoreButton, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15))
        );

        verticalSeparator3.setOrientation(javax.swing.SwingConstants.VERTICAL);
        verticalSeparator3.setToolTipText("");

        dataViewer.setAutoCreateRowSorter(true);
        dataViewer.setBackground(new java.awt.Color(246, 248, 255));
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
                .addComponent(dataViewerScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 522, Short.MAX_VALUE)
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

    private void generateKeyPairButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateKeyPairButtonActionPerformed
        this.displayKeyGen();
    }//GEN-LAST:event_generateKeyPairButtonActionPerformed

    private void goBackButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_goBackButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_goBackButtonActionPerformed

    private void goForwardButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_goForwardButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_goForwardButtonActionPerformed

    private void rootButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rootButtonActionPerformed
        this.selectPage(this.selectedPage);
    }//GEN-LAST:event_rootButtonActionPerformed

    private void keystoreButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keystoreButtonActionPerformed
        this.selectPage(DashboardPage.KEY_STORE);
    }//GEN-LAST:event_keystoreButtonActionPerformed

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

    private void createKeyMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createKeyMenuActionPerformed
        this.displayKeyGen();
    }//GEN-LAST:event_createKeyMenuActionPerformed

    private void removeKeyMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeKeyMenuActionPerformed
        this.removeSelectedKey();
    }//GEN-LAST:event_removeKeyMenuActionPerformed

    private void exportKeyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportKeyButtonActionPerformed
        this.exportSelectedKey();
    }//GEN-LAST:event_exportKeyButtonActionPerformed

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        try {
            CipherIO.getDefault().loadKeystore();
            this.selectPage(DashboardPage.KEY_STORE);
        } catch (IOException | GeneralSecurityException ex) {
            Reporter.put(getClass(), ex);
        }
    }//GEN-LAST:event_refreshButtonActionPerformed

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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToolBar actionToolbar;
    private javax.swing.JButton addBookmarkButton;
    private javax.swing.JPopupMenu browserPopup;
    private javax.swing.JMenuItem createKeyMenu;
    private javax.swing.JTable dataViewer;
    private javax.swing.JScrollPane dataViewerScrollPane;
    private javax.swing.JMenuItem exportKeyButton;
    private javax.swing.JButton favoriteButton;
    private javax.swing.JPopupMenu favoritesPopup;
    private javax.swing.JButton generateKeyPairButton;
    private javax.swing.JButton goBackButton;
    private javax.swing.JButton goForwardButton;
    private javax.swing.JButton homeButton;
    private javax.swing.JSeparator horizontalSeparator1;
    private javax.swing.JSeparator horizontalSeparator2;
    private javax.swing.JSeparator horizontalSeparator3;
    private javax.swing.JButton importFileButton;
    private javax.swing.JButton importFileToolButton;
    private javax.swing.JButton importFolderButton;
    private javax.swing.JButton importFolderToolButton;
    private javax.swing.JMenuItem importKeyButton;
    private javax.swing.JButton keystoreButton;
    private javax.swing.JPopupMenu keystorePopup;
    private javax.swing.JPopupMenu.Separator keystorePopupSeparator1;
    private javax.swing.JPopupMenu.Separator keystorePopupSeparator2;
    private javax.swing.JButton logoutButton;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JToolBar navigationToolbar;
    private javax.swing.JTextField pathInput;
    private javax.swing.JPanel pathSelectorPanel;
    private javax.swing.JPopupMenu recentItemsPopup;
    private javax.swing.JButton recentsButton;
    private javax.swing.JMenuItem refreshButton;
    private javax.swing.JMenuItem removeKeyMenu;
    private javax.swing.JButton rootButton;
    private javax.swing.JPanel sidePanel;
    private javax.swing.JPanel topPanel;
    private javax.swing.JSeparator verticalSeparator1;
    private javax.swing.JSeparator verticalSeparator2;
    private javax.swing.JSeparator verticalSeparator3;
    // End of variables declaration//GEN-END:variables

}
