package james.swing;

import james.core.Alignment;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;

public class AlignmentComponent extends JComponent {

    public static Color[] DNA_COLORS = {Color.red, Color.blue, Color.black, Color.green};

    Color[] colors;
    Alignment alignment;

    public AlignmentComponent(Alignment alignment, Color[] colors) {
        this.colors = colors;
        this.alignment = alignment;
    }

    public void paintComponent(Graphics g) {

        double h = getHeight() / (double)alignment.n();
        double w = getWidth() / (double)alignment.L();

        Graphics2D g2D = (Graphics2D)g;

        for (int i = 0; i < alignment.n(); i++) {
            for (int j = 0; j < alignment.L(); j++) {
                g.setColor(colors[alignment.getState(i,j)]);

                Rectangle2D rect2D = new Rectangle2D.Double(j*w, i*h, w, h);

                g2D.fill(rect2D);
            }
        }

    }
}
