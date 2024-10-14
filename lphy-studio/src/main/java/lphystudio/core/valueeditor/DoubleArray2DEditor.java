package lphystudio.core.valueeditor;

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

    protected String formatMatrixElement(Object element) {
        String str = elementToString(element);
        if (str == null || str.isEmpty() || str.equals("null"))
            return "null";
        return format.format(Double.parseDouble(str));
    }

}
