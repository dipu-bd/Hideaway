/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dpulab.hideaway.utils;

import java.util.ArrayList;
import java.util.HashMap;
import org.dpulab.hideaway.models.EncryptedFile;

/**
 *
 * @author dipu
 */
public class Storage {
    
    private static final HashMap<String, Storage> STORAGE = new HashMap<>();
    
    public static Storage getStorage(String folder) {
        if (!Storage.STORAGE.containsKey(folder)) {
            Storage.STORAGE.put(folder, new Storage(folder));
        }
        return Storage.STORAGE.get(folder);
    }
    
    private final String workPath;
    private final ArrayList<EncryptedFile> files;
    private final HashMap<String, String> publicKeys;
    
    private Storage(String folder) {
        this.workPath = folder;
        this.files = new ArrayList<>();
        this.publicKeys = new HashMap<>();
    }
    
    public void checkFolder() {
        
    }
    
    
}
