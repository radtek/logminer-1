package com.logminerplus.gui.tablemodel;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.logminerplus.bean.Log;

public class LogTableModel extends AbstractTableModel {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1671171731887042516L;

    private List<String> columnNames = new ArrayList<String>();

    private List<Log> rowData = new ArrayList<Log>();

    @Override
    public int getRowCount() {
        // TODO Auto-generated method stub
        return rowData.size();
    }

    @Override
    public int getColumnCount() {
        // TODO Auto-generated method stub
        return 3;
    }

    @Override
    public String getColumnName(int column) {
        // TODO Auto-generated method stub
        return columnNames.get(column);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Log log = rowData.get(rowIndex);
        if (columnIndex == 0)
            return log.getTs();
        else if (columnIndex == 1)
            return log.getFilename();
        return log.getAbsolutePath();
    }

    public void setColumnNames(List<String> columnNames) {
        this.columnNames = columnNames;
    }

    public void setData(List<Log> rowData) {
        if (rowData == null)
            throw new IllegalArgumentException("data is null");
        this.rowData = rowData;
        fireTableDataChanged();
    }

}
