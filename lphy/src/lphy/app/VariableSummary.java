package lphy.app;

import lphy.core.VarFileLogger;
import lphy.graphicalModel.*;

import javax.swing.*;
import javax.swing.event.TableModelListener;
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
    double[] stderr;

    public VariableSummary(boolean logStatistics, boolean logVariables) {

        this.logStatistics = logStatistics;
        this.logVariables = logVariables;

        setLoggableMap(VarFileLogger.loggableMap);

        setModel(new TableModel() {
            @Override
            public int getRowCount() {
                return ((variableNames != null) ? variableNames.size() : 0);
            }

            @Override
            public int getColumnCount() {
                return 3;
            }

            @Override
            public String getColumnName(int columnIndex) {
                switch (columnIndex) {
                    case 0:
                        return "Variable";
                    case 1:
                        return "Mean";
                    case 2:
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
                        return Double.class;
                    case 2:
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
                        if (stderr != null) {
                            return stderr[rowIndex];
                        } else {
                            return Double.NaN;
                        }
                }
                return "";
            }

            @Override
            public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

            }

            @Override
            public void addTableModelListener(TableModelListener l) {

            }

            @Override
            public void removeTableModelListener(TableModelListener l) {

            }
        });
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
                    for (String logValue : loggable.getLogValues(randomValue)) {
                        values[i].add(Double.parseDouble(logValue));
                        i += 1;
                    }
                }
            }
        }
    }

    public boolean isLogged(Value randomValue) {
        boolean random = ((randomValue instanceof RandomVariable && logVariables) || (!(randomValue instanceof RandomVariable) && randomValue.isRandom() && logStatistics));
        boolean number = ValueUtils.isNumberOrNumberArray(randomValue);

        return random && number;
    }

    @Override
    public void close() {
        means = new double[variableNames.size()];
        stderr = new double[variableNames.size()];

        for (int i = 0; i < variableNames.size(); i++) {
            for (int j = 0; j < values[i].size(); j++) {
                means[i] += values[i].get(j);
            }
            means[i] /= values[i].size();
            for (int j = 0; j < values[i].size(); j++) {
                double deviation = means[i] - values[i].get(j);
                stderr[i] += deviation * deviation;
            }
            // variance
            stderr[i] /= values[i].size();

            // stderr = stdev / sqrt(N)
            stderr[i] = Math.sqrt(stderr[i]) / Math.sqrt(values[i].size());
        }
    }
}
