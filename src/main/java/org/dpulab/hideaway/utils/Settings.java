/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dpulab.hideaway.utils;

import java.util.HashMap;
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
    private final HashMap<String, Object> session;
    
    private Settings() {
        String pathName = "org/dpulab/hideaway/v1";
        this.preferences = Preferences.userRoot().node(pathName);
        this.session = new HashMap<>();
        
        try {
            this.preferences.sync();
        } catch (BackingStoreException ex) {
            Reporter.put(Settings.class, ex);
        }
        
        Reporter.format(Settings.class, "Preferences @ %s", this.preferences.absolutePath());
    }
        
    public Preferences getPreferences() {
        return this.preferences;
    }
    
    public String get(String name) {
        return this.preferences.get(name, "");
    }
    
    public void set(String name, String value) {
        this.preferences.put(name, value);
    }
    
    public <T> T getSession(String name) {
        if (this.session.containsKey(name)) {
            return (T) this.session.get(name);
        }
        return null;
    }
    
    public void setSession(String name, Object value) {
        this.session.put(name, value);
    }
}
