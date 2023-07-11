//package lphy.core.logger;
//
//import lphy.core.model.Symbols;
//import lphy.core.model.Value;
//import lphy.core.vectorization.VectorUtils;
//import lphy.core.vectorization.VectorizedRandomVariable;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collection;
//import java.util.List;
//
//public class ArrayValueFormatterUtils {
//
//
//    public static List<String> getValueID(Value value) {
//        List<String> ids = new ArrayList<>();
//
//        // VectorizedRandomVariable.value() is array
////        if (value instanceof VectorizedRandomVariable vectRandVar) {
////            // VectorizedRandomVariable
////            for (int i = 0; i < vectRandVar.size(); i++) {
////                // make sure to populate the vectorized values into different List<Value>
////                ids.add(Symbols.getCanonical(vectRandVar.getId()) + VectorUtils.INDEX_SEPARATOR + i);
////            }
////        } else
//        if (value.value() instanceof Object[][] arr) {
//            // flatten
//            for (int i = 0; i < arr.length; i++) {
//                for (int j = 0; j < arr[i].length; j++) {
//                    ids.add(Symbols.getCanonical(value.getId()) + VectorUtils.INDEX_SEPARATOR +
//                            i + VectorUtils.INDEX_SEPARATOR + j);
//                }
//            }
//        } else if (value.value() instanceof Object[] arr) {
//            for (int i = 0; i < arr.length; i++) {
//                ids.add(Symbols.getCanonical(value.getId()) + VectorUtils.INDEX_SEPARATOR + i);
//            }
//        } else
//            ids.add(Symbols.getCanonical(value.getId()));
//
//
//        return ids;
//    }
//
//    public static String[] getValueIDs(List<Value> values) {
//        List<String> allIds = new ArrayList<>();
//
//        for (Value v : values) {
//            List<String> ids = getValueID(v);
//            allIds.addAll(ids);
//        }
//
//        return allIds.toArray(String[]::new);
//    }
//
//
//    public static List<Object> getValue(Value value) {
//        List<Object> valueVals = new ArrayList<>();
//
//        // VectorizedRandomVariable.value() is array
////        if (value instanceof VectorizedRandomVariable vectRandVar) {
////            // VectorizedRandomVariable
////            for (int i = 0; i < vectRandVar.size(); i++) {
////                // make sure to populate the vectorized values into different List<Value>
////                valueVals.add(vectRandVar.getComponentValue(i).value());
////            }
////        } else
//        if (value.value() instanceof Object[][] arr) {
//            // method overload: flatten 2d => 1d
//            for (Object[] arr1d : arr)
//                valueVals.addAll(Arrays.asList(arr1d));
//        } else if (value.value() instanceof Object[] arr) {
//            valueVals.addAll(Arrays.asList(arr));
//        }
//        valueVals.add(value.value());
//
//        return valueVals;
//    }
//
//
//    public static Object[] getValues(List<Value> values) {
//        List<Object> allValueVals = new ArrayList<>();
//        for (Value v : values) {
//            List<Object> valueVals = getValue(v);
//            allValueVals.addAll(valueVals);
//        }
//        return allValueVals.toArray(Object[]::new);
//    }
//
//
//    /**
//     * @param value can be single value or 1d array or 2d array.
//     * @return the array of formatted string(s) from given value.
//     * If single value, then return one-element array.
//     * If 1d-array value, then return the same length of string array.
//     * If 2d-array value, then flatten to 1d string array.
//     */
//
//
//    public static class Utils {
//        public static Object[] flatten2d(Object[][] array) {
//            if (array == null)
//                return new Object[0];
//
//            // flatten 2d => 1d
//            List<Object> objectList = new ArrayList<>();
//            for (Object[] arr1d : array) {
//                objectList.addAll(Arrays.asList(arr1d));
//            }
//            return objectList.toArray(Object[]::new);
//        }
//
//
//        public static String convertToString(Object value) {
//            if (value.getClass().isArray() || value instanceof Collection) {
//                throw new IllegalArgumentException(value + " cannot be array or collection !");
//            }
//            return String.valueOf(value);
//        }
//
//        public static String[] convertToString(Object[] array) {
//            if (array == null)
//                return new String[0];
//
//            String[] stringArray = new String[array.length];
//            Arrays.setAll(stringArray, i -> convertToString(array[i]));
//            return stringArray;
//        }
//
//        public static String[] convertToString(Object[][] array) {
//            if (array == null)
//                return new String[0];
//
//            // flatten 2d => 1d
//            List<String> strList = new ArrayList<>();
//            for (Object[] arr1d : array) {
//                String[] strs = convertToString(arr1d);
//                strList.addAll(Arrays.asList(strs));
//            }
//            return strList.toArray(String[]::new);
//        }
//    }
//}
