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

        Insets insets = getInsets();
        g.translate(insets.left,insets.right);
        int width = getWidth()-insets.left-insets.right;
        int height = getHeight()-insets.top-insets.bottom;

        Graphics2D g2d = (Graphics2D)g;

        double h = height / (double)alignment.n();
        double w = width / (double)alignment.L();

        for (int i = 0; i < alignment.n(); i++) {
            for (int j = 0; j < alignment.L(); j++) {
                g.setColor(colors[alignment.getState(i,j)]);

                Rectangle2D rect2D = new Rectangle2D.Double(j*w, i*h, w, h*0.95);

                g2d.fill(rect2D);
            }
        }
        g.translate(-insets.left,-insets.right);

    }
}
