package james;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * @author Alexei Drummond
 */

public class TimeTreeDrawing {

    TimeTree tree;
    TimeTreeComponent treeComponent;

    public double getLineThickness() {
        return 1.0;
    }

    public boolean showLeafLabels() {
        return true;
    }

    enum TreeOrientation {up, down, left, right}

    enum TreeBranchStyle {line, square}

    enum NodePosition {average, triangulated, firstChild}

    public TimeTreeDrawing() {
    }

    public TimeTreeDrawing(TimeTreeComponent treeComponent) {
        this.tree = treeComponent.tree;
        this.treeComponent = treeComponent;
    }

    public void setBounds(Rectangle2D bounds) {
        treeComponent.setBounds(bounds);
    }

    public void paintTimeTree(Graphics2D g) {
        treeComponent.paint(g);
    }

    public void setRootHeightForCanonicalScaling(double height) {
        treeComponent.rootHeightForScale = height;
    }

    public TimeTree getTree() {
        return tree;
    }

    public final void drawString(String string, double x, double y, Graphics2D g) {
        if (string != null) {
            //Object oldAnchorValue = g.getRenderingHint(TikzRenderingHints.KEY_NODE_ANCHOR);
            //Object oldFontSize = g.getRenderingHint(TikzRenderingHints.KEY_FONT_SIZE);
            //g.setRenderingHint(TikzRenderingHints.KEY_NODE_ANCHOR, anchor);
            //g.setRenderingHint(TikzRenderingHints.KEY_FONT_SIZE, fontSize);

            int ascent = g.getFontMetrics().getAscent();

            g.drawString(string, (float) x, (float) y + ascent/2.0f);

            //if (oldAnchorValue != null) g.setRenderingHint(TikzRenderingHints.KEY_NODE_ANCHOR, oldAnchorValue);
            //if (oldFontSize != null) g.setRenderingHint(TikzRenderingHints.KEY_FONT_SIZE, oldFontSize);
        }
    }

}
