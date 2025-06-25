package lphy.core.logger;

import lphy.core.model.Symbols;
import lphy.core.vectorization.VectorUtils;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * The 1d array case for the implementation of ValueFormatter.
 *
 * @param <T>
 */
public class ArrayElementFormatter<T> implements ValueFormatter<T[]> {

    final String elementValueId; // it is the original array value ID before decomposition
    final ValueFormatter<T> valueFormatter;
    final int arrayIndex;

    //TODO better structure ?

    /**
     * Decompose array into elements before this, and use this constructor to wrap
     * the ValueFormatter of the single element. Therefore, the implementation of
     * ValueFormatter will stay in the business to process single element only.
     * @param arrayValueID     the original id of array value.
     * @param valueFormatter   created from the ith element decomposed from the array,
     *                         which should contain the value of the element.
     * @param i                the element index of array.
     */
    public ArrayElementFormatter(String arrayValueID, ValueFormatter<T> valueFormatter, int i) {
        this.elementValueId = getElementValueId(arrayValueID, i);
        this.valueFormatter = valueFormatter;
        arrayIndex = i;
    }

    public static String getElementValueId(String arrayValueID, int arrayIndex) {
        return Symbols.getCanonical(arrayValueID) + VectorUtils.INDEX_SEPARATOR + arrayIndex;
    }

    @Override
    public void writeToFile(BufferedWriter writer, T[] value) {
        try {
            writer.write(format(value));
        } catch(IOException ex) {
            ex.printStackTrace();
        }
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
     * @param value  It is a 1d array from {@link lphy.core.model.Value#value()}
     * @return   the formatted string of the ith element in the given array value.
     */
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
