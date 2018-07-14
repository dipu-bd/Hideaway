/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dpulab.hideaway.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystemException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dpulab.hideaway.models.EncryptedFile;
import org.dpulab.hideaway.view.PasswordInput;

/**
 *
 * @author dipu
 */
public class Storage {
    
    private static final HashMap<String, Storage> STORAGE = new HashMap<>();
    
    public static Storage getFor(String folder) {
        if (!Storage.STORAGE.containsKey(folder)) {
            // attach a new storage class with the folder
            Storage.STORAGE.put(folder, new Storage(folder));
        }
        return Storage.STORAGE.get(folder);
    }    
    
    public static Storage getDefault() {
        String folder = Settings.getDefault().get("WORK_DIRECTORY");
        return Storage.getFor(folder);
    }
    
    private final Path workDir;
    private final ArrayList<EncryptedFile> files;
    private final HashMap<String, String> publicKeys;
    
    private Storage(String folder) {
        this.workDir = new File(folder).toPath();
        this.files = new ArrayList<>();
        this.publicKeys = new HashMap<>();
    }
    
    public void checkFolder() throws IOException {
        // Check the working directory
        File folder = this.workDir.toFile();
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                throw new FileSystemException("Failed to create work directory");
            }
            if (!folder.canWrite()) {
                throw new FileSystemException("The work directory do not have write permission");
            }
            Logger.getLogger(Storage.class.getName()).log(Level.INFO, "Created work directory.");
        } else if(!folder.isDirectory()) {
            throw new FileSystemException("The work directory is not a folder");
        }
        
        // Check the subdirectories and index file
        String password = Settings.getDefault().getSession("PASSWORD");
        String passwordHash = CryptoService.getDefault().getHash(password);
        
        File data = workDir.resolve("data").toFile();
        File keys = workDir.resolve("keys").toFile();
        File index = workDir.resolve(passwordHash + ".index").toFile();
        
        if (!index.exists()) {
            // verify the password
            PasswordInput passwordInput = new PasswordInput(null);
            passwordInput.setVisible(true);
            String verifyPassword = passwordInput.getPassword();
            passwordInput.dispose();
            if (!password.equals(verifyPassword)) {
                throw new AccessDeniedException("Retyped password did not match the initial one");
            }
            // create new index file
            index.createNewFile();
            Logger.getLogger(Storage.class.getName()).log(Level.INFO, "Created index file.");
        }
        
        if (!data.exists()) {
            data.mkdir();
            Logger.getLogger(Storage.class.getName()).log(Level.INFO, "Created data folder.");
        }
        
        if (!keys.exists()) {
            keys.mkdir();
            Logger.getLogger(Storage.class.getName()).log(Level.INFO, "Created folder for public keys.");
        }
        
        // TODO: Read index files, verify list
    }
    
    
}
