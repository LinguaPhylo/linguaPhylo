package lphy.core.parser.graphicalmodel;

import lphy.core.model.Value;
import lphy.core.vectorization.array.*;

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
     * Builds the value of an array literal such as {@code [1, 2, 3]} from its element values, the
     * way the LPhy parser does: picks the array function by {@link #getType(Value[])} and applies it.
     * A 1D array of constants loses its function, so that it stays one constant in the graphical model.
     * Shared by {@code LPhyListenerImpl} and lphy-phylospec's converter.
     * @param var the element values
     * @return the array value
     */
    public static Value createArrayValue(Value[] var) {
        Class<?> type = getType(var);

        Value res;
        // if all values null assume double array
        if (type == Double.class) {
            // It is against the principle to create new Value, which could lose
            // their generator. Handle var[i] == null in DoubleArray apply()
            DoubleArray doubleArray = new DoubleArray(var);
            res = doubleArray.apply();
        } else if (type == Double[].class) {
            //TODO not sure how to do properly for 2d ?
            DoubleArray2D doubleArray2D = new DoubleArray2D(var);
            return doubleArray2D.apply();

        } else if (type == Integer[].class) {
            IntegerArray2D integerArray2D = new IntegerArray2D(var);
            return integerArray2D.apply();

        } else if (type == Integer.class) {
            IntegerArray intArray = new IntegerArray(var);
            res = intArray.apply();

        } else if (type == Boolean[].class) {
            BooleanArray2D booleanArray2D = new BooleanArray2D(var);
            return booleanArray2D.apply();

        } else if (type == Boolean.class) {
            BooleanArray booleanArray = new BooleanArray(var);
            res = booleanArray.apply();

        } else if (type == String[].class) {
            StringArray2D stringArray2D = new StringArray2D(var);
            return stringArray2D.apply();

        } else if (type == String.class) {
            StringArray stringArray = new StringArray(var);
            res = stringArray.apply();

        } else if (type == Number[].class) {
            NumberArray2D numberArray2D = new NumberArray2D(var);
            return numberArray2D.apply();

        } else if (type == Number.class) {
            NumberArray numberArray = new NumberArray(var);
            res = numberArray.apply();

        } else if (type == Object[].class) {
            ObjectArray2D objectArray2D = new ObjectArray2D(var);
            return objectArray2D.apply();

        } else {
            // handle generic value array construction
            ObjectArray objectArray = new ObjectArray(var);
            res = objectArray.apply();
        }

        // It is necessary here to set generator to null, in order to avoid break constant array into pieces,
        // because doubleArray function will display in the graphical model.
        if (allConstants(var))
            res.setFunction(null);
        return res;
    }

    /**
     * @param var
     * @return true if all values are null or constant.
     */
    private static boolean allConstants(Value[] var) {
        for (Value v : var) {
            if (v != null && !v.isConstant()) return false;
        }
        return true;
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
