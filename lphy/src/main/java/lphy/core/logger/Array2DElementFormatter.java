package lphy.core.logger;

import lphy.core.model.Symbols;
import lphy.core.vectorization.VectorUtils;

import java.util.Objects;

/**
 * The 2d array case for the implementation of ValueFormatter.
 *
 * @param <T>
 */
public class Array2DElementFormatter<T> implements ValueFormatter<T[][]> {

    final String elementValueId; // it is the original array value ID before decomposition
    final ValueFormatter<T> valueFormatter;
    // array2d[rowIndex][colIndex]
    final int rowIndex;
    final int colIndex;

    /**
     * Decompose array into elements before this, and use this constructor to wrap
     * the ValueFormatter of the single element. Therefore, the implementation of
     * ValueFormatter will stay in the business to process single element only.
     * @param arrayValueID     the original id of array value.
     * @param valueFormatter   created from the ith-jth element decomposed from the 2d array,
     *                         which should contain the value of the element.
     * @param rowIndex         the row index of 2d array for this element.
     * @param colIndex         the column index of 2d array for this element.
     */
    public Array2DElementFormatter(String arrayValueID, ValueFormatter<T> valueFormatter,
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

    /**
     * @param value  It is a 2d array from {@link lphy.core.model.Value#value()}
     * @return   the formatted string of the ith-jth element in the given array value.
     */
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
