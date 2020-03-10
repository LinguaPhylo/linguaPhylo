package lphy.app;

import lphy.core.VarFileLogger;
import lphy.graphicalModel.Loggable;
import lphy.graphicalModel.RandomVariable;
import lphy.graphicalModel.RandomVariableLogger;
import lphy.graphicalModel.Value;

import javax.swing.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VariableLog extends JTextArea implements RandomVariableLogger {

    Map<Class, Loggable> loggableMap;

    public VariableLog() {

        setTabSize(4);
        setEditable(false);

        setLoggableMap(VarFileLogger.loggableMap);
    }

    public void clear() {
        setText("");
    }

    public void setLoggableMap(Map<Class, Loggable> loggableMap) {
        this.loggableMap = loggableMap;
    }

    public void log(int rep, List<RandomVariable<?>> variables) {
        StringBuilder builder = new StringBuilder();

        if (rep == 0) {
            clear();
            // start with titles
            builder.append("sample");
            for (RandomVariable variable : variables) {
                Loggable loggable = loggableMap.get(variable.value().getClass());
                if (loggable != null) {
                    for (String title : loggable.getLogTitles(variable)) {
                        builder.append("\t");
                        builder.append(title);
                    }
                }
            }
            builder.append("\n");

        }
        if (rep < 1000) {
            builder.append(rep);
            for (RandomVariable variable : variables) {
                Loggable loggable = loggableMap.get(variable.value().getClass());
                if (loggable != null) {
                    for (String logValue : loggable.getLogValues(variable)) {
                        builder.append("\t");
                        builder.append(logValue);
                    }
                }
            }
            builder.append("\n");
        }
        append(builder.toString());
    }

    @Override
    public void close() {

    }
}
