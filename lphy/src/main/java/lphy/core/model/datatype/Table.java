package lphy.core.model.datatype;

import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.MethodInfo;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Store everything as String. Use functions to cast the type.
 * @author Walter Xie
 */
public class Table extends LinkedHashMap<String, List<String>> {

    @MethodInfo(description="Return the array of values given a column name.",
            category = GeneratorCategory.TAXA_ALIGNMENT)
    public String[] getColumn(String columnName) {
        if (!containsKey(columnName))
            throw new IllegalArgumentException("The column name (" + columnName +
                    ") does not exist in the Table ");
        return get(columnName).toArray(new String[0]);
    }

    @MethodInfo(description="Return the array of ith column values, where i starts from 0.",
            category = GeneratorCategory.TAXA_ALIGNMENT)
    public String[] getColumn(Integer i) {
        List<List<String>> cols = values().stream().toList();
        return cols.get(i).toArray(new String[0]);
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


}
