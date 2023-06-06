package lphy.core.model;

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

}
