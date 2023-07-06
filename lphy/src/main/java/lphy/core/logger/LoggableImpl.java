package lphy.core.logger;

import lphy.core.model.Symbols;
import lphy.core.model.Value;
import lphy.core.vectorization.VectorUtils;

import java.util.*;

@Deprecated
public class LoggableImpl {

    private static Map<Class<?>, Loggable<?>> loggableMap = new HashMap<>();

    public static Map<Class<?>, Loggable<?>> getLoggableMap() {
        return loggableMap;
    }

    @Deprecated
    public static Loggable<?> getLoggable(Class<?> cls) {
        return loggableMap.get(cls);
    }

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

}
