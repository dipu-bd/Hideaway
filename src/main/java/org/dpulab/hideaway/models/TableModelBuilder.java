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
import javax.swing.table.TableColumnModel;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author dipu
 */
public class TableModelBuilder {

    private final ArrayList<String> columnNames = new ArrayList<>();
    private final ArrayList<Integer> columnWidths = new ArrayList<>();
    private final ArrayList<String> columnFormat = new ArrayList<>();
    private final ArrayList<Boolean> columnEditable = new ArrayList<>();
    private final ArrayList<Object[]> tableData = new ArrayList<>();

    public TableModelBuilder() {
    }

    public TableModelBuilder addColumn(
            String name,
            int width,
            boolean editable,
            String htmlFormat
    ) {
        this.columnNames.add(name);
        this.columnWidths.add(width);
        this.columnEditable.add(editable);
        this.columnFormat.add(StringUtils.isEmpty(htmlFormat) ? "%s" : htmlFormat);
        return this;
    }

    public TableModelBuilder addData(Object... data) {
        this.tableData.add(data);
        return this;
    }

    public void build(JTable table) {
        // Set new table model
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return columnEditable.get(columnIndex);
            }
        };
        table.setModel(model);

        // add columns
        this.columnNames.forEach((name) -> {
            model.addColumn(name);
        });

        // set column width
        TableColumnModel columns = table.getColumnModel();
        for (int i = 0; i < columns.getColumnCount(); ++i) {
            if (i < this.columnWidths.size()) {
                columns.getColumn(i).setPreferredWidth(this.columnWidths.get(i));
            }
        }

        this.tableData.forEach((item) -> {
            for (int i = 0; i < item.length; ++i) {
                if (i < columnFormat.size()) {
                    String fmt = "<html>" + columnFormat.get(i) + "</html>";
                    item[i] = String.format(fmt, item[i]);
                }
            }
            model.addRow(item);
        });
    }
}
