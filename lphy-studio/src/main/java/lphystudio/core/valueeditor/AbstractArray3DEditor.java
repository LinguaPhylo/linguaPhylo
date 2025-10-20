package lphystudio.core.valueeditor;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractArray3DEditor<T> extends JPanel {
    protected final boolean editable;
    protected final JSlider zSlider;
    protected final T[][][] matrix3D;
    protected Abstract2DEditor<T> current2DView;

    public AbstractArray3DEditor(T[][][] matrix3D, boolean editable) {
        super(new BorderLayout());
        this.editable = editable;
        this.matrix3D = matrix3D;

        // Create slider to switch between layers (z-axis)
        int depth = matrix3D.length;
        zSlider = new JSlider(0, depth - 1, 0);
        zSlider.setMajorTickSpacing(1);
        zSlider.setSnapToTicks(true);  // ensures it stops exactly on integers
        zSlider.setPaintTicks(true);
        zSlider.setPaintLabels(true);

        add(zSlider, BorderLayout.SOUTH);

        // Listen to slider changes to update 2D view
        zSlider.addChangeListener(e -> {
            int z = zSlider.getValue();
            updateSlice(z);
        });

    }

    protected abstract void updateSlice(int z);

}
