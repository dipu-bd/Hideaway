/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dpulab.hideaway.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import org.dpulab.hideaway.models.EncryptedFile;

/**
 *
 * @author dipu
 */
public class Storage {
    
    private static final HashMap<String, Storage> STORAGE = new HashMap<>();
    
    public static Storage getStorage() throws FileSystemException, SecurityException {
        String folder = Settings.getDefault().get("WORK_DIRECTORY");
        if (!Storage.STORAGE.containsKey(folder)) {
            // validate the work folder
            File workDir = new File(folder);
            if (!workDir.exists()) {
                if (!workDir.mkdirs()) {
                    throw new FileSystemException("Failed to create work directory");
                }
                if (!workDir.canWrite()) {
                    throw new FileSystemException("The work directory do not have write permission");
                }
            }
            // attach a new storage class with the folder
            Storage.STORAGE.put(folder, new Storage(workDir));
        }
        return Storage.STORAGE.get(folder);
    }
    
    private final Path workDir;
    private final ArrayList<EncryptedFile> files;
    private final HashMap<String, String> publicKeys;
    
    private Storage(File workDir) {
        this.workDir = workDir.toPath();
        this.files = new ArrayList<>();
        this.publicKeys = new HashMap<>();
    }
    
    public void checkFolder() throws IOException {
        String password = Settings.getDefault().getSession("PASSWORD");
        String passwordHash = CryptoService.getDefault().getHash(password);
        
        File data = workDir.resolve("data").toFile();
        File keys = workDir.resolve("keys").toFile();
        File index = workDir.resolve(passwordHash + ".index").toFile();
        
        if (!data.exists()) data.mkdir();
        if (!keys.exists()) data.mkdir();
        if (!index.exists()) {
            index.createNewFile();
        }
    }
    
    
}
