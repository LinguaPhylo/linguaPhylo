package lphystudio.core.valueeditors;

import java.text.DecimalFormat;

public class DoubleArray2DEditor extends Abstract2DEditor {

    Double[][] matrix;

    int maxFracDigits = 5;

    DecimalFormat format = new DecimalFormat();

    public DoubleArray2DEditor(Double[][] matrix, boolean editable) {
        this.matrix = matrix;
        format.setMaximumFractionDigits(maxFracDigits);

        draw2DArray(matrix, editable);
    }

    @Override
    protected String elementToString(Object obj) {
        if (obj instanceof Double d)
            return Double.toString(d);
        return obj.toString();
    }

    protected String formatMatrixElement(Object obj) {
        return format.format(obj);
    }

}
