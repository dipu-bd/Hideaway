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
package org.dpulab.hideaway.models.objecttable;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.dpulab.hideaway.utils.GeneralUtils;

/**
 *
 * @author dipu
 */
public class TableColumn {

    private final AccessibleObject field;

    private String columnName;
    private int minWidth;
    private int maxWidth;
    private int prefWidth;
    private boolean editable;
    private String columnStyle;

    private TableColumn(AccessibleObject field) {
        this.field = field;
    }

    public static TableColumn build(AccessibleObject field) {
        TableColumn col = new TableColumn(field);

        TableColumnInfo info = field.getAnnotation(TableColumnInfo.class);
        if (info != null) {
            col.columnName = info.name();
            col.minWidth = info.min();
            col.maxWidth = info.max();
            col.prefWidth = info.prefer();
            col.editable = info.editable();
        } else {
            col.minWidth = 0;
            col.maxWidth = Integer.MAX_VALUE;
            col.prefWidth = 80;
            col.editable = false;
            col.columnName = GeneralUtils.titleCase(col.getFieldName(), true);
        }

        TableColumnStyle style = field.getAnnotation(TableColumnStyle.class);
        col.columnStyle = String.join(";", style.value());

        return col;
    }

    /**
     * @return the columnName
     */
    public String getColumnName() {
        return columnName;
    }

    /**
     * @return the minWidth
     */
    public int getMinWidth() {
        return minWidth;
    }

    /**
     * @return the maxWidth
     */
    public int getMaxWidth() {
        return maxWidth;
    }

    /**
     * @return the prefWidth
     */
    public int getPrefWidth() {
        return prefWidth;
    }

    /**
     * @return the editable
     */
    public boolean isEditable() {
        return editable;
    }

    /**
     * @return the columnStyle
     */
    public String getColumnStyle() {
        return columnStyle;
    }

    /**
     * @return the fieldName
     */
    public String getFieldName() {
        if (field.getClass() == Field.class) {
            return ((Field) field).getName();
        } else if (field.getClass() == Method.class) {
            return ((Method) field).getName();
        }
        return null;
    }

    /**
     * Extracts the column value from the instance.
     *
     * @param instance the instance to get
     * @return the value that was get
     */
    public Object extractValue(Object instance) {
        try {
            if (field.getClass() == Field.class) {
                return ((Field) field).get(instance);
            } else if (field.getClass() == Method.class) {
                return ((Method) field).invoke(instance);
            }
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
        }
        return null;
    }

    /**
     * Put the value in the instance.
     *
     * @param instance the instance
     * @param value the value to be set
     * @return True if success; False otherwise
     */
    public boolean updateValue(Object instance, Object value) {
        try {
            if (field.getClass() == Field.class) {
                ((Field) field).set(instance, value);
                return true;
            } else if (field.getClass() == Method.class) {
                throw new IllegalAccessException("Method does not support update operation");
            }
        } catch (IllegalAccessException | IllegalArgumentException ex) {
        }
        return false;
    }

}
