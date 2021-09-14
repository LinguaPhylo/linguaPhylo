package lphystudio.app;

import lphy.graphicalModel.Value;

import javax.swing.*;

public interface Viewer<T> {

    /**
     * @param value
     * @return true if this viewer can be used for the given value
     */
    boolean match(T value);

    /**
     * @param value
     * @return a viewer for the given value.
     */
    JComponent getViewer(T value);
}
