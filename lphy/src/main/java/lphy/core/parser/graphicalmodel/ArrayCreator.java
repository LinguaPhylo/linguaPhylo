package lphy.core.parser.graphicalmodel;

import lphy.core.model.Value;

public class ArrayCreator {


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
