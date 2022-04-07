package lphystudio.app.graphicalmodelpanel;

import javax.swing.*;
import java.awt.*;

public class TidyComboBox<U> extends JComboBox<U> {

    public TidyComboBox(U[] data) {
        super(data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dimension getMaximumSize() {
        Dimension max = super.getMaximumSize();
        max.height = getPreferredSize().height;
        max.width = getPreferredSize().width + max.height;
        return max;
    }
}
