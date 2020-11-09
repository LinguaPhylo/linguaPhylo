package lphy.app;

import lphy.core.VarFileLogger;
import lphy.graphicalModel.*;

import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class VariableSummary extends JTable implements RandomValueLogger {

    Map<Class, Loggable> loggableMap;

    boolean logVariables;
    boolean logStatistics;

    List<String> variableNames;
    List<Double>[] values;
    double[] means;
    double[] stdev;
    double[] stderr;

    AbstractTableModel tableModel;

    public VariableSummary(boolean logStatistics, boolean logVariables) {

        this.logStatistics = logStatistics;
        this.logVariables = logVariables;

        setLoggableMap(VarFileLogger.loggableMap);

        tableModel = new AbstractTableModel() {
            @Override
            public int getRowCount() {
                return ((variableNames != null) ? variableNames.size() : 0);
            }

            @Override
            public int getColumnCount() {
                return 4;
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
                switch (columnIndex) {
                    case 0:
                        if (variableNames != null) {
                            return variableNames.get(rowIndex);
                        } else return "";
                    case 1:
                        if (means != null) {
                            return means[rowIndex];
                        } else {
                            return Double.NaN;
                        }
                    case 2:
                        if (stdev != null) {
                            return stdev[rowIndex];
                        } else {
                            return Double.NaN;
                        }
                    case 3:
                        if (stderr != null) {
                            return stderr[rowIndex];
                        } else {
                            return Double.NaN;
                        }
                }
                return "";
            }
        };

        setModel(tableModel);
    }

    public void setLoggableMap(Map<Class, Loggable> loggableMap) {
        this.loggableMap = loggableMap;
    }

    public void log(int rep, List<Value<?>> randomValues) {

        if (rep == 0) {
            // start with titles
            variableNames = new ArrayList<>();
            for (Value randomValue : randomValues) {
                if (isLogged(randomValue)) {
                    Loggable loggable = loggableMap.get(randomValue.value().getClass());
                    if (loggable != null) {
                        Collections.addAll(variableNames, loggable.getLogTitles(randomValue));
                    }
                }
            }
            values = new List[variableNames.size()];
            for (int i = 0; i < variableNames.size(); i++) {
                values[i] = new ArrayList<>();
            }
        }
        int i = 0;
        for (Value randomValue : randomValues) {

            if (isLogged(randomValue)) {
                Loggable loggable = loggableMap.get(randomValue.value().getClass());
                if (loggable != null) {
                    for (Object logValue : loggable.getLogValues(randomValue)) {
                        if (logValue instanceof Number) {
                            values[i].add(((Number) logValue).doubleValue());
                            i += 1;
                        } else if (logValue instanceof Boolean) {
                            values[i].add(((Boolean) logValue ? 1.0 : 0.0));
                            i += 1;
                        }
                    }
                }
            }
        }
    }

    public boolean isLogged(Value randomValue) {
        boolean random = ((randomValue instanceof RandomVariable && logVariables) || (!(randomValue instanceof RandomVariable) && randomValue.isRandom() && logStatistics));
        boolean number = ValueUtils.isNumberOrNumberArray(randomValue) || randomValue.value() instanceof Boolean;

        return random && number;
    }

    @Override
    public void close() {
        means = new double[variableNames.size()];
        stdev = new double[variableNames.size()];
        stderr = new double[variableNames.size()];

        for (int i = 0; i < variableNames.size(); i++) {
            for (int j = 0; j < values[i].size(); j++) {
                means[i] += values[i].get(j);
            }
            means[i] /= values[i].size();
            for (int j = 0; j < values[i].size(); j++) {
                double deviation = means[i] - values[i].get(j);
                stdev[i] += deviation * deviation;
            }
            // variance
            stdev[i] /= values[i].size();
            // stdev
            stdev[i] = Math.sqrt(stdev[i]);

            // stderr = stdev / sqrt(N)
            stderr[i] = stdev[i] / Math.sqrt(values[i].size());
        }

        tableModel.fireTableDataChanged();
    }
}
