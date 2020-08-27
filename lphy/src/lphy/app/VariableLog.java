package lphy.app;

import lphy.core.VarFileLogger;
import lphy.graphicalModel.Loggable;
import lphy.graphicalModel.RandomVariable;
import lphy.graphicalModel.RandomValueLogger;
import lphy.graphicalModel.Value;

import javax.swing.*;
import java.util.List;
import java.util.Map;

public class VariableLog extends JTextArea implements RandomValueLogger {

    Map<Class, Loggable> loggableMap;

    boolean logVariables;
    boolean logStatistics;

    public VariableLog(boolean logStatistics, boolean logVariables) {

        setTabSize(4);
        setEditable(false);

        this.logStatistics = logStatistics;
        this.logVariables = logVariables;

        setLoggableMap(VarFileLogger.loggableMap);
    }

    public void clear() {
        setText("");
    }

    public void setLoggableMap(Map<Class, Loggable> loggableMap) {
        this.loggableMap = loggableMap;
    }

    public void log(int rep, List<Value<?>> randomValues) {
        StringBuilder builder = new StringBuilder();

        if (rep == 0) {
            clear();
            // start with titles
            builder.append("sample");
            for (Value randomValue : randomValues) {
                if (isLogged(randomValue)) {
                    Loggable loggable = loggableMap.get(randomValue.value().getClass());
                    if (loggable != null) {
                        for (String title : loggable.getLogTitles(randomValue)) {
                            builder.append("\t");
                            builder.append(title);
                        }
                    }
                }
            }
            builder.append("\n");

        }
        if (rep < 1000) {
            builder.append(rep);
            for (Value randomValue : randomValues) {
                if (isLogged(randomValue)) {
                    Loggable loggable = loggableMap.get(randomValue.value().getClass());
                    if (loggable != null) {
                        for (String logValue : loggable.getLogValues(randomValue)) {
                            builder.append("\t");
                            builder.append(logValue);
                        }
                    }
                }
            }
            builder.append("\n");
        }
        append(builder.toString());
    }

    public boolean isLogged(Value randomValue) {
        return ((randomValue instanceof RandomVariable && logVariables) || (!(randomValue instanceof RandomVariable) && randomValue.isRandom() && logStatistics));
    }

    @Override
    public void close() {

    }
}
