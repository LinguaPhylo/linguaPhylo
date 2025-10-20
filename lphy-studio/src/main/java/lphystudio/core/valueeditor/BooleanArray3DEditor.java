package lphystudio.core.valueeditor;


import java.awt.*;

public class BooleanArray3DEditor extends AbstractArray3DEditor<Boolean> {

    public BooleanArray3DEditor(Boolean[][][] matrix3D, boolean editable) {
        super(matrix3D, editable);

        // Create first 2D view (z = 0)
        current2DView = new BooleanArray2DEditor(matrix3D[0], editable);
        // Layout: matrix display + slider
        add(current2DView, BorderLayout.CENTER);
    }

    protected void updateSlice(int z) {
        // Remove old 2D view and replace it with a new one
        remove(current2DView);
        current2DView = new BooleanArray2DEditor(matrix3D[z], editable);
        add(current2DView, BorderLayout.CENTER);
        revalidate();
        repaint();
    }
}
