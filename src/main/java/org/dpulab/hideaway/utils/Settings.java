/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dpulab.hideaway.utils;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 *
 * @author dipu
 */
public class Settings {
    
    private static final Settings INSTANCE = new Settings();
    
    public static Settings getDefault() {
        return Settings.INSTANCE;
    }
    
    private final Preferences preferences;
    
    public Settings() {
        String pathName = "org/dpulab/hideaway/v1";
        this.preferences = Preferences.userRoot().node(pathName);
        
        try {
            this.preferences.sync();
        } catch (BackingStoreException ex) {
            Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        System.out.println("Created preferences: " + this.preferences.absolutePath());
    }
    
    public String get(String name) {
        return this.preferences.get(name, "");
    }
    
    public void set(String name, String value) {
        this.preferences.put(name, value);
    }
}
