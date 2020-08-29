package lphy.graphicalModel;

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

    /**
     * useful function to get a number value as a double;
     * @param value
     * @return
     */
    public static double doubleValue(Value<Number> value) {
        return value.value().doubleValue();
    }
}
