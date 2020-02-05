package james.graphicalModel;

public class ValueUtils {

    public static boolean isValueOfFunction(Value value) {
        return value.getFunction() != null;
    }

    public static boolean isFixedValue(Value value) {
        return value.getFunction() == null && !(value instanceof RandomVariable);
    }

    public static boolean isRandomVariable(Value value) {
        return value instanceof RandomVariable;
    }
}
