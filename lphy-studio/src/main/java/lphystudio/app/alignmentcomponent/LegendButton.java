package lphystudio.app.alignmentcomponent;

import lphystudio.core.swing.SquareButton;

import java.awt.*;

/**
 * adjust text colour by background
 * @author Walter Xie
 */
public class LegendButton extends SquareButton {

    final float DARK_THRESHOLD = -5000000f; // empirical threshold
    final float BRIGHT_THRESHOLD = -10000f;

    public LegendButton(String text, Color backgroundColor) {
        super(text, backgroundColor, Color.white);
    }

    @Override
    public void paintComponent(Graphics g) {
        int rgbCl = backgroundColor.getRGB();
        // higher RGB darker
        if (rgbCl < DARK_THRESHOLD)
            // set text colour
            setForeground(Color.white);
//        else if (rgbCl > BRIGHT_THRESHOLD)
//            setForeground(Color.lightGray);
        else
            setForeground(Color.black);

        super.paintComponent(g);
    }

}
