package lphystudio.core.valueeditor;

public class BooleanArray2DEditor extends Abstract2DEditor<Boolean> {

    Boolean[][] matrix;

    public BooleanArray2DEditor(Boolean[][] matrix, boolean editable) {
        this.matrix = matrix;

        draw2DArray(matrix, editable);
    }

    protected String formatMatrixElement(Boolean element) {
        return elementToString(element);
    }
}
