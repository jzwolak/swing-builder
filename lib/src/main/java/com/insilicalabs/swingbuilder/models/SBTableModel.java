package com.insilicalabs.swingbuilder.models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.table.AbstractTableModel;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Created by jzwolak on 3/28/16.
 */
public class SBTableModel extends AbstractTableModel {

    private static final Logger LOG = LoggerFactory.getLogger(SBTableModel.class);

    private List data;
    private List<String> columnNames;
    private Function<Object, Object[]> rowFn;

    public SBTableModel() {
        this.data = Collections.emptyList();
        this.columnNames = Collections.emptyList();
        this.rowFn = (o) -> new Object[]{o};
    }

    public void setColumnNames(List<String> columnNames) {
        this.columnNames = columnNames;
        fireTableStructureChanged();
    }

    public void setData(List data) {
        this.data = data;
        // This must be structure changed because the number of rows and columns in data may be different now.
        fireTableStructureChanged();
    }

    /*
     * I added this getter years later. Why wasn't it here? Did I leave it out on purpose? To avoid callers from
     * modifying the data?
     */
    public List getData() {
        return data;
    }

    public void setRowFn(Function<Object, Object[]> rowFn) {
        this.rowFn = rowFn;
        fireTableStructureChanged();
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        if (data.isEmpty()) return 0;
        return rowFn.apply(data.get(0)).length;
    }

    @Override
    public String getColumnName(int column) {
        try {
            return columnNames.get(column);
        } catch (IndexOutOfBoundsException ex) {
            return "";
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return rowFn.apply(data.get(rowIndex))[columnIndex];
    }

}
