/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dpulab.hideaway.view;

import java.awt.Color;
import javax.swing.SwingUtilities;
import org.dpulab.hideaway.Program;
import static org.dpulab.hideaway.Program.start;
import org.dpulab.hideaway.models.DashboardPage;
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
    }
    
    void selectPage(DashboardPage page) {
        this.selectedPage = page;
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

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
        newFileButton = new javax.swing.JButton();
        newFolderButton = new javax.swing.JButton();
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
        changePasswordButton = new javax.swing.JButton();
        verticalSeparator3 = new javax.swing.JSeparator();
        mainPanel = new javax.swing.JPanel();
        dataViewerScrollPane = new javax.swing.JScrollPane();
        dataViewer = new javax.swing.JTable();

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

        pathInput.setBackground(new java.awt.Color(214, 217, 223));
        pathInput.setFont(new java.awt.Font("Monospaced", 1, 16)); // NOI18N
        pathInput.setForeground(new java.awt.Color(32, 78, 78));
        pathInput.setText("Keystore");
        pathInput.setToolTipText("");
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

        newFileButton.setFont(newFileButton.getFont().deriveFont(newFileButton.getFont().getSize()+12f));
        newFileButton.setText("<html>&#x1f5ba;</html>");
        newFileButton.setToolTipText("New file");
        newFileButton.setActionCommand("File");
        newFileButton.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), javax.swing.BorderFactory.createEmptyBorder(3, 10, 3, 10)));
        newFileButton.setFocusPainted(false);
        newFileButton.setFocusable(false);
        newFileButton.setMaximumSize(new java.awt.Dimension(2147483647, 40));
        newFileButton.setMinimumSize(new java.awt.Dimension(46, 40));
        newFileButton.setOpaque(true);
        newFileButton.setPreferredSize(new java.awt.Dimension(46, 40));
        actionToolbar.add(newFileButton);

        newFolderButton.setFont(newFolderButton.getFont().deriveFont(newFolderButton.getFont().getSize()+12f));
        newFolderButton.setText("<html>&#x1f5bf;</html>");
        newFolderButton.setToolTipText("New folder");
        newFolderButton.setActionCommand("Folder");
        newFolderButton.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), javax.swing.BorderFactory.createEmptyBorder(3, 10, 3, 10)));
        newFolderButton.setFocusPainted(false);
        newFolderButton.setFocusable(false);
        newFolderButton.setMaximumSize(new java.awt.Dimension(2147483647, 40));
        newFolderButton.setMinimumSize(new java.awt.Dimension(46, 40));
        newFolderButton.setOpaque(true);
        newFolderButton.setPreferredSize(new java.awt.Dimension(46, 40));
        actionToolbar.add(newFolderButton);

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
        homeButton.setText("<html>&#x1f5b4; Home</html>");
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
        importFileButton.setText("<html>&#x271c; Import File</html>");

        importFolderButton.setBackground(new java.awt.Color(192, 190, 196));
        importFolderButton.setFont(importFolderButton.getFont().deriveFont(importFolderButton.getFont().getSize()+2f));
        importFolderButton.setText("<html>&#x27f4; Import Folder</html>");

        keystoreButton.setFont(keystoreButton.getFont().deriveFont(keystoreButton.getFont().getSize()+5f));
        keystoreButton.setText("<html>&#x26d3; Keystore</html>");
        keystoreButton.setBorderPainted(false);
        keystoreButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keystoreButtonActionPerformed(evt);
            }
        });

        changePasswordButton.setBackground(new java.awt.Color(186, 182, 180));
        changePasswordButton.setFont(changePasswordButton.getFont().deriveFont(changePasswordButton.getFont().getSize()+2f));
        changePasswordButton.setText("<html>&#x1f5dd; Change Password</html>");
        changePasswordButton.setToolTipText("");
        changePasswordButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changePasswordButtonActionPerformed(evt);
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
                    .addComponent(importFolderButton, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(changePasswordButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE))
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 120, Short.MAX_VALUE)
                .addComponent(horizontalSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(changePasswordButton, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(generateKeyPairButton, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(keystoreButton, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15))
        );

        verticalSeparator3.setOrientation(javax.swing.SwingConstants.VERTICAL);
        verticalSeparator3.setToolTipText("");

        mainPanel.setBackground(new java.awt.Color(250, 250, 255));

        dataViewer.setBackground(new java.awt.Color(246, 248, 255));
        dataViewer.setFont(dataViewer.getFont().deriveFont(dataViewer.getFont().getSize()+2f));
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
        ));
        dataViewer.setCellSelectionEnabled(true);
        dataViewer.setDoubleBuffered(true);
        dataViewer.setFillsViewportHeight(true);
        dataViewer.setRowHeight(24);
        dataViewer.setRowMargin(3);
        dataViewerScrollPane.setViewportView(dataViewer);

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(dataViewerScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 798, Short.MAX_VALUE)
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
                        .addComponent(sidePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 521, Short.MAX_VALUE))
                    .addComponent(verticalSeparator3)))
        );

        getAccessibleContext().setAccessibleDescription("Hideaway - Secure yourself with confidence");

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void generateKeyPairButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateKeyPairButtonActionPerformed
        KeyPairGenerator kpGen = new KeyPairGenerator(this);
        kpGen.setVisible(true);
        kpGen.dispose();
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

    private void changePasswordButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changePasswordButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_changePasswordButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToolBar actionToolbar;
    private javax.swing.JButton addBookmarkButton;
    private javax.swing.JButton changePasswordButton;
    private javax.swing.JTable dataViewer;
    private javax.swing.JScrollPane dataViewerScrollPane;
    private javax.swing.JButton favoriteButton;
    private javax.swing.JButton generateKeyPairButton;
    private javax.swing.JButton goBackButton;
    private javax.swing.JButton goForwardButton;
    private javax.swing.JButton homeButton;
    private javax.swing.JSeparator horizontalSeparator1;
    private javax.swing.JSeparator horizontalSeparator2;
    private javax.swing.JSeparator horizontalSeparator3;
    private javax.swing.JButton importFileButton;
    private javax.swing.JButton importFolderButton;
    private javax.swing.JButton keystoreButton;
    private javax.swing.JButton logoutButton;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JToolBar navigationToolbar;
    private javax.swing.JButton newFileButton;
    private javax.swing.JButton newFolderButton;
    private javax.swing.JTextField pathInput;
    private javax.swing.JPanel pathSelectorPanel;
    private javax.swing.JButton recentsButton;
    private javax.swing.JButton rootButton;
    private javax.swing.JPanel sidePanel;
    private javax.swing.JPanel topPanel;
    private javax.swing.JSeparator verticalSeparator1;
    private javax.swing.JSeparator verticalSeparator2;
    private javax.swing.JSeparator verticalSeparator3;
    // End of variables declaration//GEN-END:variables
}
