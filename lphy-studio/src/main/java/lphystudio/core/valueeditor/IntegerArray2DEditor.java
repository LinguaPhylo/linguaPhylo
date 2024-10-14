package lphystudio.core.valueeditor;

public class IntegerArray2DEditor extends Abstract2DEditor {

    Integer[][] matrix;

    public IntegerArray2DEditor(Integer[][] matrix, boolean editable) {
        this.matrix = matrix;

        draw2DArray(matrix, editable);
    }


    @Override
    protected String elementToString(Object obj) {
        if (obj instanceof Integer d)
            return Integer.toString(d);
        return obj.toString();
    }

    protected String formatMatrixElement(Object element) {
        return elementToString(element);
    }
}
