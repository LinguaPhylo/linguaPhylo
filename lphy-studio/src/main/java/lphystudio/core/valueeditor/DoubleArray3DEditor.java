package lphystudio.core.valueeditor;


import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class DoubleArray3DEditor extends JPanel {

    private final Double[][][] matrix3D;
    private final boolean editable;
    private final JSlider zSlider;
    private DoubleArray2DEditor current2DView;

    public DoubleArray3DEditor(Double[][][] matrix3D, boolean editable) {
        super(new BorderLayout());
        this.matrix3D = matrix3D;
        this.editable = editable;

        // Create slider to switch between layers (z-axis)
        int depth = matrix3D.length;
        zSlider = new JSlider(0, depth - 1, 0);
        zSlider.setMajorTickSpacing(1);
        zSlider.setSnapToTicks(true);  // ensures it stops exactly on integers
        zSlider.setPaintTicks(true);
        zSlider.setPaintLabels(true);

        // Create first 2D view (z = 0)
        current2DView = new DoubleArray2DEditor(matrix3D[0], editable);

        // Layout: matrix display + slider
        add(current2DView, BorderLayout.CENTER);
        add(zSlider, BorderLayout.SOUTH);

        // Listen to slider changes to update 2D view
        zSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int z = zSlider.getValue();
                updateSlice(z);
            }
        });
    }

    private void updateSlice(int z) {
        // Remove old 2D view and replace it with a new one
        remove(current2DView);
        current2DView = new DoubleArray2DEditor(matrix3D[z], editable);
        add(current2DView, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    public int getCurrentZ() {
        return zSlider.getValue();
    }
}
