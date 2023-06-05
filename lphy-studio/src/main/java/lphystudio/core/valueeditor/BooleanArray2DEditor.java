package lphystudio.core.valueeditor;

public class BooleanArray2DEditor extends Abstract2DEditor {

    Boolean[][] matrix;

    public BooleanArray2DEditor(Boolean[][] matrix, boolean editable) {
        this.matrix = matrix;

        draw2DArray(matrix, editable);
    }


    @Override
    protected String elementToString(Object obj) {
        if (obj instanceof Integer d)
            return Integer.toString(d);
        return obj.toString();
    }

    protected String formatMatrixElement(Object obj) {
        return elementToString(obj);
    }
}
