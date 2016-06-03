package com.logminerplus.gui.pub;

import javax.swing.JTable;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 * Add row no LineNumber.decorate(JTable);
 */
public class LineNumber {

    public static void decorate(JTable table) {

        if (table == null)
            throw new NullPointerException("table is null.");

        TableModel model = table.getModel();
        if (!(model instanceof LineNumberTableModel))
            table.setModel(new LineNumberTableModel(model));
    }

    private static class LineNumberTableModel implements TableModel {

        private final TableModel model;

        private LineNumberTableModel(TableModel model) {

            this.model = model;
        }

        public int getRowCount() {

            return model.getRowCount();
        }

        public int getColumnCount() {

            return 1 + model.getColumnCount();
        }

        public String getColumnName(int columnIndex) {

            return columnIndex == 0 ? "Row" : model.getColumnName(columnIndex - 1);
        }

        public Class<?> getColumnClass(int columnIndex) {

            return columnIndex == 0 ? Integer.class : model.getColumnClass(columnIndex - 1);
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {

            return columnIndex == 0 ? false : model.isCellEditable(rowIndex, columnIndex - 1);
        }

        public Object getValueAt(int rowIndex, int columnIndex) {

            return columnIndex == 0 ? rowIndex + 1 : model.getValueAt(rowIndex, columnIndex - 1);
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

            if (rowIndex == 0)
                throw new UnsupportedOperationException("Cannot modify line number.");
            else
                model.setValueAt(aValue, rowIndex, columnIndex - 1);
        }

        public void addTableModelListener(TableModelListener l) {

            model.addTableModelListener(l);
        }

        public void removeTableModelListener(TableModelListener l) {

            model.removeTableModelListener(l);
        }
    }
}
