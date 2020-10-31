package lphy.app;

import lphy.graphicalModel.Value;
import lphy.graphicalModel.Vector;
import lphy.graphicalModel.types.VectorValue;

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
