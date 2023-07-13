package lphystudio.core.logger;

import lphy.core.logger.RandomNumberLoggerListener;
import lphy.core.model.Value;
import lphy.core.simulator.SimulatorListener;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.SynchronizedDescriptiveStatistics;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class VariableSummaryLog extends JTable implements SimulatorListener {

//    boolean logVariables;
//    boolean logStatistics;

    List<ValueRow> valueRows = new ArrayList<>();

    final RandomNumberLoggerListener randomNumberLogger;

    AbstractTableModel tableModel;

    public VariableSummaryLog() { // boolean logStatistics, boolean logVariables

//        this.logStatistics = logStatistics;
//        this.logVariables = logVariables;
        randomNumberLogger = new RandomNumberLoggerListener();//logVariables, logStatistics

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
                            return valueRow.stats.getMean();
                        } else {
                            return Double.NaN;
                        }
                    case 2:
                        if (valueRow != null) {
                            return valueRow.stats.getStandardDeviation();
                        } else {
                            return Double.NaN;
                        }
                    case 3:
                        if (valueRow != null) {
                            // standard err of the mean
                            return valueRow.stats.getStandardDeviation() / Math.sqrt(valueRow.stats.getN());
                        } else {
                            return Double.NaN;
                        }
                    case 4:
                        if (valueRow != null) {
                            return valueRow.stats.getMin();
                        } else {
                            return Double.NaN;
                        }
                    case 5:
                        if (valueRow != null) {
                            return valueRow.stats.getMax();
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
    public void start(List<Object> configs) {

    }

    @Override
    public void replicate(int index, List<Value> values) {
        if (index == 0) valueRows.clear();

        randomNumberLogger.replicate(index, values);
    }

    @Override
    public void complete() {

        List<String> headers = randomNumberLogger.getHeaders();
        for (int i = 0; i < headers.size(); i++) {
            String id = headers.get(i);
            if (id == null) {
                throw new RuntimeException("Not expecting null id in variable summary!");
            }
            List<Double> randomNumbers = randomNumberLogger.getFormattedValuesById().get(id);

            //TODO use Apache
            DescriptiveStatistics stats = new SynchronizedDescriptiveStatistics();
            // Add the data
            for (int j = 0; j < randomNumbers.size(); j++) {
                stats.addValue(randomNumbers.get(j));
            }

            valueRows.add(new ValueRow(id, i, stats));

        }

        tableModel.fireTableDataChanged();
    }


}
