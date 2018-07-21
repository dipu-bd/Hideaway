package org.dpulab.hideaway.utils;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import org.apache.commons.io.FileUtils;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author dipu
 */
public final class FileIO {

    // hide the constructor
    private FileIO() {
    }

    /**
     * Displays a file chooser dialog to select a folder. It keeps the last
     * visited folder path in memory.
     *
     * @param parent The parent component. Can be <code>null</code>.
     * @return The selected folder, or null if nothing is selected.
     */
    public static String chooseFolder(Component parent) {
        String lastDirectory = Settings.getDefault().get("LAST_DIRECTORY");
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new java.io.File(lastDirectory));
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setFileHidingEnabled(false);
        fileChooser.setApproveButtonText("Select");
        int returnVal = fileChooser.showSaveDialog(parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File folder = fileChooser.getSelectedFile();
            Settings.getDefault().set("LAST_DIRECTORY", folder.getParent());
            return folder.getAbsolutePath();
        }
        return null;
    }

    /**
     * Displays a file chooser dialog to select a file. It keeps the last
     * visited folder path in memory.
     *
     * @param parent The parent component. Can be <code>null</code>.
     * @return The selected file, or null if nothing is selected.
     */
    public static String chooseFile(Component parent) {
        String lastDirectory = Settings.getDefault().get("LAST_DIRECTORY");
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new java.io.File(lastDirectory));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileHidingEnabled(false);
        fileChooser.setApproveButtonText("Save");
        int returnVal = fileChooser.showSaveDialog(parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            Settings.getDefault().set("LAST_DIRECTORY", file.getParent());
            return file.getAbsolutePath();
        }
        return null;
    }

    /**
     * Select a file and write all contents to it.
     *
     * @param parent The parent component
     * @param content the text to write
     * @throws IOException
     */
    public static void saveToFile(Component parent, String content) throws IOException {
        String filePath = FileIO.chooseFile(parent);
        if (filePath != null) {
            File file = new File(filePath);
            Settings.getDefault().set("LAST_DIRECTORY", file.getParent());
            FileUtils.writeStringToFile(file, content, "UTF-8");
        }
    }

}
