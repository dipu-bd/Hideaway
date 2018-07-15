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
    
    /*
     * The public constants
     */
    // crypto algorithms
    public static final String DEFAULT_CHARSET = "UTF-8";
    public static final String STORE_TYPE = "PKCS12";
    public static final String AES_ALGORITHM = "AES/CBC/PKCS5Padding";
    public static final String INDEX_KEY_ALIAS = "INDEX_PASSWD";
    public static final String INDEX_IV_ALIAS = "INDEX_IvParam";
    // persistant settings
    public static final String WORK_DIR = "Work Directory";
    // session sessions
    public static final String PASSWORD = "Password";
    
    
    /**
     * Gets a default instance of Settings
     * @return 
     */
    public static Settings getDefault() {
        return Settings.INSTANCE;
    }
    
    private static final Settings INSTANCE = new Settings();
    
    /*
     * Private variables
     */
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
    
    /**
     * Gets the underlying preferences object.
     * @return 
     */
    public Preferences getPreferences() {
        return this.preferences;
    }
    
    /**
     * Gets a value by the key.
     * @param name The key.
     * @return The value.
     */
    public String get(String name) {
        return this.preferences.get(name, "");
    }
    
    /**
     * Set a string value by the key.
     * @param name The key.
     * @param value The value.
     */
    public void set(String name, String value) {
        this.preferences.put(name, value);
    }
    
    /**
     * Gets a session value by the key.
     * @param <T> Data type of the session value.
     * @param name The key.
     * @return The value.
     */
    public <T> T getSession(String name) {
        if (this.session.containsKey(name)) {
            return (T) this.session.get(name);
        }
        return null;
    }
    
    /**
     * Sets a session value by the key.
     * @param name The key.
     * @param value The value to store.
     */
    public void setSession(String name, Object value) {
        this.session.put(name, value);
    }
}
