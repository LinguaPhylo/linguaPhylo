package lphy.core.model.datatype;

import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.MethodInfo;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Store everything as String. Use functions to cast the type.
 * @author Walter Xie
 */
public class Table extends LinkedHashMap<String, List> {

    @MethodInfo(description="Return the column values given a column name.",
            category = GeneratorCategory.TAXA_ALIGNMENT)
    public List getColumn(String columnName) {
        return get(columnName);
    }

    @MethodInfo(description="Return the ith column values given its index which starts from 0.",
            category = GeneratorCategory.TAXA_ALIGNMENT)
    public List getColumn(Integer i) {
        Object columnName = keySet().toArray()[i];
        return get(columnName);
    }

    @MethodInfo(description="Return ith column name, where i starts from 0.",
            category = GeneratorCategory.TAXA_ALIGNMENT)
    public String getColumnName(Integer i) {
        return getColumnNames()[i];
    }

    @MethodInfo(description="Return the array of column names.",
            category = GeneratorCategory.TAXA_ALIGNMENT)
    public String[] getColumnNames() {
        return keySet().toArray(new String[0]);
    }

    @MethodInfo(description = "return the array of (array) records for selected column indices.")
    public Double[][] getColumnAsMatrix(Integer... ArrayIndex) {

        List[] columns = new List[ArrayIndex.length];
        for (int i = 0; i < ArrayIndex.length; i++) {
            columns[i] = getColumn(ArrayIndex[i]);
        }

        Double[][] anglesMatrix = new Double[columns[0].size()][ArrayIndex.length];

        for (int i = 0; i < anglesMatrix.length; i++) {
            for (int j = 0; j < anglesMatrix[i].length; j++) {
                anglesMatrix[i][j] = Double.parseDouble(columns[j].get(i).toString());
            }
        }
        return anglesMatrix;
    }

    //TODO set column type ?


    /**
     * @param str  value in string
     * @return   the cast value in the following types by order: Boolean, Integer, Double, String
     */
    public static Object getValueGuessType(String str) {
        // TODO what about 0 or 1 only?
        if (str.equalsIgnoreCase("true") || str.equalsIgnoreCase("false"))
            return Boolean.parseBoolean(str);
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException ei) {
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException ed) {
                return str;
            }
        }

    }


//    @MethodInfo(description="Return the array of values given a column name.",
//            category = GeneratorCategory.TAXA_ALIGNMENT)
//    public <T> T[] getColumn(String columnName, Class<T> cls) {
//        if (!containsKey(columnName)) {
//            throw new IllegalArgumentException("The column name (" + columnName + ") does not exist in the table.");
//        }
//
//        List<String> original = get(columnName);
//        List<T> converted;
//
//        if (cls.equals(Boolean.class)) {
//            converted = (List<T>) original.stream().map(Boolean::parseBoolean).collect(Collectors.toList());
//        } else if (cls.equals(Integer.class)) {
//            converted = (List<T>) original.stream().map(Integer::parseInt).collect(Collectors.toList());
//        } else if (cls.equals(Double.class)) {
//            converted = (List<T>) original.stream().map(Double::parseDouble).collect(Collectors.toList());
//        } else {
//            // No explicit parsing needed for String class
//            converted = (List<T>) original;
//        }
//
//        T[] array = (T[]) Array.newInstance(cls, converted.size());
//        return converted.toArray(array);
//    }


//    public static Class guessType(String str) {
//        // TODO what about 0 or 1 only?
//        if (str.equalsIgnoreCase("true") || str.equalsIgnoreCase("false"))
//            return Boolean.class;
//
//        try {
//            Integer.parseInt(str);
//            return Integer.class;
//        } catch (NumberFormatException ei) {
//            try {
//                Double.parseDouble(str);
//                return Double.class;
//            } catch (NumberFormatException ed) {
//                return String.class;
//            }
//        }
//
//    }

//    public enum ColumnType {
//        BOOLEAN,
//        INTEGER,
//        DOUBLE,
//        STRING
//    }
}
