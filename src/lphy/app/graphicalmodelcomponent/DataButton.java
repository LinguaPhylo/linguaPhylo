package lphy.app.graphicalmodelcomponent;

import java.awt.*;

public class DataButton extends SquareButton {

    public DataButton(String text) {

        super(text, Color.orange, Color.darkGray);

    }

    @Override
    public void paintComponent(Graphics g) {

        ((Graphics2D)g).setStroke(new BasicStroke(2.0f));
        super.paintComponent(g);
        ((Graphics2D)g).setStroke(new BasicStroke(1.0f));

        //g.setColor(Color.lightGray);
        //((Graphics2D)g).drawString("data", (float)origin.getX(), (float)origin.getY()+10.0f);
    }
}