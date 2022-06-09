package lphystudio.core.swing;

import javax.swing.table.AbstractTableModel;
import java.util.List;

/**
 * Give <code>String[] columnNames and List<T> dataList</code>.
 * Overwrite getValueAt(int rowIndex, int columnIndex) to parse row class.
 * @author Walter Xie
 */
public class EasyTableModel extends AbstractTableModel {

    protected final String[] columnNames;
    protected final List<?> dataList;

    /**
     * Set dataList
     * @param columnNames  column names
     * @param dataList     list of data to display in the table,
     *                     the type class can be the data structure of a row.
     */
    public EasyTableModel(String[] columnNames, List<?> dataList) {
        this.columnNames = columnNames;
        this.dataList = dataList;
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public int getRowCount() {
        return dataList.size();
    }

    /**
     * Must overwrite this method to parse row class.
     * Get data from dataList, for example,
     * <code>Model model = (Model) dataList.get(row);</code>.
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return null; // overwrite this
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append(getColumnName(0));
        for (int j = 1; j < getColumnCount(); j++) {
            buffer.append("\t");
            buffer.append(getColumnName(j));
        }
        buffer.append("\n");

        for (int i = 0; i < getRowCount(); i++) {
            buffer.append(getValueAt(i, 0));
            for (int j = 1; j < getColumnCount(); j++) {
                buffer.append("\t");
                buffer.append(getValueAt(i, j));
            }
            buffer.append("\n");
        }

        return buffer.toString();
    }
}
