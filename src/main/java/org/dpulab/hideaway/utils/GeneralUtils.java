/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dpulab.hideaway.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author dipu
 */
public class GeneralUtils {

    public static String formatDate(long unixTime) {
        return GeneralUtils.formatDate(new Date(unixTime));
    }

    public static String formatDate(Date date) {
        return GeneralUtils.formatDate(date, "HH:mm:ss dd/MM/yy");
    }

    public static String formatDate(Date date, String formatter) {
        return new SimpleDateFormat(formatter).format(date);
    }

}
