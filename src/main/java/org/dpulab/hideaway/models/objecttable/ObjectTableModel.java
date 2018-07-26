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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 *
 * @author dipu
 * @param <T>
 */
public final class ObjectTableModel<T> extends AbstractTableModel {

    private final ArrayList<T> entries;
    private final TableColumnInfo[] columns;
    private boolean indexVisible = true;
    private JTable table = null;

    public ObjectTableModel(Class<T> type) {
        this.entries = new ArrayList<>();
        this.columns = TableColumnInfo.build(type);
    }

    public T getData(int row) {
        return this.entries.get(row);
    }

    public TableColumnInfo getColumn(int col) {
        if (this.indexVisible) {
            col--;
        }
        return col >= 0 ? this.columns[col] : null;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 0 && this.indexVisible) {
            return false;
        }
        return getColumn(columnIndex).isEditable();
    }

    @Override
    public int getColumnCount() {
        return this.columns.length + (this.indexVisible ? 1 : 0);
    }

    @Override
    public String getColumnName(int columnIndex) {
        String name = "#";
        if (!(columnIndex == 0 && this.indexVisible)) {
            name = getColumn(columnIndex).getName();
        }
        return String.format("<html><b>%s</b></html>", name);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0 && this.indexVisible) {
            return Integer.class;
        }
        return getColumn(columnIndex).getColumnClass();
    }

    @Override
    public String getValueAt(int rowIndex, int columnIndex) {
        Object value = rowIndex + 1;
        if (!(columnIndex == 0 && this.indexVisible)) {
            T data = this.entries.get(rowIndex);
            value = getColumn(columnIndex).extractValue(data);
        }
        return String.valueOf(value);
    }

    @Override
    public int getRowCount() {
        return this.entries.size();
    }

    @Override
    public void setValueAt(Object val, int row, int col) {
        if (col == 0 && this.indexVisible) {
            throw new UnsupportedOperationException("Index is not editable");
        }
        if (!getColumn(col).isEditable()) {
            throw new UnsupportedOperationException("Update is not permitted");
        } else {
            getColumn(col).updateValue(this.entries.get(row), val);
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

    /**
     * @return the indexVisible
     */
    public boolean isIndexVisible() {
        return indexVisible;
    }

    /**
     * @param indexVisible the indexVisible to set
     */
    public void setIndexVisible(boolean indexVisible) {
        this.indexVisible = indexVisible;
    }

    /**
     * Clears all rows
     */
    public void clear() {
        this.entries.clear();
    }

    /**
     * Adds a row to the model
     *
     * @param entry the row entry to add
     * @return
     */
    public ObjectTableModel addData(T entry) {
        this.entries.add(entry);
        return this;
    }

    /**
     * Adds batch of rows to the model
     *
     * @param entries the list of rows to add
     * @return
     */
    public ObjectTableModel setEntries(T... entries) {
        Collections.addAll(this.entries, entries);
        return this;
    }

    /**
     * Gets the selected row
     *
     * @return the row data, or null if none.
     */
    public T getSelectedRow() {
        if (this.table != null && this.table.getSelectedRow() != -1) {
            return this.getData(this.table.getSelectedRow());
        }
        return null;
    }

    public void setCellRenderer(TableColumn column, Color foreground, int fontStyle, int align) {
        column.setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setForeground(foreground);
                label.setHorizontalAlignment(align);
                label.setFont(new Font(label.getFont().getFamily(), fontStyle, label.getFont().getSize()));
                // set background
                if (isSelected) {
                    label.setBackground(table.getSelectionBackground());
                } else {
                    label.setBackground(table.getBackground());
                }
                return label;
            }
        });
    }

    public void attachTo(JTable table) {
        this.table = table;

        // set tp table model
        table.setModel(this);

        // set column constraints
        TableColumnModel columnModel = table.getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); ++i) {
            TableColumn column = columnModel.getColumn(i);
            TableColumnInfo info = this.getColumn(i);
            if (info == null) {
                continue;
            }
            if (info.getMinWidth() > 0) {
                column.setMinWidth(info.getMinWidth());
            }
            if (info.getMaxWidth() > 0 && info.getMaxWidth() > info.getMinWidth()) {
                column.setMaxWidth(info.getMaxWidth());
            }
            if (info.getPrefWidth() > 0) {
                column.setPreferredWidth(info.getPrefWidth());
            }

            if (info.getStyle() != null) {
                setCellRenderer(column, info.getColor(), info.getStyle().fontStyle(), info.getStyle().align());
            }
        }
        if (this.indexVisible) {
            TableColumn column = columnModel.getColumn(0);
            column.setMinWidth(25);
            column.setMaxWidth(50);
            column.setPreferredWidth(30);
            setCellRenderer(column, Color.GRAY, Font.BOLD, JLabel.CENTER);
        }
    }

}
