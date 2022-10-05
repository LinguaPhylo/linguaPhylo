package lphy.graphicalModel.logger;

import lphy.graphicalModel.*;
import lphy.util.Symbols;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by Alexei Drummond on 10/03/20.
 */
public class VarFileLogger implements RandomValueLogger {

    public static Map<Class, Loggable> loggableMap = new HashMap<>();

    static {
        loggableMap.put(Integer.class, new Loggable<Integer>() {
            @Override
            public String[] getLogTitles(Value<Integer> value) {
                return new String[]{Symbols.getCanonical(value.getId())};
            }

            public Integer[] getLogValues(Value<Integer> value) {
                return new Integer[]{value.value()};
            }
        });

        loggableMap.put(Boolean.class, new Loggable<Boolean>() {
            @Override
            public String[] getLogTitles(Value<Boolean> value) {
                return new String[]{Symbols.getCanonical(value.getId())};
            }

            public Boolean[] getLogValues(Value<Boolean> value) {
                return new Boolean[]{value.value()};
            }
        });

        loggableMap.put(Double.class, new Loggable<Double>() {
            @Override
            public String[] getLogTitles(Value<Double> value) {
                return new String[]{Symbols.getCanonical(value.getId())};
            }

            public Double[] getLogValues(Value<Double> value) {
                return new Double[]{value.value()};
            }
        });

        loggableMap.put(Double[].class, new Loggable<Double[]>() {
            @Override
            public String[] getLogTitles(Value<Double[]> value) {
                String[] names = new String[value.value().length];
                for (int i = 0; i < names.length; i++) {
                    names[i] = Symbols.getCanonical(value.getId()) + VectorUtils.INDEX_SEPARATOR + i;
                }
                return names;
            }

            public Double[] getLogValues(Value<Double[]> value) {
                return value.value();
            }
        });

        loggableMap.put(Integer[].class, new Loggable<Integer[]>() {
            @Override
            public String[] getLogTitles(Value<Integer[]> value) {
                String[] names = new String[value.value().length];
                for (int i = 0; i < names.length; i++) {
                    names[i] = Symbols.getCanonical(value.getId()) + VectorUtils.INDEX_SEPARATOR + i;
                }
                return names;
            }

            public Integer[] getLogValues(Value<Integer[]> value) {
                return value.value();
            }
        });

        // only for vectorized 1d array => 2d array
        loggableMap.put(Double[][].class, new Loggable<Double[][]>() {
            @Override
            public String[] getLogTitles(Value<Double[][]> value) {
                // flatten
                List<String> names = new ArrayList<>();
                Double[][] tmpArr = value.value();
                for (int i = 0; i < tmpArr.length; i++) {
                    for (int j = 0; j < tmpArr[i].length; j++) {
                        names.add(Symbols.getCanonical(value.getId()) + VectorUtils.INDEX_SEPARATOR +
                                i + VectorUtils.INDEX_SEPARATOR + j);
                    }
                }
                return names.toArray(String[]::new);
            }

            // flatten 2d => 1d
            public Double[] getLogValues(Value<Double[][]> value) {
                List<Double> vals = new ArrayList<>();
                Double[][] tmpArr = value.value();
                for (Double[] doubles : tmpArr) {
                    vals.addAll(Arrays.asList(doubles));
                }
                return vals.toArray(Double[]::new);
            }
        });

        // only for vectorized 1d array => 2d array
        loggableMap.put(Integer[][].class, new Loggable<Integer[][]>() {
            @Override
            public String[] getLogTitles(Value<Integer[][]> value) {
                // flatten
                List<String> names = new ArrayList<>();
                Integer[][] tmpArr = value.value();
                for (int i = 0; i < tmpArr.length; i++) {
                    for (int j = 0; j < tmpArr[i].length; j++) {
                        names.add(Symbols.getCanonical(value.getId()) + VectorUtils.INDEX_SEPARATOR +
                                i + VectorUtils.INDEX_SEPARATOR + j);
                    }
                }
                return names.toArray(String[]::new);
            }

            // flatten 2d => 1d
            public Integer[] getLogValues(Value<Integer[][]> value) {
                List<Integer> vals = new ArrayList<>();
                Integer[][] tmpArr = value.value();
                for (Integer[] doubles : tmpArr) {
                    vals.addAll(Arrays.asList(doubles));
                }
                return vals.toArray(Integer[]::new);
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
                    for (Object logValue : loggable.getLogValues(randomValue)) {
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
        return ((randomValue instanceof RandomVariable && logVariables) ||
                // random value but no anonymous
                (!(randomValue instanceof RandomVariable) && randomValue.isRandom() &&
                        logStatistics && !randomValue.isAnonymous()));
    }
}
