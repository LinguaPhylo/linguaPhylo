package lphy.core;

import lphy.graphicalModel.Loggable;
import lphy.graphicalModel.RandomVariable;
import lphy.graphicalModel.RandomValueLogger;
import lphy.graphicalModel.Value;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by adru001 on 10/03/20.
 */
public class VarFileLogger implements RandomValueLogger {

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

    boolean logVariables;
    boolean logStatistics;

    public VarFileLogger(String name, boolean logStatistics, boolean logVariables) {

        this.name = name;

        this.logStatistics = logStatistics;
        this.logVariables = logVariables;
    }

    public void log(int rep, List<Value<?>> randomValues) {
        StringBuilder builder = new StringBuilder();

        if (rep == 0) {
            lines = new ArrayList<>();
            // start with titles
            builder.append("sample");
            for (Value randomValue : randomValues) {
                if (isLogged(randomValue)) {
                    Loggable loggable = VarFileLogger.loggableMap.get(randomValue.value().getClass());
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
        builder.append(rep);
        for (Value randomValue : randomValues) {
            if (isLogged(randomValue)) {
                Loggable loggable = VarFileLogger.loggableMap.get(randomValue.value().getClass());
                if (loggable != null) {
                    for (String logValue : loggable.getLogValues(randomValue)) {
                        builder.append("\t");
                        builder.append(logValue);
                    }
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

    public boolean isLogged(Value randomValue) {
        return ((randomValue instanceof RandomVariable && logVariables) || (!(randomValue instanceof RandomVariable) && randomValue.isRandom() && logStatistics));
    }
}
