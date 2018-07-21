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

import java.util.ArrayList;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 *
 * @author dipu
 */
public class TableModelBuilder {

    private final ArrayList<TableColumnInfo> columns = new ArrayList<>();
    private final ArrayList<Object[]> tableData = new ArrayList<>();

    public TableModelBuilder() {
    }

    public void build(JTable table) {
        // Set new table model
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return getColumn(columnIndex).isEditable();
            }
        };
        table.setModel(model);

        // add columns
        this.columns.forEach((info) -> {
            model.addColumn(info.getColumnName());
        });

        this.tableData.forEach((item) -> {
            for (int i = 0; i < item.length && i < this.columns.size(); ++i) {
                String fmt = "<html>" + this.getColumn(i).getFormat() + "</html>";
                item[i] = String.format(fmt, item[i]);
            }
            model.addRow(item);
        });

        // set column size
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

        /*
        // listen to resize event to autoreize the last column
        final TableColumn last = columns.getColumn(columns.getColumnCount() - 1);
        final int lastMinSize = this.columnWidths.get(columns.getColumnCount() - 1);
        final int effectiveTotalSize = totalSize - lastMinSize;
        table.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                System.out.println("resized: " + e.getComponent());
                if (table.getWidth() > effectiveTotalSize) {
                    last.setPreferredWidth(table.getWidth() - effectiveTotalSize + lastMinSize);
                }
            }
        });*/
    }

    public TableColumnInfo getColumn(int index) {
        return this.columns.get(index);
    }

    public TableModelBuilder addData(Object... data) {
        this.tableData.add(data);
        return this;
    }

    public TableModelBuilder addColumn(TableColumnInfo info) {
        this.columns.add(info);
        return this;
    }

    public TableModelBuilder addColumn(String name) {
        return addColumn(new TableColumnInfo(name));
    }

    public TableModelBuilder addColumn(String name, String format) {
        return addColumn(new TableColumnInfo(name, format));
    }

    public TableModelBuilder addColumn(String name, int prefWidth) {
        TableColumnInfo info = new TableColumnInfo(name);
        info.setPreferedWidth(prefWidth);
        return addColumn(info);
    }

    public TableModelBuilder addColumn(String name, String format, int prefWidth) {
        TableColumnInfo info = new TableColumnInfo(name, format);
        info.setPreferedWidth(prefWidth);
        return addColumn(info);
    }

    public TableModelBuilder addColumn(String name, int minWidth, int maxWidth) {
        TableColumnInfo info = new TableColumnInfo(name);
        info.setMaxWidth(maxWidth);
        info.setMinWidth(minWidth);
        return addColumn(info);
    }

    public TableModelBuilder addColumn(String name, String format, int minWidth, int maxWidth) {
        TableColumnInfo info = new TableColumnInfo(name, format);
        info.setMaxWidth(maxWidth);
        info.setMinWidth(minWidth);
        return addColumn(info);
    }

    public TableModelBuilder addColumn(String name, String format, int prefWidth, boolean editable) {
        TableColumnInfo info = new TableColumnInfo(name, format);
        info.setPreferedWidth(prefWidth);
        info.setEditable(editable);
        return addColumn(info);
    }

    public TableModelBuilder addColumn(String name, int prefWidth, boolean editable) {
        TableColumnInfo info = new TableColumnInfo(name);
        info.setPreferedWidth(prefWidth);
        info.setEditable(editable);
        return addColumn(info);
    }

    public TableModelBuilder addColumn(String name, String format, int minWidth, int maxWidth, boolean editable) {
        TableColumnInfo info = new TableColumnInfo(name, format);
        info.setMaxWidth(maxWidth);
        info.setMinWidth(minWidth);
        info.setEditable(editable);
        return addColumn(info);
    }

    public TableModelBuilder addColumn(String name, int minWidth, int maxWidth, boolean editable) {
        TableColumnInfo info = new TableColumnInfo(name);
        info.setMaxWidth(maxWidth);
        info.setMinWidth(minWidth);
        info.setEditable(editable);
        return addColumn(info);
    }

}
