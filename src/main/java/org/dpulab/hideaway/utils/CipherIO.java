/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dpulab.hideaway.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystemException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.dpulab.hideaway.models.CipherFile;
import org.dpulab.hideaway.view.PasswordInput;

/**
 *
 * @author dipu
 */
public class CipherIO {

    //<editor-fold defaultstate="collapsed" desc=" Get instance methods ">
    private static final HashMap<String, CipherIO> STORAGE = new HashMap<>();

    public static CipherIO getFor(String folder) {
        if (!CipherIO.STORAGE.containsKey(folder)) {
            // attach a new storage class with the folder
            CipherIO.STORAGE.put(folder, new CipherIO(folder));
        }
        return CipherIO.STORAGE.get(folder);
    }

    public static CipherIO getDefault() {
        String folder = Settings.getDefault().get("WORK_DIRECTORY");
        return CipherIO.getFor(folder);
    }
    //</editor-fold>

    private final Path workDir;
    private final ArrayList<CipherFile> fileList;
    private final HashMap<String, String> publicKeys;

    private CipherIO(String folder) {
        this.workDir = new File(folder).toPath();
        this.fileList = new ArrayList<>();
        this.publicKeys = new HashMap<>();
    }

    // TODO: Save status somewhere and display them
    private void updateStatus(Level level, String status) {
        Logger.getLogger(CipherIO.class.getName()).log(level, status);
    }

    public File getDataFolder() {
        return workDir.resolve("data").toFile();
    }

    public File getKeysFolder() {
        return workDir.resolve("keys").toFile();
    }

    public File getIndexFile() {
        String password = Settings.getDefault().getSession("PASSWORD");
        String passwordHash = CryptoService.getDefault().getHash(password);
        return workDir.resolve(passwordHash + ".index").toFile();
    }

    // TODO: Use multi-threading here
    public void checkFolder() throws IOException, GeneralSecurityException, ClassNotFoundException {
        // Check the working directory
        File folder = this.workDir.toFile();
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                throw new FileSystemException("Failed to create work directory");
            }
            if (!folder.canWrite()) {
                throw new FileSystemException("The work directory do not have write permission");
            }
            this.updateStatus(Level.INFO, "Created work directory");
        } else if (!folder.isDirectory()) {
            throw new FileSystemException("The work directory is not a folder");
        }

        // Check the subdirectories and index file
        File indexFile = this.getIndexFile();
        if (!indexFile.exists()) {
            // verify the password
            if (!this.confirmPassword()) {
                throw new AccessDeniedException("Retyped password did not match the initial one");
            }
            // create new index file
            indexFile.createNewFile();
            this.backupIndex();
            this.updateStatus(Level.INFO, "Created index file.");
        }

        File dataFolder = this.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdir();
            this.updateStatus(Level.INFO, "Created data folder.");
        }

        File keysFolder = this.getKeysFolder();
        if (!keysFolder.exists()) {
            keysFolder.mkdir();
            this.updateStatus(Level.INFO, "Created folder for public keys.");
        }

        // Get list of available public keys
        this.loadPublicKeys();

        // Read index files
        this.restoreIndex();

        // Verify file list
        this.verifyFileList();
    }

    public boolean confirmPassword() {
        String password = Settings.getDefault().getSession("PASSWORD");
        PasswordInput passwordInput = new PasswordInput(null);
        passwordInput.setVisible(true);
        String verifyPassword = passwordInput.getPassword();
        passwordInput.dispose();
        return password.equals(verifyPassword);
    }

    public void loadPublicKeys() throws IOException {
        File keysFolder = this.getKeysFolder();
        for (File keyFile : keysFolder.listFiles()) {
            if (!keyFile.isFile() || !keyFile.canRead()) {
                continue;
            }
            String name = keyFile.getName();
            String publicKey = FileUtils.readFileToString(keyFile);
            publicKeys.put(name, publicKey);
        }
        this.updateStatus(Level.INFO, String.format("Public keys are loaded. %d are available.", this.publicKeys.size()));
    }

    public void backupIndex() throws GeneralSecurityException, IOException, ClassNotFoundException {
        File indexFile = this.getIndexFile();
        String password = Settings.getDefault().getSession("PASSWORD");
        try (
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(this.fileList.toArray(new CipherFile[0]));
            byte[] plainText = baos.toByteArray();
            CryptoService.getDefault().saveEncrypted(plainText, indexFile, password);
        }
        this.updateStatus(Level.INFO, String.format("Index entry saved. %s files", this.fileList.size()));
    }

    public void restoreIndex() throws GeneralSecurityException, IOException, ClassNotFoundException {
        File indexFile = this.getIndexFile();
        String password = Settings.getDefault().getSession("PASSWORD");
        byte[] indexBuffer = CryptoService.getDefault().loadDecrypted(indexFile, password);
        try (
                ByteArrayInputStream bais = new ByteArrayInputStream(indexBuffer);
                ObjectInputStream ois = new ObjectInputStream(bais)) {
            this.fileList.clear();
            CipherFile[] list = (CipherFile[]) ois.readObject();
            this.fileList.addAll(Arrays.asList(list));
        }
        this.updateStatus(Level.INFO, String.format("Index entry loaded. %d files", this.fileList.size()));
    }

    // TODO: Verify file list
    public void verifyFileList() {
    }

    public void addFile(File sourceFile, String destFolder) {
        
    }
}
