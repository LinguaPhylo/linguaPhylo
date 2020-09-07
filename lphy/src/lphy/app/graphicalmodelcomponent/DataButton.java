package lphy.app.graphicalmodelcomponent;

import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DataButton extends SquareButton {

    public DataButton(String text) {

        super(text, Color.orange, Color.darkGray);
        setBorder(new EmptyBorder(12,0,0,0));

    }

    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);

        g.setColor(Color.lightGray);
        g.drawString("data", 0, 0);
    }
}