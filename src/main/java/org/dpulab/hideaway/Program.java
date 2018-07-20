/*
 * Copyright (C) 2018 dipu
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
package org.dpulab.hideaway;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.dpulab.hideaway.utils.Settings;
import org.dpulab.hideaway.utils.CipherIO;
import org.dpulab.hideaway.utils.Reporter;
import org.dpulab.hideaway.view.Dashboard;
import org.dpulab.hideaway.view.KeyPairGenerator;
import org.dpulab.hideaway.view.Login;

/**
 *
 * @author dipu
 */
public class Program {
    
    public static void main(String args[]) {
         /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            HashMap<String, String> lfmap = new HashMap<>();
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                lfmap.put(info.getName(), info.getClassName());
            }
            String[] availableThemes = { "GTK+", "Windows", "Macintosh", "Nimbus" };
            for (String key : availableThemes) {
                if (lfmap.containsKey(key)) {
                    Logger.getLogger(Login.class.getName()).log(Level.INFO, "Using {0} look and feel.", key);
                    UIManager.setLookAndFeel(lfmap.get(key));
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
        }
        //</editor-fold>
                
         // new Playground().play();
        
        /* Create and display the form */
        SwingUtilities.invokeLater(() -> { start(); });                
    }
    
    public static void start() {
        // Display login modal
        Login loginFrame = new Login();
        loginFrame.setVisible(true);
        loginFrame.dispose();

        // After login modal closed, check password
        if (Settings.getDefault().getSession(Settings.PASSWORD) == null) {
            Logger.getLogger(Login.class.getName()).log(Level.SEVERE, "No password found.");
            return;
        }

        // Check the folder
        try {
            CipherIO.getDefault().checkFolder();
        } catch (IOException | GeneralSecurityException | ClassNotFoundException ex) {
            Reporter.put(Program.class, ex);
            Reporter.dialog(Level.SEVERE, "Failed to configure work directory.\n\n[ %s ]  ", ex.getMessage());
            System.exit(1);
        }

        // If a password is given show dashboard
        Dashboard dashboard = new Dashboard();
        dashboard.setVisible(true);
        
        dashboard.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Program.beforeExit();
            }
        });
    }
    
    public static void beforeExit() {
        try {
            Settings.getDefault().flush();
            CipherIO.getDefault().saveIndex();
            CipherIO.getDefault().saveKeystore();
        } catch (IOException | GeneralSecurityException | BackingStoreException ex) {
            Reporter.put(ex);
        }
    }
}
