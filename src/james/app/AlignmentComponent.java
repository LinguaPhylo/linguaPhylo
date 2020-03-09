package james.app;

import james.TimeTree;
import james.TimeTreeComponent;
import james.core.Alignment;
import james.core.ErrorAlignment;
import james.core.PhyloCTMC;
import james.graphicalModel.GenerativeDistribution;
import james.graphicalModel.RandomVariable;
import james.graphicalModel.Value;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.prefs.Preferences;

import static james.app.Utils.MAX_FONT_SIZE;
import static james.app.Utils.MIN_FONT_SIZE;

public class AlignmentComponent extends JComponent {

    static Preferences preferences = Preferences.userNodeForPackage(AlignmentComponent.class);

    public static Color[] DNA_COLORS = {Color.red, Color.blue, Color.black, Color.green};

    public static Color[] BINARY_COLORS = {Color.red, Color.blue};


    Color[] colors;
    Value<Alignment> alignmentValue;
    Alignment alignment;

    Value<TimeTree> timeTree = null;

    int spacer = 5;

    static boolean showErrorsIfAvailable = true;

    public AlignmentComponent(Value<Alignment> av, Color[] colors) {
        this.colors = colors;
        this.alignmentValue = av;
        this.alignment = av.value();

        if (av instanceof RandomVariable) {
            GenerativeDistribution gen = ((RandomVariable)av).getGenerativeDistribution();
            if (gen instanceof PhyloCTMC) {
                timeTree = ((PhyloCTMC) gen).getParams().get("tree");
            }
        }

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                if (e.getButton() == MouseEvent.BUTTON3) {
                    setShowTreeInAlignmentViewerIfAvailable(!getShowTreeInAlignmentViewerIfAvailable());
                } else {
                    showErrorsIfAvailable = !showErrorsIfAvailable;
                }
                repaint();
            }
        });

        int desktopWidth = Toolkit.getDefaultToolkit().getScreenSize().width;

        int maximumWidth = Math.min(desktopWidth,MAX_FONT_SIZE*alignment.L());
        int maximumHeight = MAX_FONT_SIZE*alignment.n();
        int minimumHeight = MIN_FONT_SIZE*alignment.n();

        setMaximumSize(new Dimension(maximumWidth, maximumHeight));
        setMinimumSize(new Dimension(100, minimumHeight));
    }

    static boolean getShowTreeInAlignmentViewerIfAvailable() {
        return preferences.getBoolean("showTreeInAlignmentViewerIfAvailable", true);
    }

    static void setShowTreeInAlignmentViewerIfAvailable(boolean showTreeInAlignmentViewerIfAvailable) {
        preferences.putBoolean("showTreeInAlignmentViewerIfAvailable", showTreeInAlignmentViewerIfAvailable);
    }

    public void paintComponent(Graphics g) {

        Insets insets = getInsets();
        g.translate(insets.left, insets.top);
        int width = getWidth() - insets.left - insets.right;
        int height = getHeight() - insets.top - insets.bottom;

        Graphics2D g2d = (Graphics2D) g;

        int xdelta = 0;

        double h = height / (double) alignment.n();

        Font f = g.getFont();
        if (h<9) {
            g.setFont(f.deriveFont(8.0f));
        } else if (h < 10) {
            g.setFont(f.deriveFont(9.0f));
        } else if (h < 11) {
            g.setFont(f.deriveFont(10.0f));
        } else if (h < 12) {
            g.setFont(f.deriveFont(11.0f));
        } else {
            g.setFont(f.deriveFont(12.0f));
        }

        if (isShowingTree()) {

            int ytrans = (int)Math.round(h/2);

            int treeHeight = (int)Math.round(height - h);
            g.translate(0, ytrans);

            TimeTreeComponent treeComponent = new TimeTreeComponent(timeTree.value());
            treeComponent.setBorder(BorderFactory.createEmptyBorder(1,1,1,0));
            treeComponent.setSize(width/2, treeHeight);
            treeComponent.paintComponent(g);
            width /= 2;
            xdelta = width;
            g.translate(0, -ytrans);
        }

        int maxWidth = 0;
        int[] sWidth = new int[alignment.n()];

        for (int i = 0; i < alignment.n(); i++) {

            sWidth[i] = g.getFontMetrics().stringWidth(alignment.getId(i));
            if (sWidth[i] > maxWidth) maxWidth = sWidth[i];
        }

        double w = (width - maxWidth - spacer) / (double) alignment.L();

        int ascent = g.getFontMetrics().getAscent();
        double ydelta = (h - ascent) / 2.0 + ascent;

        for (int i = 0; i < alignment.n(); i++) {
            double y = i * h;

            if (!isShowingTree()) {
                g.setColor(Color.black);
                g.drawString(alignment.getId(i),maxWidth-sWidth[i]+xdelta,(int)Math.round(y+ydelta));
            }

            for (int j = 0; j < alignment.L(); j++) {

                Color c = colors[alignment.getState(i, j)];

                if (alignment instanceof ErrorAlignment && showErrorsIfAvailable && ((ErrorAlignment)alignment).isError(i,j)) {
                    c = new Color(255-c.getRed(), 255-c.getGreen(), 255-c.getBlue());
                }

                g.setColor(c);

                Rectangle2D rect2D = new Rectangle2D.Double(j * w + xdelta + maxWidth + spacer, y, w, h * 0.95);

                g2d.fill(rect2D);
            }
        }
        g.translate(-insets.left, -insets.top);

    }

    boolean isShowingTree() {
        return getShowTreeInAlignmentViewerIfAvailable() && timeTree != null;
    }
}
