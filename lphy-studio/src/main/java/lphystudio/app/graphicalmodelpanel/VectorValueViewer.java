package lphystudio.app.graphicalmodelpanel;

import lphy.core.model.datatype.Vector;
import lphystudio.app.graphicalmodelpanel.viewer.Viewer;

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
