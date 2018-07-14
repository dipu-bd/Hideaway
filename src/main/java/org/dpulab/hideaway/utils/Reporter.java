/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dpulab.hideaway.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dipu
 */
public class Reporter {

    public static final Class DEFAULT_SOURCE = Reporter.class;
    public static final Level DEFAULT_LEVEL = Level.INFO;
    public static final String DEFAULT_STATUS = null;
    public static final int DEFAULT_PROGRESS = 0;
    public static final Exception DEFAULT_ERROR = null;

    public static void put(Class source, Level level, String status, int progress, Exception ex) {
        status = String.format("%2d%%: %s", progress, status);
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

}
