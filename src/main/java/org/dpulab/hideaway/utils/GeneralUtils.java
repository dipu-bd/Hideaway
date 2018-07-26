/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dpulab.hideaway.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author dipu
 */
public class GeneralUtils {

    public static String formatDate(long unixTime) {
        return unixTime == 0 ? "" : GeneralUtils.formatDate(new Date(unixTime));
    }

    public static String formatDate(Date date) {
        return GeneralUtils.formatDate(date, "HH:mm:ss dd/MM/yy");
    }

    public static String formatDate(Date date, String formatter) {
        return date == null ? "" : new SimpleDateFormat(formatter).format(date);
    }

    public static String formatFileSize(long size) {
        return GeneralUtils.formatFileSize((double) size);
    }

    public static String formatFileSize(double size) {
        final String[] SIZE_SUFFIX = {"B", "KB", "MB", "GB", "TB", "PB"};
        int p = 0;
        while (size > 1024) {
            size /= 1024;
            p++;
        }
        return String.format("%.2f %s", size, SIZE_SUFFIX[p]);
    }

    /**
     * Returns the character type. <br>
     * <br>
     * Digit = 2 <br>
     * Lower case alphabet = 0 <br>
     * Uppercase case alphabet = 1 <br>
     * All else = -1.
     *
     * @param ch
     * @return
     */
    private static int getCharType(char ch) {
        if (Character.isLowerCase(ch)) {
            return 0;
        } else if (Character.isUpperCase(ch)) {
            return 1;
        } else if (Character.isDigit(ch)) {
            return 2;
        }
        return -1;
    }

    /**
     * Converts any given string in camel or snake case to title case.
     * <br>
     * It uses the method getCharType and ignore any character that falls in
     * negative character type category. It separates two alphabets of not-equal
     * cases with a space. It accepts numbers and append it to the currently
     * running group, and puts a space at the end.
     * <br>
     * If the result is empty after the operations, original string is returned.
     *
     * @param text the text to be converted.
     * @return a title cased string
     */
    public static String titleCase(String text) {
        if (StringUtils.isEmpty(text)) {
            return text;
        }
        StringBuilder sb = new StringBuilder();
        char[] str = text.toCharArray();
        boolean capRepeated = false;
        for (int i = 0, prev = -1, next; i < str.length; ++i, prev = next) {
            next = getCharType(str[i]);
            // trace consecutive capital cases
            if (prev == 1 && next == 1) {
                capRepeated = true;
            } else if (prev != 1 || next != 0) {
                capRepeated = false;
            }
            // next is ignorable
            if (next == -1) {
                continue; // does not append anything
            }
            // prev and next are of same type
            if (prev == next) {
                sb.append(str[i]);
                continue; // take the current one
            }
            // next is not an alphabet
            if (next == 2) {
                sb.append(str[i]);
                continue; // take the current one
            }
            // next is an alphabet, prev was not +
            // next is uppercase and prev was lowercase
            if (prev == -1 || prev == 2 || prev == 0) {
                if (sb.length() != 0) {
                    sb.append(' ');
                }
                sb.append(Character.toUpperCase(str[i]));
                continue;
            }
            // next is lowercase and prev was uppercase
            if (prev == 1) {
                if (capRepeated) {
                    sb.insert(sb.length() - 1, ' ');
                    capRepeated = false;
                }
                sb.append(str[i]);
            }
        }
        String output = sb.toString().trim();
        output = (output.length() == 0) ? text : output;
        // Capitalize all words 
        String[] result = output.split(" ");
        for (int i = 0; i < result.length; ++i) {
            result[i] = result[i].charAt(0) + result[i].substring(1).toLowerCase();
        }
        output = String.join(" ", result);
        return output;
    }

    /**
     * Ignores the first word after converting the text to titleCase.
     *
     * @param text
     * @param ignoreFirstWord
     * @return
     */
    public static String titleCase(String text, boolean ignoreFirstWord) {
        text = titleCase(text);
        if (ignoreFirstWord && StringUtils.containsAny(text, ' ')) {
            return text.substring(text.indexOf(' ') + 1);
        }
        return text;
    }

}
