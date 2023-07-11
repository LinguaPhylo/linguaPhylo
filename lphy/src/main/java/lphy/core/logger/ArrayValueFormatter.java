package lphy.core.logger;

import lphy.core.model.Symbols;
import lphy.core.vectorization.VectorUtils;

public class ArrayValueFormatter<T> implements ValueFormatter<T[]> {

    final String elementValueId; // it is the original array value ID before decomposition
    final ValueFormatter<T> valueFormatter;
    final int arrayIndex;

    //TODO incomplete
    public ArrayValueFormatter(String arrayValueID, ValueFormatter<T> valueFormatter, int i) {
        this.elementValueId = getElementValueId(arrayValueID, i);
        this.valueFormatter = valueFormatter;
        arrayIndex = i;
    }

    public static String getElementValueId(String arrayValueID, int arrayIndex) {
        return Symbols.getCanonical(arrayValueID) + VectorUtils.INDEX_SEPARATOR + arrayIndex;
    }

    @Override
    public String getExtension() {
        return valueFormatter.getExtension();
    }

    @Override
    public Mode getMode() {
        return valueFormatter.getMode();
    }

    @Override
    public Class getDataTypeClass() {
        return valueFormatter.getDataTypeClass();
    }

    @Override
    public String header() {
//        if (this.arrayValueID == null)
//            setValueID(id);
        // pass the array element id to valueFormatter
        return valueFormatter.header();
    }

    @Override
    public String getValueID() {
        return elementValueId;
    }

    @Override
    public String format(T[] value) {
        if (arrayIndex >= value.length)
            throw new IllegalArgumentException("Invalid array index : " + arrayIndex +
                    " must < length " + value.length + " at value " + elementValueId);
        return valueFormatter.format(value[arrayIndex]);
    }

    @Override
    public String footer() {
        return valueFormatter.footer();
    }

    @Override
    public String getRowName(int rowId) {
        return valueFormatter.getRowName(rowId);
    }

}
