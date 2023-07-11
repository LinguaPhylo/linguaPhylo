package lphy.core.logger;

import lphy.core.model.Symbols;
import lphy.core.vectorization.VectorUtils;

import java.util.Objects;

public class Array2DValueFormatter<T> implements ValueFormatter<T[][]> {

    final String elementValueId; // it is the original array value ID before decomposition
    final ValueFormatter<T> valueFormatter;
    // array2d[rowIndex][colIndex]
    final int rowIndex;
    final int colIndex;

    //TODO incomplete
    public Array2DValueFormatter(String arrayValueID, ValueFormatter<T> valueFormatter,
                                 int rowIndex, int colIndex) {
        this.elementValueId = getElementValueId(arrayValueID, rowIndex, colIndex);
        this.valueFormatter = valueFormatter;
        this.rowIndex = rowIndex;
        this.colIndex = colIndex;
    }

    public static String getElementValueId(String arrayValueID, int rowIndex, int colIndex) {
        return Symbols.getCanonical(arrayValueID) + VectorUtils.INDEX_SEPARATOR + rowIndex +
                VectorUtils.INDEX_SEPARATOR + colIndex;
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
    public String format(T[][] value) {
        if (rowIndex >= value.length || colIndex >= value[0].length)
            throw new IllegalArgumentException("Invalid array index : row index " + rowIndex +
                    " must < length " + Objects.requireNonNull(value).length + ", col index " + colIndex +
                    " must < length " + Objects.requireNonNull(value[0]).length + " at value " + elementValueId);
        return valueFormatter.format(value[rowIndex][colIndex]);
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
