package lphystudio.core.swing;

import java.awt.*;

public class DataDiamondButton extends DiamondButton {

    public DataDiamondButton(String text) {
        super(text, Color.orange, Color.darkGray);
    }

    @Override
    public void paintComponent(Graphics g) {

        ((Graphics2D)g).setStroke(new BasicStroke(2.0f));
        super.paintComponent(g);
        ((Graphics2D)g).setStroke(new BasicStroke(1.0f));

        //g.setColor(Color.lightGray);
        //g.drawString("data", 0, 0);
    }
}