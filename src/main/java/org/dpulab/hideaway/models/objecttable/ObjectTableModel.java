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

import java.util.ArrayList;
import javax.swing.JTable;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.dpulab.hideaway.models.IndexEntryModel;

/**
 *
 * @author dipu
 * @param <T>
 */
public final class ObjectTableModel<T> extends AbstractTableModel {

    private final ArrayList<T> entries;
    private final TableColumnInfo[] columns;

    public ObjectTableModel() {
        this.entries = new ArrayList<>();
        this.columns = TableColumnInfo.build(IndexEntryModel.class);
    }

    public void attachTo(JTable table) {
        // set tp table model
        table.setModel(this);

        // set column constraints
        TableColumnModel columnModel = table.getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount() && i < this.columns.length; ++i) {
            TableColumn column = columnModel.getColumn(i);
            TableColumnInfo info = this.getColumn(i);
            if (info.getMinWidth() > 0) {
                column.setMinWidth(info.getMinWidth());
            }
            if (info.getMaxWidth() > 0 && info.getMaxWidth() > info.getMinWidth()) {
                column.setMaxWidth(info.getMaxWidth());
            }
            if (info.getPrefWidth() > 0) {
                column.setPreferredWidth(info.getPrefWidth());
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

    public TableColumnInfo getColumn(int col) {
        return this.columns[col];
    }

    public void clear() {
        this.entries.clear();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return this.columns[columnIndex].isEditable();
    }

    @Override
    public int getColumnCount() {
        return this.columns.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return this.columns[columnIndex].getColumnName();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public String getValueAt(int rowIndex, int columnIndex) {
        T data = this.entries.get(rowIndex);
        Object value = this.columns[columnIndex].extractValue(data);
        String style = this.columns[columnIndex].getColumnStyle();
        String item = String.format(
                "<html><span style=\"%s\">%s</span></html>",
                style == null ? "" : style,
                value == null ? "" : value);
        return item;
    }

    @Override
    public int getRowCount() {
        return this.entries.size();
    }

    @Override
    public void setValueAt(Object val, int row, int col) {
        if (!this.columns[col].isEditable()) {
            throw new UnsupportedOperationException("Update is not permitted");
        } else {
            this.columns[col].updateValue(this.entries.get(row), val);
        }
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
