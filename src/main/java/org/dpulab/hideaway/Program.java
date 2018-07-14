/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dpulab.hideaway;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.dpulab.hideaway.utils.Settings;
import org.dpulab.hideaway.utils.Storage;
import org.dpulab.hideaway.view.Dashboard;
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
                
        /* Create and display the form */
        SwingUtilities.invokeLater(() -> {
            // Display login modal
            Login loginFrame = new Login();
            loginFrame.setVisible(true);
             
            // After login modal closed, check password
            if (Settings.getDefault().getSession("PASSWORD") == null) {
                Logger.getLogger(Login.class.getName()).log(Level.SEVERE, "No password found.");
                return;
            }
            
            // Check the folder
            try {
                Storage.getDefault().checkFolder();
            } catch (IOException | GeneralSecurityException ex) {
                Logger.getLogger(Program.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(null, "Failed to initialize work directory.\n\n" + ex.getMessage() + "\t");
                return;
            }
            
            // If a password is given show dashboard
            Dashboard dashboard = new Dashboard();
            dashboard.setVisible(true);
        });
                        
        //Playground playground = new Playground();
        //playground.play();
    }
}
