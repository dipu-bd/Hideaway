/*
 * Copyright (C) 2018 dipu
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.dpulab.hideaway.models;

/**
 *
 * @author dipu
 */
public class TableColumnInfo {

    private String getter;
    private String name = "Column";
    private String format = "%s";
    private int minWidth = 0;
    private int maxWidth = (1 << 31) - 1;
    private int preferedWidth = 80;
    private boolean editable = false;

    //<editor-fold defaultstate="collapsed" desc="  Constructors  ">
    public TableColumnInfo(String getter) {
        this.getter = getter;
    }

    public TableColumnInfo(String getter, String name) {
        this(getter);
        this.name = name;
    }

    public TableColumnInfo(String getter, String name, String format) {
        this(getter, name);
        this.format = format;
    }

    public TableColumnInfo(String getter, String name, boolean editable) {
        this(getter, name);
        this.editable = editable;
    }

    public TableColumnInfo(String getter, String name, int preferedWidth) {
        this(getter, name);
        this.preferedWidth = preferedWidth;
    }

    public TableColumnInfo(String getter, String name, String format, boolean editable) {
        this(getter, name, format);
        this.editable = editable;
    }

    public TableColumnInfo(String getter, String name, int minWidth, int maxWidth) {
        this(getter, name);
        this.minWidth = minWidth;
        this.maxWidth = maxWidth;
    }

    public TableColumnInfo(String getter, String name, int preferedWidth, boolean editable) {
        this(getter, name, preferedWidth);
        this.editable = editable;
    }

    public TableColumnInfo(String getter, String name, int minWidth, int maxWidth, boolean editable) {
        this(getter, name, maxWidth, minWidth);
        this.editable = editable;
    }
    //</editor-fold>

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the getter
     */
    public String getGetter() {
        return getter;
    }

    /**
     * @param getter the getter to set
     */
    public void setGetter(String getter) {
        this.getter = getter;
    }

    /**
     * @return the format
     */
    public String getFormat() {
        return format;
    }

    /**
     * @param format the format to set
     */
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * @return the minWidth
     */
    public int getMinWidth() {
        return minWidth;
    }

    /**
     * @param minWidth the minWidth to set
     */
    public void setMinWidth(int minWidth) {
        this.minWidth = minWidth;
    }

    /**
     * @return the maxWidth
     */
    public int getMaxWidth() {
        return maxWidth;
    }

    /**
     * @param maxWidth the maxWidth to set
     */
    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    /**
     * @return the preferedWidth
     */
    public int getPreferedWidth() {
        return preferedWidth;
    }

    /**
     * @param preferedWidth the preferedWidth to set
     */
    public void setPreferedWidth(int preferedWidth) {
        this.preferedWidth = preferedWidth;
    }

    /**
     * @return the editable
     */
    public boolean isEditable() {
        return editable;
    }

    /**
     * @param editable the editable to set
     */
    public void setEditable(boolean editable) {
        this.editable = editable;
    }

}
