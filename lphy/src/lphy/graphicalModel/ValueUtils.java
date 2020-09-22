package lphy.graphicalModel;

import lphy.graphicalModel.types.*;

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
        return Number.class.isAssignableFrom(valueClass) || (valueClass.isArray() && Number.class.isAssignableFrom(valueClass.getComponentType()));
    }

    public static boolean isNumber(Value value) {
        Object val = value.value();
        return val instanceof Number;
    }

    /**
     * useful function to get a number value as a double;
     * @param value
     * @return
     */
    public static double doubleValue(Value<Number> value) {
        return value.value().doubleValue();
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

    public static Value createValue(Object value, DeterministicFunction generator) {
        if (value instanceof Integer) return createValue((Integer)value, generator);
        if (value instanceof Integer[]) return createValue((Integer[])value, generator);
        if (value instanceof Double) return createValue((Double)value, generator);
        if (value instanceof Double[]) return createValue((Double[])value, generator);
        if (value instanceof Boolean) return createValue((Boolean)value, generator);
        if (value instanceof Boolean[]) return createValue((Boolean[])value, generator);
        return new Value(null, value, generator);
    }
}
