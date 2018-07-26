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
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;
import org.dpulab.hideaway.utils.GeneralUtils;

/**
 *
 * @author dipu
 */
public class TableColumnInfo implements Comparable<TableColumnInfo> {

    private final AccessibleObject field;

    private int columnIndex;
    private String columnName;
    private int minWidth;
    private int maxWidth;
    private int prefWidth;
    private boolean editable;
    private String columnStyle;

    private TableColumnInfo(AccessibleObject field) {
        this.field = field;
    }

    public static TableColumnInfo[] build(Class<?> clazz) {
        ArrayList<TableColumnInfo> columns = new ArrayList<>();
        ArrayList<AccessibleObject> list = new ArrayList<>();
        list.addAll(Arrays.asList(clazz.getFields()));
        list.addAll(Arrays.asList(clazz.getMethods()));
        list.forEach((f) -> {
            TableColumnInfo col = TableColumnInfo.build(f);
            if (col != null) {
                columns.add(col);
            }
        });
        TableColumnInfo[] result = columns.toArray(new TableColumnInfo[0]);
        Arrays.sort(result);
        return result;
    }

    public static TableColumnInfo build(AccessibleObject field) {
        // check if this is enabled as a table column
        if (field.getAnnotation(TableColumn.class) == null) {
            return null;
        }

        // create new instance
        TableColumnInfo col = new TableColumnInfo(field);

        // set column index
        TableColumn index = field.getAnnotation(TableColumn.class);
        if (index != null) {
            col.columnIndex = index.value();
        } else {
            col.columnIndex = col.hashCode();
        }

        // set style
        TableColumnStyle style = field.getAnnotation(TableColumnStyle.class);
        if (style != null) {
            col.columnStyle = String.join(";", style.value());
        }

        // set column name
        TableColumnName name = field.getAnnotation(TableColumnName.class);
        if (name != null && !StringUtils.isEmpty(name.value())) {
            col.columnName = name.value();
        } else {
            boolean ignore = !col.canUpdate() && col.getFieldName().startsWith("get");
            col.columnName = GeneralUtils.titleCase(col.getFieldName(), ignore);
        }

        // set width constraints
        TableColumnWidth width = field.getAnnotation(TableColumnWidth.class);
        if (width != null) {
            col.minWidth = width.min();
            col.maxWidth = width.min();
            col.prefWidth = width.prefer();
        } else {
            col.minWidth = 0;
            col.prefWidth = 100;
            col.maxWidth = Integer.MAX_VALUE;
        }

        // set only prefered width
        TableColumnPreferWidth preferWidth = field.getAnnotation(TableColumnPreferWidth.class);
        if (preferWidth != null) {
            col.prefWidth = preferWidth.value();
        } else if (width == null) {
            col.prefWidth = 100;
        }

        // set the editable property
        if (col.canUpdate()) {
            TableColumnEditable editable = field.getAnnotation(TableColumnEditable.class);
            if (editable != null) {
                col.editable = col.canUpdate();
            } else {
                col.editable = true;
            }
        } else {
            col.editable = false;
        }

        return col;
    }

    /**
     * Checks whether the column can be updated
     *
     * @return
     */
    public boolean canUpdate() {
        return Field.class.isInstance(field);
    }

    /**
     * @return the column index
     */
    public int getIndex() {
        return columnIndex;
    }

    /**
     * @return the columnName
     */
    public String getName() {
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
    public String getStyle() {
        return columnStyle;
    }

    /**
     * @return the fieldName
     */
    public String getFieldName() {
        if (Field.class.isInstance(field)) {
            return ((Field) field).getName();
        } else if (Method.class.isInstance(field)) {
            return ((Method) field).getName();
        }
        return null;
    }

    public Class<?> getColumnClass() {
        if (Field.class.isInstance(field)) {
            return ((Field) field).getType();
        } else if (Method.class.isInstance(field)) {
            return ((Method) field).getReturnType();
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
            if (Field.class.isInstance(field)) {
                return ((Field) field).get(instance);
            } else if (Method.class.isInstance(field)) {
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
            if (Field.class.isInstance(field)) {
                ((Field) field).set(instance, value);
                return true;
            } else if (Method.class.isInstance(field)) {
                throw new IllegalAccessException("Method does not support update operation");
            }
        } catch (IllegalAccessException | IllegalArgumentException ex) {
        }
        return false;
    }

    @Override
    public int compareTo(TableColumnInfo that) {
        if (this.columnIndex != that.columnIndex) {
            return this.columnIndex - that.columnIndex;
        }
        return this.columnName.compareTo(that.columnName);
    }

}
