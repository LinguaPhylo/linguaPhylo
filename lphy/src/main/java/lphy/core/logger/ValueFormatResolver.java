package lphy.core.logger;

import lphy.core.model.Value;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Resolve the multiple ValueFormatter mapping to the same data type.
 * Pick up the 1st as default.
 */
public class ValueFormatResolver {

    // after newInstance
    private static Map<Class<?>, Class<? extends ValueFormatter>> resolvedFormatterClasses;
    private static final int DEFAULT_FORMATTER = 0; // 1st formatter

    public ValueFormatResolver(Map<Class<?>, Set<Class<? extends ValueFormatter>>> valueFormatterClasses) {
        init();
        resolveFormatters(valueFormatterClasses);
    }

    /**
     * @param value {@link Value}
     * @return      the ValueFormatter mapped to a data type.
     *              If the data type of {@link Value} is an array,
     *              the key used for matching with the ValueFormatter is determined
     *              based on the data type of the first element in the array.
     */
    public List<ValueFormatter> getFormatter(Value value) {
        ValueFormatter valueFormatter = createInstanceFromSingleton(value.getType(), value.getId(), value.value());
        if (valueFormatter != null)
            return List.of(valueFormatter);

        // else decompose arrays into singleton,
        List<ValueFormatter> arrVFList = new ArrayList<>();
        // then use the singleton data type as key
        if (value.value() instanceof Object[][] arr) {
            // flatten 2d to 1d
            for (int i = 0; i < arr.length; i++) {
                for (int j = 0; j < arr[i].length; j++) {
                    Class elementCls = arr[i][j].getClass();
                    String elementValueId = Array2DValueFormatter.getElementValueId(value.getId(), i, j);
                    ValueFormatter elementValueFormatter =
                            // must use array element value Id here
                            createInstanceFromSingleton(elementCls, elementValueId, arr[i]);
                    if (elementValueFormatter == null)
                        throw new RuntimeException("Cannot resolve formatter for " + value.getId() +
                                ", where value = " + value.value());
                    // must use array value id here
                    arrVFList.add(new Array2DValueFormatter(value.getId(), elementValueFormatter, i, j));
                }
            }

        } else if (value.value() instanceof Object[] arr) {
            for (int i = 0; i < arr.length; i++) {
                Class elementCls = arr[i].getClass();
                String elementValueId = ArrayValueFormatter.getElementValueId(value.getId(), i);
                ValueFormatter elementValueFormatter =
                        // must use array element value Id here
                        createInstanceFromSingleton(elementCls, elementValueId, arr[i]);
                if (elementValueFormatter == null)
                    throw new RuntimeException("Cannot resolve formatter for " + value.getId() +
                            ", where value = " + value.value());
                arrVFList.add(new ArrayValueFormatter(value.getId(), elementValueFormatter, i));
            }
        }
        return arrVFList;
    }

    public <T> ValueFormatter<T> createInstanceFromSingleton(Class valType, String valId, T valValue) {

//        Class cls = value.getType();
        // if data type is declared in the map, e.g. special ValueFormatter for T[] or T[][]
        if (resolvedFormatterClasses.containsKey(valType)) {

            Class<? extends ValueFormatter> clsVF = resolvedFormatterClasses.get(valType);

            Constructor<?> constructor;
            try {
                // valType is T of Value<T>, so it must have a constructor ValueFormatterImpl(String, T)
                constructor = clsVF.getDeclaredConstructor(String.class, valType);
            } catch (NoSuchMethodException e) {
                try {
                    constructor = clsVF.getDeclaredConstructor(String.class, Object.class);
                } catch (NoSuchMethodException ex) {
                    throw new RuntimeException("ValueFormatter implementation must have a constructor " +
                            "ValueFormatterImpl(String, T), where T is the type of Value<T> !\n\n" + e);
                }
            }

            ValueFormatter valueFormatter = null;
            try {
                valueFormatter = (ValueFormatter) constructor.newInstance(valId, valValue);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            return valueFormatter;
        }
        return null;
    }


    //TODO add strategies

    private void init() {
        resolvedFormatterClasses = new HashMap<>();

        // add primary data types first
        resolvedFormatterClasses.put(Integer.class, ValueFormatter.Base.class);
        resolvedFormatterClasses.put(Double.class, ValueFormatter.Base.class);
        resolvedFormatterClasses.put(Boolean.class, ValueFormatter.Base.class);
        resolvedFormatterClasses.put(String.class, ValueFormatter.Base.class);

//        Map<Class<?>, ValueFormatter> primaryFormatters = PrimaryFormatterRegistry.getPrimaryFormatters();
//        resolvedFormatters.putAll(primaryFormatters);
    }


    private void resolveFormatters(Map<Class<?>, Set<Class<? extends ValueFormatter>>> valueFormatterClasses) {

        for (Map.Entry<Class<?>, Set<Class<? extends ValueFormatter>>> entry : valueFormatterClasses.entrySet()) {
            Class<?> cls = entry.getKey();
            Set<Class<? extends ValueFormatter>> fmClsSet = entry.getValue();

            // TODO
            Class<? extends ValueFormatter> f = null;
//            try {
                f = resolveFormatter(cls, fmClsSet);
//            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
//                     IllegalAccessException | IllegalArgumentException e) {
//                throw new RuntimeException(e);
//            }

            resolvedFormatterClasses.put(cls, f);
        }
    }


    private Class<? extends ValueFormatter> resolveFormatter(Class<?> cls, Set<Class<? extends ValueFormatter>> formatterClasses)
//            throws NoSuchMethodException, InvocationTargetException, InstantiationException,IllegalAccessException,
            throws IllegalArgumentException{

        if (formatterClasses.size() < 1)
            //TODO inherited data type, such as a different Alignment
//            return new ValueFormatter<T>() { };
          throw new IllegalArgumentException("Cannot find the formatter for " + cls.getName());

        List<Class<? extends ValueFormatter>> vFClsist = new ArrayList<>(formatterClasses);

        //TODO more strategies here

        // default to take the 1st
        Class<? extends ValueFormatter> vFCls = vFClsist.get(DEFAULT_FORMATTER);

        return vFCls;

        // create instance
//        return vFCls.getDeclaredConstructor().newInstance();
    }


}
