package lphy.app;

import lphy.graphicalModel.Loggable;
import lphy.graphicalModel.RandomVariable;
import lphy.graphicalModel.RandomVariableLogger;
import lphy.graphicalModel.Value;

import javax.swing.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Log extends JTextArea implements RandomVariableLogger {

    int rep = 0;
    Map<Class, Loggable> loggableMap = new HashMap<>();

    public Log() {

        setTabSize(4);
        setEditable(false);

        loggableMap.put(Integer.class, new Loggable<Integer>() {
            @Override
            public String[] getLogTitles(Value<Integer> value) {
                return new String[] {value.getId()};
            }

            public String[] getLogValues(Value<Integer> value) {
                return new String[] {value.value().toString()};
            }
        });

        loggableMap.put(Double.class, new Loggable<Double>() {
            @Override
            public String[] getLogTitles(Value<Double> value) {
                return new String[] {value.getId()};
            }

            public String[] getLogValues(Value<Double> value) {
                return new String[] {value.value().toString()};
            }
        });

        loggableMap.put(Double[].class, new Loggable<Double[]>() {
            @Override
            public String[] getLogTitles(Value<Double[]> value) {
                String[] names = new String[value.value().length];
                for (int i = 0; i < names.length; i++) {
                    names[i] = value.getId() + "." + i;
                }
                return names;
            }

            public String[] getLogValues(Value<Double[]> value) {
                String[] vals = new String[value.value().length];
                for (int i = 0; i < vals.length; i++) {
                    vals[i] = value.value()[i].toString();
                }
                return vals;
            }
        });
    }

    public void clear() {
        setText("");
        rep = 0;
    }

    public void log(List<RandomVariable<?>> variables) {
        if (rep < 1000) {
            StringBuilder builder = new StringBuilder();
            if (getText().length() == 0) {
                // start with titles
                builder.append("rep");
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
            append(builder.toString());
            rep += 1;
        }
    }
}
