package lphy.core;

import lphy.graphicalModel.Loggable;
import lphy.graphicalModel.RandomVariable;
import lphy.graphicalModel.RandomVariableLogger;
import lphy.graphicalModel.Value;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by adru001 on 10/03/20.
 */
public class VarFileLogger implements RandomVariableLogger {

    public static Map<Class, Loggable> loggableMap = new HashMap<>();

    static {
        loggableMap.put(Integer.class, new Loggable<Integer>() {
            @Override
            public String[] getLogTitles(Value<Integer> value) {
                return new String[]{value.getId()};
            }

            public String[] getLogValues(Value<Integer> value) {
                return new String[]{value.value().toString()};
            }
        });

        loggableMap.put(Double.class, new Loggable<Double>() {
            @Override
            public String[] getLogTitles(Value<Double> value) {
                return new String[]{value.getId()};
            }

            public String[] getLogValues(Value<Double> value) {
                return new String[]{value.value().toString()};
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

    String name;

    List<String> lines;

    public VarFileLogger(String name) {

        this.name = name;
    }

    public void log(int rep, List<RandomVariable<?>> variables) {
        StringBuilder builder = new StringBuilder();

        if (rep == 0) {
            lines = new ArrayList<>();
            // start with titles
            builder.append("sample");
            for (RandomVariable variable : variables) {
                Loggable loggable = VarFileLogger.loggableMap.get(variable.value().getClass());
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
            Loggable loggable = VarFileLogger.loggableMap.get(variable.value().getClass());
            if (loggable != null) {
                for (String logValue : loggable.getLogValues(variable)) {
                    builder.append("\t");
                    builder.append(logValue);
                }
            }
        }
        builder.append("\n");

        lines.add(builder.toString());
    }

    public void close() {

        try {
            FileWriter writer = new FileWriter(name + ".log");
            for (String line : lines) {
                writer.write(line);
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
