package lphystudio.core.swing;

import javax.swing.*;
import java.awt.*;

public class TidyTextField extends JTextField {

    public TidyTextField(String text, int columns) {
        super(text, columns);
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
