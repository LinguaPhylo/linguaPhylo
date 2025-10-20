package lphystudio.core.valueeditor;

public class IntegerArray2DEditor extends Abstract2DEditor<Integer> {

    Integer[][] matrix;

    public IntegerArray2DEditor(Integer[][] matrix, boolean editable) {
        this.matrix = matrix;

        draw2DArray(matrix, editable);
    }


//    @Override
//    protected String elementToString(Integer element) {
//        if (element instanceof Integer d)
//            return Integer.toString(d);
//        return Integer.toString(element);
//    }

    protected String formatMatrixElement(Integer element) {
        return elementToString(element);
    }
}
