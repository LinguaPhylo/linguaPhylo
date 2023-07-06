package lphy.core.logger;

import lphy.core.model.Symbols;
import lphy.core.model.Value;
import lphy.core.vectorization.VectorUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static lphy.core.logger.ValueFormatter.Utils.convertToString;

public interface ValueFormatter {

    enum Mode {
        // The value of each replicate should be logged into separate containers (e.g. file).
        VALUE_PER_FILE,
        // The value of each replicate should be logged into separate lines within a single container.
        // Two values cannot go to the same line.
        VALUE_PER_LINE,
        // The value of each replicate should be logged into separate rows as a column in a single container.
        VALUE_PER_CELL
    }

    String getExtension();

    Mode getMode();

    default String[] getValueID(Value<?> value) {
        if (value.value() instanceof Object[][] arr) {
            // flatten
            List<String> names = new ArrayList<>();
            for (int i = 0; i < arr.length; i++) {
                for (int j = 0; j < arr[i].length; j++) {
                    names.add(Symbols.getCanonical(value.getId()) + VectorUtils.INDEX_SEPARATOR +
                            i + VectorUtils.INDEX_SEPARATOR + j);
                }
            }
            return names.toArray(String[]::new);
        } else if (value.value() instanceof Object[] arr) {
            String[] names = new String[arr.length];
            for (int i = 0; i < names.length; i++) {
                names[i] = Symbols.getCanonical(value.getId()) + VectorUtils.INDEX_SEPARATOR + i;
            }
            return names;
        }
        return new String[]{Symbols.getCanonical(value.getId())};
    }

    default String[] header(Value<?> value) {
        return getValueID(value);
    }

    /**
     * @param value can be single value or 1d array or 2d array.
     * @return  the array of formatted string(s) from given value.
     *          If single value, then return one-element array.
     *          If 1d-array value, then return the same length of string array.
     *          If 2d-array value, then flatten to 1d string array.
     */
    default String[] format(Value<?> value) {
        if (value.value() instanceof Object[][] arr) {
            // method overload: flatten 2d => 1d
            return convertToString(arr);
        } else if (value.value() instanceof Object[] arr) {
            return convertToString(arr);
        }
        return new String[]{value.value().toString()};
    }

    default Object[] getValues(Value<?> value) {
        if (value.value() instanceof Object[][] arr) {
            // method overload: flatten 2d => 1d
            return Utils.flatten2d(arr);
        } else if (value.value() instanceof Object[] arr) {
            return arr;
        }
        return new Object[]{value.value()};
    }

    default String[] footer(Value<?> value) {
        return new String[0];
    }

    // overwrite to return "", if no row name.
    default String getRowName(int rowId) {
        return String.valueOf(rowId);
    }

    class Base implements ValueFormatter {
        public Base() { // for getDeclaredConstructor().newInstance()
        }

        @Override
        public String getExtension() {
            return ".log";
        }

        @Override
        public Mode getMode() {
            return Mode.VALUE_PER_CELL;
        }

    }

    class Utils {
        public static Object[] flatten2d(Object[][] array) {
            if (array == null)
                return new Object[0];

            // flatten 2d => 1d
            List<Object> objectList = new ArrayList<>();
            for (Object[] arr1d : array) {
                objectList.addAll(Arrays.asList(arr1d));
            }
            return objectList.toArray(Object[]::new);
        }


        public static String convertToString(Object value) {
            if (value.getClass().isArray() || value instanceof Collection) {
                throw new IllegalArgumentException(value + " cannot be array or collection !");
            }
            return String.valueOf(value);
        }

        public static String[] convertToString(Object[] array) {
            if (array == null)
                return new String[0];

            String[] stringArray = new String[array.length];
            Arrays.setAll(stringArray, i -> convertToString(array[i]));
            return stringArray;
        }

        public static String[] convertToString(Object[][] array) {
            if (array == null)
                return new String[0];

            // flatten 2d => 1d
            List<String> strList = new ArrayList<>();
            for (Object[] arr1d : array) {
                String[] strs = convertToString(arr1d);
                strList.addAll(Arrays.asList(strs));
            }
            return strList.toArray(String[]::new);
        }
    }

}
