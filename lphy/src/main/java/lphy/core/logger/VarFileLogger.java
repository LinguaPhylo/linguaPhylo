package lphy.core.logger;

import lphy.core.model.RandomVariable;
import lphy.core.model.Symbols;
import lphy.core.model.Value;
import lphy.core.vectorization.VectorUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by Alexei Drummond on 10/03/20.
 */
public class VarFileLogger implements FileLogger {

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

    } // static { } finishes here

    File dir = null;
    String fileStem;

    private StringBuilder builder = new StringBuilder();

//    boolean logVariables;
//    boolean logStatistics;

//    public VarFileLogger(String fileStem, boolean logStatistics, boolean logVariables) {
//        this.fileStem = fileStem;
//        this.logStatistics = logStatistics;
//        this.logVariables = logVariables;
//    }

//    public VarFileLogger(String fileStem, File dir, boolean logStatistics, boolean logVariables) {
//        this(fileStem, logStatistics, logVariables);
//        this.dir = dir;
//    }

    /**
     * used by SPI, then use getter setter to config files and paths.
     */
    public VarFileLogger() {
    }

    @Override
    public void start(List<Value<?>> randomValues) {
        // start with titles
        builder.append("sample");
        for (Value randomValue : randomValues) {
            if (isValueLoggable(randomValue)) {
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

    @Override
    public void log(int rep, List<Value<?>> randomValues) {
        builder.append(rep);
        for (Value randomValue : randomValues) {
            if (isValueLoggable(randomValue)) {
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
    }

    @Override
    public void stop() {
        String fileName = createFileName(fileStem, "", ".log");
        File file = getFile(fileName);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.append(builder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isValueLoggable(Value randomValue) {
        return randomValue instanceof RandomVariable ||
                // random value but no anonymous
                (randomValue.isRandom() && !randomValue.isAnonymous());
    }

    @Override
    public File getFile(String fileName) {
        File file;
        if (dir != null)
            file = new File(dir + File.separator + fileName);
        else file = new File(fileName);
        return file;
    }

    @Override
    public void setDir(File dir) {
        this.dir = dir;
    }

    @Override
    public void setFileStem(String fileStem) {
        this.fileStem = fileStem;
    }

}
