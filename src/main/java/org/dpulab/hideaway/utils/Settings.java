/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dpulab.hideaway.utils;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.dpulab.hideaway.view.Login;

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
            Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Logger.getLogger(Login.class.getName()).log(Level.INFO, "Preferences @ {0}", this.preferences.absolutePath());
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
    
    public Object getSession(String name) {
        if (this.session.containsKey(name)) {
            return this.session.get(name);
        }
        return null;
    }
    
    public void setSession(String name, Object value) {
        this.session.put(name, value);
    }
}
