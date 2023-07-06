package lphy.core.logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FormatterRegistry {
    private static Map<Class<?>, Set<Class<? extends ValueFormatter>>> allFormatters = new HashMap<>();

    public static Map<Class<?>, Set<Class<? extends ValueFormatter>>> getAllFormattersMap() {
        return allFormatters;
    }

//    public static List<Class<? extends ValueFormatter>> getAllFormatterClsList() {
//        List<Class<? extends ValueFormatter>> formatterList = new ArrayList<>();
//        for (Set<Class<? extends ValueFormatter>> fmSet : allFormatters.values()) {
//            formatterList.addAll(fmSet);
//        }
//        return formatterList;
//    }

    public static Set<Class<? extends ValueFormatter>> getFormatter(Class cls) {
//        Class cls = value.getType();
        return allFormatters.get(cls);
    }

//    public static List<ValueFormatter> createValueFormatterInstances() {
//
//        // get defaults
//        List<ValueFormatter> formatters = FormatterRegistry.getAllFormatterClsList();
//
//        List<ValueFormatter> instances = valueFormatters.values().stream()
//                .flatMap(Set::stream)
//                .map(cls -> {
//                    try {
//                        return (ValueFormatter) cls.getDeclaredConstructor().newInstance();
//                    } catch (InstantiationException | IllegalAccessException |
//                             NoSuchMethodException | InvocationTargetException e) {
//                        e.printStackTrace();
//                        return null; // or handle the exception as needed
//                    }
//                }).toList();
//
//        formatters.addAll(instances);
//        return formatters;
//    }
//    public static List<SimulatorListener> createSimulatorListenerInstances() {
//
//        return simulatorListeners.values().stream()
//                .flatMap(Set::stream)
//                .map(cls -> {
//                    try {
//                        return (SimulatorListener) cls.getDeclaredConstructor().newInstance();
//                    } catch (InstantiationException | IllegalAccessException |
//                             NoSuchMethodException | InvocationTargetException e) {
//                        e.printStackTrace();
//                        return null; // or handle the exception as needed
//                    }
//                }).collect(Collectors.toList());
//    }


    static {
        allFormatters.put(Integer.class, Set.of(ValueFormatter.Base.class) );
        allFormatters.put(Double.class, Set.of(ValueFormatter.Base.class) );
        allFormatters.put(Boolean.class, Set.of(ValueFormatter.Base.class) );
//        allFormatters.put(Boolean.class, Arrays.asList(new ValueFormatter<Boolean>() {
//            @Override
//            public String[] format(Value<Boolean> value) {
//                return new String[]{value.value() ? "1.0" : "0.0"};
//            }
//        } ));
        allFormatters.put(String.class, Set.of(ValueFormatter.Base.class) );

        allFormatters.put(Integer[].class, Set.of(ValueFormatter.Base.class) );
        allFormatters.put(Double[].class, Set.of(ValueFormatter.Base.class) );
        allFormatters.put(Boolean[].class, Set.of(ValueFormatter.Base.class) );
        allFormatters.put(String[].class, Set.of(ValueFormatter.Base.class) );

        allFormatters.put(Integer[][].class, Set.of(ValueFormatter.Base.class) );
        allFormatters.put(Double[][].class, Set.of(ValueFormatter.Base.class) );
        allFormatters.put(Boolean[][].class, Set.of(ValueFormatter.Base.class) );
        allFormatters.put(String[][].class, Set.of(ValueFormatter.Base.class) );
    }

}
