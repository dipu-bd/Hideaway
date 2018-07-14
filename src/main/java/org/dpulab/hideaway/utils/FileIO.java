package org.dpulab.hideaway.utils;

import java.awt.Component;
import java.io.File;
import javax.swing.JFileChooser;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author dipu
 */
public class FileIO {
    
    private static final FileIO INSTANCE = new FileIO();
    
    public static FileIO getDefault() {
        return FileIO.INSTANCE;
    }
    
    private FileIO() {
        // hides FileIO constructor
    }
    
    /**
     * Displays a file chooser dialog to select a folder.
     * It keeps the last visited folder path in memory.
     * @param parent The parent component. Can be <code>null</code>.
     * @return The selected folder, or null if nothing is selected.
     */
    public String chooseFolder(Component parent) {
        String lastDirectory = Settings.getDefault().get("LAST_DIRECTORY");
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new java.io.File(lastDirectory));
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setFileHidingEnabled(false);
        fileChooser.setApproveButtonText("Select");
        int returnVal = fileChooser.showSaveDialog(parent);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            File workFolder = fileChooser.getSelectedFile();
            Settings.getDefault().set("LAST_DIRECTORY", workFolder.getParent());
            return workFolder.getAbsolutePath();
        }
        return null;
    }
    
    
}
