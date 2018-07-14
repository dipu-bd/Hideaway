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

    public static void put(Class source, Level level, String status, int progress, Exception ex) {
        if (progress > 0) {
            status = String.format("%2d%%: %s", progress, status);
        }
        Logger.getLogger(source.getName()).log(level, status, ex);
    }

    public static void put(Level level, String status, int progress) {
        Reporter.put(DEFAULT_SOURCE, level, status, progress, DEFAULT_ERROR);
    }

    public static void put(Level level, String status) {
        Reporter.put(level, status, DEFAULT_PROGRESS);
    }

    public static void put(String status, int progress) {
        Reporter.put(DEFAULT_LEVEL, status, progress);
    }

    public static void put(String status) {
        Reporter.put(status, DEFAULT_PROGRESS);
    }

    public static void put(int progress) {
        Reporter.put(DEFAULT_STATUS, progress);
    }

    public static void put(Class source, Level level, Exception ex) {
        Reporter.put(source, level, DEFAULT_STATUS, DEFAULT_PROGRESS, ex);
    }

    public static void put(Class source, String status, Exception ex) {
        Reporter.put(source, DEFAULT_LEVEL, status, DEFAULT_PROGRESS, ex);
    }

    public static void put(Class source, Exception ex) {
        Reporter.put(source, Level.SEVERE, ex);
    }

    public static void put(Exception ex) {
        Reporter.put(DEFAULT_SOURCE, ex);
    }

    public static void format(Class source, Level level, String fmt, Exception ex, Object... args) {
        String status = String.format(fmt, args);
        Reporter.put(source, level, status, DEFAULT_PROGRESS, ex);
    }

    public static void format(Class source, String fmt, Exception ex, Object... args) {
        Reporter.format(source, DEFAULT_LEVEL, fmt, ex, args);
    }

    public static void format(String fmt, Exception ex, Object... args) {
        Reporter.format(DEFAULT_LEVEL, fmt, ex, args);
    }

    public static void format(Level level, String fmt, Exception ex, Object... args) {
        Reporter.format(DEFAULT_SOURCE, level, fmt, ex, args);
    }

    public static void format(Class source, Level level, String fmt, Object... args) {
        Reporter.format(DEFAULT_SOURCE, level, fmt, DEFAULT_ERROR, args);
    }

    public static void format(Class source, String fmt, Object... args) {
        Reporter.format(source, DEFAULT_LEVEL, fmt, DEFAULT_ERROR, args);
    }

    public static void format(Level level, String fmt, Object... args) {
        Reporter.format(DEFAULT_SOURCE, level, fmt, args);
    }

    public static void format(String fmt, Object... args) {
        Reporter.format(DEFAULT_LEVEL, fmt, args);
    }

    public static void dialog(Component parent, Level level, String title, String status, Object ... args) {
        int type;
        String defTitle;
        if (level == Level.SEVERE
                || level == Level.FINE
                || level == Level.FINER
                || level == Level.FINEST) {
            type = JOptionPane.ERROR_MESSAGE;
            defTitle = "Hideaway :: Error";
        } else if (level == Level.INFO) {
            type = JOptionPane.INFORMATION_MESSAGE;
            defTitle = "Hideaway :: Information";
        } else if (level == Level.WARNING) {
            type = JOptionPane.WARNING_MESSAGE;
            defTitle = "Hideaway :: Warning!";
        } else {
            type = JOptionPane.PLAIN_MESSAGE;
            defTitle = "Hideaway :: Message";
        }
        status = String.format(status, args);
        if (title == null || title.length() == 0) title = defTitle;
        JOptionPane.showMessageDialog(parent, status, title, type);
    }
    
    public static void dialog(Level level, String status, Object ... args) {
        Reporter.dialog(DEFAULT_PARENT, level, null, status, args);
    }
    
    public static void dialog(String status, Object ... args) {
        Reporter.dialog(DEFAULT_LEVEL, status, args);
    }
}
