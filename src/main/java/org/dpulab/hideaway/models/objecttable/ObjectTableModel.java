/*
 * Copyright (C) 2018 Sudipto Chandra
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

import org.dpulab.hideaway.models.objecttable.TableColumnInfo;
import java.util.ArrayList;
import javax.swing.JTable;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 *
 * @author dipu
 */
public final class ObjectTableModel<T> extends AbstractTableModel {

    private final ArrayList<T> entries;
    private final ArrayList<TableColumnInfo> columns;

    public ObjectTableModel() {
        this.columns = new ArrayList<>();
        this.entries = new ArrayList<>();
    }

    private String getValue(T entry, TableColumnInfo info) {
        try {
            Object result = entry.getClass().getMethod(info.getGetter()).invoke(entry);
            return String.format(info.getFormat(), result);
        } catch (ReflectiveOperationException | RuntimeException ex) {
            return "";
        }
    }

    public void attachTo(JTable table) {
        // set tp table model
        table.setModel(this);
        // set column constraints
        TableColumnModel columnModel = table.getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount() && i < this.columns.size(); ++i) {
            TableColumn column = columnModel.getColumn(i);
            TableColumnInfo info = this.getColumn(i);
            if (info.getMinWidth() > 0) {
                column.setMinWidth(info.getMinWidth());
            }
            if (info.getMaxWidth() > 0 && info.getMaxWidth() > info.getMinWidth()) {
                column.setMaxWidth(info.getMaxWidth());
            }
            if (info.getPreferedWidth() > 0) {
                column.setPreferredWidth(info.getPreferedWidth());
            }
        }
    }

    public ObjectTableModel addData(T entry) {
        this.entries.add(entry);
        return this;
    }

    public T getData(int row) {
        return this.entries.get(row);
    }

    public ObjectTableModel addColumn(TableColumnInfo info) {
        this.columns.add(info);
        return this;
    }

    public TableColumnInfo getColumn(int columnIndex) {
        return this.columns.get(columnIndex);
    }

    public void clear() {
        this.entries.clear();
    }

    public void clearAll() {
        this.entries.clear();
        this.columns.clear();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return this.columns.get(columnIndex).isEditable();
    }

    @Override
    public int getColumnCount() {
        return this.columns.size();
    }

    @Override
    public String getColumnName(int columnIndex) {
        return this.columns.get(columnIndex).getName();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public String getValueAt(int rowIndex, int columnIndex) {
        T val = this.entries.get(rowIndex);
        TableColumnInfo info = this.columns.get(columnIndex);
        return getValue(val, info);
    }

    @Override
    public int getRowCount() {
        return this.entries.size();
    }

    @Override
    public void setValueAt(Object val, int row, int col) {
        throw new UnsupportedOperationException("Update is not permitted");
    }

    @Override
    public void addTableModelListener(TableModelListener l) {
        super.addTableModelListener(l);
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
        super.removeTableModelListener(l);
    }
}
