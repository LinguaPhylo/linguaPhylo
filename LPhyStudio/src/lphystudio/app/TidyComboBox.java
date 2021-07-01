package lphystudio.app;

import javax.swing.*;
import java.awt.*;

public class TidyComboBox<U> extends JComboBox<U> {

    public TidyComboBox(U[] data) {
        super(data);
    }

    /**
     * @inherited <p>
     */
    @Override
    public Dimension getMaximumSize() {
        Dimension max = super.getMaximumSize();
        max.height = getPreferredSize().height;
        max.width = getPreferredSize().width + max.height;
        return max;
    }
}
