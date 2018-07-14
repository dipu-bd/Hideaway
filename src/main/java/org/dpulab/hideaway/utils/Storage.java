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
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.dpulab.hideaway.models.EncryptedFile;
import org.dpulab.hideaway.view.PasswordInput;
import org.json.JSONArray;
import org.json.JSONObject;

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
    private final ArrayList<EncryptedFile> fileList;
    private final HashMap<String, String> publicKeys;
        
    private Storage(String folder) {
        this.workDir = new File(folder).toPath();
        this.fileList = new ArrayList<>();
        this.publicKeys = new HashMap<>();
    }
    
    private void updateStatus(Level level, String status) {
        Logger.getLogger(Storage.class.getName()).log(level, "Created work directory.");
    }
    
    public void checkFolder() throws IOException, GeneralSecurityException {
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
        } else if(!folder.isDirectory()) {
            throw new FileSystemException("The work directory is not a folder");
        }
        
        // Check the subdirectories and index file
        String password = Settings.getDefault().getSession("PASSWORD");
        String passwordHash = CryptoService.getDefault().getHash(password);
        
        File dataFolder = workDir.resolve("data").toFile();
        File keysFolder = workDir.resolve("keys").toFile();
        File indexFile = workDir.resolve(passwordHash + ".index").toFile();
        
        if (!indexFile.exists()) {
            // verify the password
            PasswordInput passwordInput = new PasswordInput(null);
            passwordInput.setVisible(true);
            String verifyPassword = passwordInput.getPassword();
            passwordInput.dispose();
            if (!password.equals(verifyPassword)) {
                throw new AccessDeniedException("Retyped password did not match the initial one");
            }
            // create new index file
            indexFile.createNewFile();
            this.updateStatus(Level.INFO, "Created index file.");
        }
        
        if (!dataFolder.exists()) {
            dataFolder.mkdir();
            this.updateStatus(Level.INFO, "Created data folder.");
        }
        
        if (!keysFolder.exists()) {
            keysFolder.mkdir();
            this.updateStatus(Level.INFO, "Created folder for public keys.");
        }
        
        // Get list of available public keys
        for (File keyFile : keysFolder.listFiles()) {
            if (!keyFile.isFile() || !keyFile.canRead()) {
                continue;
            }
            String name = keyFile.getName();
            String publicKey = FileUtils.readFileToString(keyFile);
            publicKeys.put(name, publicKey);
        }
        
        // Read index fileList
        String indexEntry = CryptoService.getDefault().decryptAES(indexFile, password);
        JSONArray jsonArray = new JSONArray(indexEntry);
        for (int i = 0; i < jsonArray.length(); ++i) {
            JSONObject item = jsonArray.getJSONObject(i);
            EncryptedFile file = new EncryptedFile(item.toMap());
            this.fileList.add(file);
        }
                
        // TODO: Verify file list
        for (EncryptedFile file : this.fileList) {
            
        }
        
    }
    
    
}
