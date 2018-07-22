/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dpulab.hideaway.utils;

import java.awt.Component;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author dipu
 */
public class Reporter {

    public static Class DEFAULT_SOURCE = Reporter.class;
    public static Level DEFAULT_LEVEL = Level.INFO;
    public static String DEFAULT_STATUS = null;
    public static int DEFAULT_PROGRESS = 0;
    public static Exception DEFAULT_ERROR = null;
    public static Component DEFAULT_PARENT = null;
        
    //<editor-fold defaultstate="collapsed" desc=" Add new log entry utility ">    
    // C L S E
    public static void put(Class source, Level level, String status, Exception ex) {
        Logger.getLogger(source.getName()).log(level, status, ex);
    }
    // C L S
    public static void put(Class source, Level level, String status) {
        Reporter.put(source, level, status, DEFAULT_ERROR);
    }
    // C L E
    public static void put(Class source, Level level, Exception ex) {
        Reporter.put(source, level, DEFAULT_STATUS, ex);
    }
    // C S E
    public static void put(Class source, String status, Exception ex) {
        Reporter.put(source, DEFAULT_LEVEL, status, ex);
    }
    // L S E
    public static void put(Level level, String status, Exception ex) {
        Reporter.put(DEFAULT_SOURCE, level, status, ex);
    }
    // C S
    public static void put(Class source, String status) {
        Reporter.put(source, DEFAULT_LEVEL, status, DEFAULT_ERROR);
    }
    // C E
    public static void put(Class source, Exception ex) {
        Reporter.put(source, DEFAULT_LEVEL, DEFAULT_STATUS, ex);
    }
    // L S
    public static void put(Level level, String status) {
        Reporter.put(DEFAULT_SOURCE, level, status, DEFAULT_ERROR);
    }
    // L E
    public static void put(Level level, Exception ex) {
        Reporter.put(DEFAULT_SOURCE, level, DEFAULT_STATUS, ex);
    }
    // S E
    public static void put(String status, Exception ex) {
        Reporter.put(DEFAULT_SOURCE, DEFAULT_LEVEL, status, ex);
    }
    // S
    public static void put(String status) {
        Reporter.put(DEFAULT_SOURCE, DEFAULT_LEVEL, DEFAULT_STATUS, DEFAULT_ERROR);
    }    
    //E
    public static void put(Exception ex) {
        Reporter.put(DEFAULT_SOURCE, DEFAULT_LEVEL, DEFAULT_STATUS, ex);
    }
    // -
    public static void put() {
        Reporter.put(DEFAULT_SOURCE, DEFAULT_LEVEL, DEFAULT_STATUS, DEFAULT_ERROR);
    }
    //</editor-fold>
        
    //<editor-fold defaultstate="collapsed" desc=" Format entry before logging ">
    // C L F *A
    public static void format(Class source, Level level, String fmt, Object... args) {
        String status = String.format(fmt, args);
        Reporter.put(source, level, status);
    }
    // C F *A
    public static void format(Class source, String fmt, Object... args) {
        Reporter.format(source, DEFAULT_LEVEL, fmt, args);
    }
    // L F *A
    public static void format(Level level, String fmt, Object... args) {
        Reporter.format(DEFAULT_SOURCE, level, fmt, args);
    }
    // F *A
    public static void format(String fmt,  Object... args) {
        Reporter.format(DEFAULT_SOURCE, DEFAULT_LEVEL, fmt, args);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc=" Display dialog modal utility">
    // C L S *A
    public static void dialog(Component parent, Level level, String status, Object ... args) {
        int type;
        String title;
        if (level == Level.SEVERE
                || level == Level.FINE
                || level == Level.FINER
                || level == Level.FINEST) {
            type = JOptionPane.ERROR_MESSAGE;
            title = "Hideaway :: Error";
        } else if (level == Level.INFO) {
            type = JOptionPane.INFORMATION_MESSAGE;
            title = "Hideaway :: Information";
        } else if (level == Level.WARNING) {
            type = JOptionPane.WARNING_MESSAGE;
            title = "Hideaway :: Warning!";
        } else {
            type = JOptionPane.PLAIN_MESSAGE;
            title = "Hideaway :: Message";
        }
        status = String.format(status, args);
        JOptionPane.showMessageDialog(parent, status, title, type);
    }
    // C S *A
    public static void dialog(Component parent, String status, Object ... args) {
        Reporter.dialog(parent, DEFAULT_LEVEL, status, args);
    }
    // L S *A
    public static void dialog(Level level, String status, Object ... args) {
        Reporter.dialog(DEFAULT_PARENT, level, status, args);
    }
    // S *A
    public static void dialog(String status, Object ... args) {
        Reporter.dialog(DEFAULT_PARENT, DEFAULT_LEVEL, status, args);
    }
    //</editor-fold>
}
