package lphy.core.model.components;

import lphy.core.model.types.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ValueUtils {

    public static boolean isValueOfDeterministicFunction(Value value) {
        return !isRandomVariable(value) && value.getGenerator() != null;
    }

    public static boolean isFixedValue(Value value) {
        return value.getGenerator() == null && !(value instanceof RandomVariable);
    }

    public static boolean isRandomVariable(Value value) {
        return value instanceof RandomVariable;
    }

    public static boolean isNumberOrNumberArray(Value value) {
        Class<?> valueClass = value.value().getClass();
        return Number.class.isAssignableFrom(valueClass) ||
                (valueClass.isArray() && Number.class.isAssignableFrom(valueClass.getComponentType()));
    }

    public static boolean is2DNumberArray(Value value) {
        Class<?> valueClass = value.value().getClass();
        return valueClass.isArray() && valueClass.getComponentType().isArray() &&
                Number.class.isAssignableFrom(valueClass.getComponentType().getComponentType());
    }

    public static boolean isNumber(Value value) {
        Object val = value.value();
        return val instanceof Number;
    }

    public static boolean isMultiDimensional(Object v) {
        return (v instanceof MultiDimensional || v instanceof Map || v.getClass().isArray() || v instanceof List<?>);
    }

    public static boolean isInteger(String s) {
        try {
            int i = Integer.parseInt(s);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    /**
     * useful function to get a number value as a double;
     *
     * @param value
     * @return
     */
    public static double doubleValue(Value<Number> value) {
        return value.value().doubleValue();
    }

    public static double[] doubleArrayValue(Value<Number[]> value) {
        Number[] num = value.value();
        double[] values = new double[num.length];
        for (int i = 0; i < num.length; i++) {
            values[i] = num[i].doubleValue();
        }
        return values;
    }

    public static Value createValue(Integer intValue, DeterministicFunction generator) {
        return new IntegerValue(null, intValue, generator);
    }

    public static Value createValue(Integer[] intArrayValue, DeterministicFunction generator) {
        return new IntegerArrayValue(null, intArrayValue, generator);
    }

    public static Value createValue(Double doubleValue, DeterministicFunction generator) {
        return new DoubleValue(null, doubleValue, generator);
    }

    public static Value createValue(Double[] doubleArrayValue, DeterministicFunction generator) {
        return new DoubleArrayValue(null, doubleArrayValue, generator);
    }

    public static Value createValue(Boolean booleanValue, DeterministicFunction generator) {
        return new BooleanValue(null, booleanValue, generator);
    }

    public static Value createValue(Boolean[] booleanArrayValue, DeterministicFunction generator) {
        return new BooleanArrayValue(null, booleanArrayValue, generator);
    }

    public static Value createValue(String strValue, DeterministicFunction generator) {
        return new StringValue(null, strValue, generator);
    }

    public static Value createValue(String[] strArrayValue, DeterministicFunction generator) {
        return new StringArrayValue(null, strArrayValue, generator);
    }

    public static Value createValue(Object value, DeterministicFunction generator) {
        if (value instanceof Integer) return createValue((Integer) value, generator);
        if (value instanceof Integer[]) return createValue((Integer[]) value, generator);
        if (value instanceof Double) return createValue((Double) value, generator);
        if (value instanceof Double[]) return createValue((Double[]) value, generator);
        if (value instanceof Boolean) return createValue((Boolean) value, generator);
        if (value instanceof Boolean[]) return createValue((Boolean[]) value, generator);
        if (value instanceof String) return createValue((String) value, generator);
        if (value instanceof String[]) return createValue((String[]) value, generator);
        return new Value(null, value, generator);
    }

    /**
     * This is used to handle generic array, which has to be initiated as Object[].
     * @param arr2List   generic type
     * @param generator  a {@link DeterministicFunction}
     * @return           an array value created from a generic type list.
     * @param <T>        Integer, Double, Boolean, ...
     */
    public static <T> Value<T[]> createValue(List<T> arr2List, DeterministicFunction generator) {
        if (arr2List.get(0) instanceof Integer)
            return ValueUtils.createValue(arr2List.toArray(Integer[]::new), generator);
        else if (arr2List.get(0) instanceof Double)
            return ValueUtils.createValue(arr2List.toArray(Double[]::new), generator);
        else if (arr2List.get(0) instanceof Boolean)
            return ValueUtils.createValue(arr2List.toArray(Boolean[]::new), generator);
        return ValueUtils.createValue(arr2List.toArray(), generator);
    }

    public static String valueToString(Object value) {

        if (value.getClass().isArray()) {

            Class<?> componentType;
            componentType = value.getClass().getComponentType();

            if (componentType.isPrimitive()) {

                if (boolean.class.isAssignableFrom(componentType)) {
                    return Arrays.toString((boolean[]) value);
                } else if (byte.class.isAssignableFrom(componentType)) {
                    return Arrays.toString((byte[]) value);
                } else if (char.class.isAssignableFrom(componentType)) {
                    return Arrays.toString((char[]) value);
                } else if (double.class.isAssignableFrom(componentType)) {
                    return Arrays.toString((double[]) value);
                } else if (float.class.isAssignableFrom(componentType)) {
                    return Arrays.toString((float[]) value);
                } else if (int.class.isAssignableFrom(componentType)) {
                    return Arrays.toString((int[]) value);
                } else if (long.class.isAssignableFrom(componentType)) {
                    return Arrays.toString((long[]) value);
                } else if (short.class.isAssignableFrom(componentType)) {
                    return Arrays.toString((short[]) value);
                }
                /* No else. No other primitive types exist. */
            } else if (String.class.isAssignableFrom(componentType)) {
                String[] stringArray = (String[]) value;
                StringBuilder builder = new StringBuilder();
                builder.append("[");
                if (stringArray.length > 0) {
                    builder.append(quotedString(stringArray[0]));
                }
                for (int i = 1; i < stringArray.length; i++) {
                    builder.append(", ");
                    builder.append(quotedString(stringArray[i]));
                }
                builder.append("]");
                return builder.toString();
            } else {
                return Arrays.toString((Object[]) value);
            }
        }

        if (value instanceof String) return quotedString(value.toString());

        return value.toString();
    }

    public static String quotedString(String str) {
        return "\"" + str + "\"";
    }

    /**
     * @param var an array of values
     * @return the type of the array if all values are the same type (or null),
     *         or Object if the types are different.
     */
    public static Class<?> getType(Value[] var) {

        if (allNull(var)) return Double.class;
        // Double and Integer must be ahead of number
        if (allAssignableFrom(var, Double.class)) return Double.class;
        if (allAssignableFrom(var, Integer.class)) return Integer.class;

        if (allAssignableFrom(var, Number.class)) return Number.class;
        if (allAssignableFrom(var, Boolean.class)) return Boolean.class;
        if (allAssignableFrom(var, String.class)) return String.class;
        if (allAssignableFrom(var, Double[].class)) return Double[].class;
        if (allAssignableFrom(var, Integer[].class)) return Integer[].class;
        if (allAssignableFrom(var, Boolean[].class)) return Boolean[].class;
        if (allAssignableFrom(var, String[].class)) return String[].class;
        return Object.class;
    }

    /**
     * @param var
     * @return the first non null value in the value array.
     */
    private static boolean allNull(Value[] var) {
        for (Value value : var) {
            if (value != null) return false;
        }
        return true;
    }

    /**
     * @param var
     * @return the first non null value in the value array.
     */
    private static boolean allAssignableFrom(Value[] var, Class superclass) {
        for (Value value : var) {
            if (value != null && !superclass.isAssignableFrom(value.value().getClass())) return false;
        }
        return true;
    }
}
