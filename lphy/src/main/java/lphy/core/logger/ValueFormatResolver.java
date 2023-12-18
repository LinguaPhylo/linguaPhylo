package lphy.core.logger;

import lphy.core.model.Value;

import java.lang.reflect.Array;
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

    //TODO in dev
    public ValueFormatter getDefaultFormatter(Value value) {
        List<ValueFormatter> formatters = getFormatter(value);
        if (formatters.size() < 1)
            return null;
        return formatters.get(DEFAULT_FORMATTER);
    }

    public Map<Class<?>, Class<? extends ValueFormatter>> getResolvedFormatterClasses() {
        return resolvedFormatterClasses;
    }

    /**
     * Only allow the single T in Value, or CompoundVectorValue for T[] or T[][]
     * @param value {@link Value}
     * @return      The list of ValueFormatters, which is mapped to data types
     *              and loaded using the SPI mechanism.
     *              If the data type of the {@link Value} is an array,
     *              the key for matching with the ValueFormatter is
     *              determined based on the data type of the first element in the array.
     * @see #createInstanceFrom(Class, Object...)
     */
    public List<ValueFormatter> getFormatter(Value value) {
        Class valType = value.getType();
        // if data type is registered in SPI, including special ValueFormatter for T[] or T[][]

        Class<? extends ValueFormatter> valueFormatterCls = getFormatterClass(valType, resolvedFormatterClasses);

        if (valueFormatterCls != null) {
//        if (resolvedFormatterClasses.containsKey(valType)) {
//            Class<? extends ValueFormatter> valueFormatterCls = resolvedFormatterClasses.get(valType);
            //TODO T[][] cannot go here
            return List.of( createInstanceFrom(valueFormatterCls,
                    value.getId(), value.value()) );
        } else {
            // else check array
            if (value.value() instanceof Object[][] arr) {
                valType = arr[0][0].getClass();
                if (resolvedFormatterClasses.containsKey(valType)) {
                    Class<? extends ValueFormatter> vfCls = resolvedFormatterClasses.get(valType);
                    return createFormatter(vfCls, value);
                }
            } else if (value.value() instanceof Object[] arr) {
                valType = arr[0].getClass();
                if (resolvedFormatterClasses.containsKey(valType)) {
                    Class<? extends ValueFormatter> vfCls = resolvedFormatterClasses.get(valType);
                    return createFormatter(vfCls, value);
                }
            }
        }
        throw new RuntimeException("Cannot resolve formatter for " + value.getId() +
                ", where type = " + value.getType() + ", value = " + value.value() + " !");
    }

    public Class<? extends ValueFormatter> getFormatterClass(Class<?> valueType, Map<Class<?>, Class<? extends ValueFormatter>> resolvedFormatterClasses) {
        if (resolvedFormatterClasses.containsKey(valueType))
            return resolvedFormatterClasses.get(valueType);
        else {
            Class<? extends ValueFormatter> formatterClass = resolvedFormatterClasses.entrySet().stream()
                    .filter(entry -> entry.getKey().isAssignableFrom(valueType))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse(null);
            return formatterClass;
        }
//        return null;
    }

    /**
     * @param valFmtCls  The class of {@link ValueFormatter}
     * @param value      {@link Value}
     * @param extraArgs  array of objects to be passed as extra arguments to the constructor call,
     *                   besides valueId and value at the base constructor
     *                   {@link ValueFormatter.Base(String, Object)}.
     *                   E.g. boolean to check if the data is clamped.
     * @return    If the given {@link Value} is an array,
     *            return a list of {@link ArrayElementFormatter} or
     *            {@link Array2DElementFormatter} instances created from the given class,
     *            where each element of the array corresponds to a ValueFormatter.
     *            Otherwise, return a single {@link ValueFormatter} instance created
     *            from the given class, with the data type matching the type of {@link Value}.
     */
    public static List<ValueFormatter> createFormatter(Class<? extends ValueFormatter> valFmtCls,
                                                       Value value, Object... extraArgs) {

        List<ValueFormatter> arrVFList = new ArrayList<>();
        // decompose arrays into singleton,
        if (value.value() instanceof Object[][] arr) {
            // flatten 2d to 1d
            for (int i = 0; i < arr.length; i++) {
                for (int j = 0; j < arr[i].length; j++) {
//                    Class elementCls = arr[i][j].getClass();
                    String elementValueId = Array2DElementFormatter.getElementValueId(value.getId(), i, j);

                    ValueFormatter  elementVF = createInstanceFrom(valFmtCls,
                            // must use array element value Id here
                            elementValueId, arr[i][j], extraArgs);
                    // must use array value id here
                    arrVFList.add(new Array2DElementFormatter(value.getId(), elementVF, i, j));
                }
            }

        } else if (value.value() instanceof Object[] arr) {
            for (int i = 0; i < arr.length; i++) {
//                Class elementCls = arr[i].getClass();
                String elementValueId = ArrayElementFormatter.getElementValueId(value.getId(), i);
                ValueFormatter elementVF = createInstanceFrom(valFmtCls,
                        // must use array element value Id here
                        elementValueId, arr[i], extraArgs);
                // must use array value id here
                arrVFList.add(new ArrayElementFormatter(value.getId(), elementVF, i));
            }
        } else {
            // else use the singleton data type as key
            ValueFormatter valueFormatter = createInstanceFrom(valFmtCls,
                    value.getId(), value.value(), extraArgs);

            arrVFList.add(valueFormatter);
        }
        return arrVFList;
    }

    public static <T> T createInstanceFrom(Class<T> valFmtCls, Object... initArgs) {
        // merge the extra args
        List<Object> mergedArgs = new ArrayList<>();
        if ( ! (initArgs.length == 2 || initArgs.length == 3) ) {
            throw new IllegalArgumentException(valFmtCls.getName() + " args have to contain " +
                    "at least ID and Value, or some extra args ! " + Arrays.toString(initArgs));
        }
        mergedArgs.add(initArgs[0]);
        mergedArgs.add(initArgs[1]);
        // Object... extraArgs
        if (initArgs.length == 3 && initArgs[2].getClass().isArray()) {
            int length = Array.getLength(initArgs[2]);
            for (int i = 0; i < length; i++) {
                Object element = Array.get(initArgs[2], i);
                // add each element
                mergedArgs.add(element);
            }
        }
        // Get the appropriate constructor based on the initialization objects
        Class<?>[] parameterTypes = mergedArgs.stream()
                .map(Object::getClass)
                .toArray(Class<?>[]::new);

        Constructor<T> constructor = null;
        try {
            constructor = valFmtCls.getConstructor(parameterTypes);
        } catch (NoSuchMethodException e) {
            // ValueFormatter.Base(String valueID, T value) will go here
            // try the 1st Constructor
            Constructor[] publicConstructors = valFmtCls.getConstructors();
            if (publicConstructors.length != 1) {
                throw new RuntimeException(valFmtCls.getName() + " do not have a constructor " +
                        "to handle the parameter types : " + Arrays.toString(parameterTypes) +
                        "!\n\n" + e);
            }
            constructor = publicConstructors[0];
        }

        try {// Create a new instance using the constructor and initialization objects
            return Objects.requireNonNull(constructor).newInstance(mergedArgs.toArray());
        } catch (InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(valFmtCls.getName() + " cannot create an instance " +
                    "from : " + mergedArgs + "!\n\n" + e);
        }
    }







//    public static <T> ValueFormatter<T> createInstanceOfSingleton(Class<? extends ValueFormatter> valueFormatterCls,
//                                                           Class valueType, String valId, T valValue) {
//
//            Constructor<?> constructor;
//            try {
//                // valueType is T of Value<T>, so it must have a constructor ValueFormatterImpl(String, T)
//                constructor = getConstructor(valueFormatterCls, String.class, valueType);
//            } catch (RuntimeException e) {
//                //
//                constructor = getConstructor(valueFormatterCls, String.class, Object.class);
//
//            }
//
//            return createInstanceFrom(constructor, valId, valValue);
//
//    }
//
//
//    public static Constructor<?> getConstructor(Class<? extends ValueFormatter> valueFormatterCls,
//                                                Class<?>... parameterTypes) {
//        Constructor<?> constructor;
//        try {
//            // e.g. constructor ValueFormatterImpl(String, T)
//            constructor = valueFormatterCls.getDeclaredConstructor(parameterTypes);
//        } catch (NoSuchMethodException e) {
//            throw new RuntimeException(valueFormatterCls.getName() + " do not have a constructor " +
//                    "to handle the parameter types : " + Arrays.toString(parameterTypes) +
//                    "!\n\n" + e);
//        }
//        return constructor;
//    }
//
//    public static ValueFormatter createInstanceFrom(Constructor<?> constructor, Object ... initargs) {
//        ValueFormatter valueFormatter = null;
//        try {
//            valueFormatter = (ValueFormatter) Objects.requireNonNull(constructor).newInstance(initargs);
//        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
//            throw new RuntimeException(e);
//        }
//        return valueFormatter;
//    }
//



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
