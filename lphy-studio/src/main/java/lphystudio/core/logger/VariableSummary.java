package lphystudio.core.logger;

import lphy.core.logger.LoggableImpl;
import lphy.core.logger.RandomNumberFormatter;
import lphy.core.logger.RandomValueFormatter;
import lphy.core.model.Value;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class VariableSummary extends JTable implements RandomValueFormatter {

//    boolean logVariables;
//    boolean logStatistics;

    List<ValueRow> valueRows = new ArrayList<>();

    final RandomNumberFormatter randomNumberLogger;

    AbstractTableModel tableModel;

    public VariableSummary() { // boolean logStatistics, boolean logVariables

//        this.logStatistics = logStatistics;
//        this.logVariables = logVariables;
        randomNumberLogger = new RandomNumberFormatter();//logVariables, logStatistics

        tableModel = new AbstractTableModel() {
            @Override
            public int getRowCount() {
                return ((valueRows != null) ? valueRows.size() : 0);
            }

            @Override
            public int getColumnCount() {
                return 6;
            }

            @Override
            public String getColumnName(int columnIndex) {
                switch (columnIndex) {
                    case 0:
                        return "Variable";
                    case 1:
                        return "Mean";
                    case 2:
                        return "Std. dev.";
                    case 3:
                        return "Std. err.";
                    case 4:
                        return "Min";
                    case 5:
                        return "Max";
                }
                return "";
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0:
                        return String.class;
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                        return Double.class;
                }
                return Object.class;
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {

                ValueRow valueRow = null;
                if (valueRows != null) {
                    valueRow = valueRows.get(rowIndex);
                }

                switch (columnIndex) {
                    case 0:
                        if (valueRow != null) {
                            return valueRow.title;
                        } else return "";
                    case 1:
                        if (valueRow != null) {
                            return valueRow.summary.mean[valueRow.row];
                        } else {
                            return Double.NaN;
                        }
                    case 2:
                        if (valueRow != null) {
                            return valueRow.summary.stdev[valueRow.row];
                        } else {
                            return Double.NaN;
                        }
                    case 3:
                        if (valueRow != null) {
                            return valueRow.summary.stderr[valueRow.row];
                        } else {
                            return Double.NaN;
                        }
                    case 4:
                        if (valueRow != null) {
                            return valueRow.summary.min[valueRow.row];
                        } else {
                            return Double.NaN;
                        }
                    case 5:
                        if (valueRow != null) {
                            return valueRow.summary.max[valueRow.row];
                        } else {
                            return Double.NaN;
                        }
                }
                return "";
            }
        };

        setModel(tableModel);
    }

    @Override
    public void setSelectedItems(List<Value<?>> randomValues) {

    }

    @Override
    public List<?> getSelectedItems() {
        return null;
    }

    @Override
    public String getHeaderFromValues() {
        //TODO
        return "";
    }

    @Override
    public String getRowFromValues(int rowIndex) {

        if (rowIndex == 0) valueRows.clear();

        randomNumberLogger.getRowFromValues(rowIndex);
        return "";
    }

    @Override
    public String getFooterFromValues() {

        for (Value value : randomNumberLogger.firstValues) {
            if (randomNumberLogger.isLogged(value)) {
                String id = value.getId();
                if (id == null) {
                    throw new RuntimeException("Not expecting null id in variable summary!");
                }
                Summary summary = new Summary(randomNumberLogger.variableValues.get(id));
                if (summary.isLengthSummary) {
                    valueRows.add(new ValueRow("length(" + id + ")", summary, 0));
                } else {
                    String[] titles = LoggableImpl.getLoggable(value.value().getClass()).getLogTitles(value);
                    for (int i = 0; i < summary.getRowCount(); i++) {
                        valueRows.add(new ValueRow(titles[i], summary, i));
                    }
                }
            }
        }

        tableModel.fireTableDataChanged();
        return "";
    }

    public String getFormatterDescription() {
        return getFormatterName() + " writes the statistical summary of random variables " +
                "from simulations into a table format for GUI.";
    }
}
