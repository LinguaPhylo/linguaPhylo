package lphy.core.model;

import lphy.core.model.datatype.StringArrayValue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ValueUtils {

    public static boolean isValueOfDeterministicFunction(Value value) {
        return !VariableUtils.isRandomVariable(value) && value.getGenerator() != null;
    }

    public static boolean isFixedValue(Value value) {
        return value.getGenerator() == null && !(value instanceof RandomVariable);
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

    // static boolean isInteger(String s) is moved to ExpressionUtils

    public static boolean isMultiDimensional(Object v) {
        return (v instanceof MultiDimensional || v instanceof Map || v.getClass().isArray() || v instanceof List<?>);
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

    public static double sum(Value<? extends Number[]> value) {
        Number[] num = value.value();
        double sum = 0;
        for (Number number : num) {
            sum += number.doubleValue();
        }
        return sum;
    }

    public static double[][] double2DArrayValue(Value<Number[][]> value) {
        Number[][] num = value.value();
        double[][] values = new double[num.length][num[0].length];
        for (int i = 0; i < num.length; i++) {
            for (int j = 0; j < num[0].length; j++) {
                values[i][j] = num[i][j].doubleValue();
            }
        }
        return values;
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
                    builder.append(StringArrayValue.quotedString(stringArray[0]));
                }
                for (int i = 1; i < stringArray.length; i++) {
                    builder.append(", ");
                    builder.append(StringArrayValue.quotedString(stringArray[i]));
                }
                builder.append("]");
                return builder.toString();
            } else {
                return Arrays.toString((Object[]) value);
            }
        }

        if (value instanceof String) return StringArrayValue.quotedString(value.toString());

        return value.toString();
    }
}
