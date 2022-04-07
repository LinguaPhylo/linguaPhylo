package lphystudio.app.viewer;

import lphy.graphicalModel.Vector;

import javax.swing.*;

public class VectorValueViewer implements Viewer {

    @Override
    public boolean match(Object value) {
        return value instanceof Vector;
    }

    public JComponent getViewer(Object value) {
        return new VectorComponent((Vector)value);
    }
}
