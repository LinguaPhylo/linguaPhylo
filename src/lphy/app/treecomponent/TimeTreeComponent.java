package lphy.app.treecomponent;

import lphy.ColorTable;
import lphy.StructuredCoalescent;
import lphy.TimeTree;
import lphy.TimeTreeNode;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.geom.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.prefs.Preferences;

import static lphy.app.Utils.MAX_FONT_SIZE;
import static lphy.app.Utils.MIN_FONT_SIZE;

/**
 * @author Alexei Drummond
 */
public class TimeTreeComponent extends JComponent {

    static Preferences preferences = Preferences.userNodeForPackage(TimeTreeComponent.class);

    TimeTreeDrawing treeDrawing;
    TreeDrawingOrientation orientation = TreeDrawingOrientation.RIGHT;
    BranchStyle branchStyle = BranchStyle.SQUARE;
    NodeDecorator leafDecorator, internalNodeDecorator;
    NodePositioningRule positioningRule = NodePositioningRule.AVERAGE_OF_CHILDREN;

    String caption = null;
    String colorTraitName = null;

    private boolean borderSet = false;

    TimeTree tree;

    // the position of the "current" leaf node
    private double p = 0;

    NumberFormat format = NumberFormat.getInstance();

    double rootHeightForScale;

    private Rectangle2D bounds = new Rectangle2D.Double(0, 0, 1, 1);

    static final Color[] traitColors = {Color.red, Color.blue, Color.green, Color.yellow, Color.orange, Color.magenta,
            Color.cyan, Color.gray, Color.darkGray, Color.lightGray, Color.black};
    private ColorTable traitColorTable = new ColorTable(Arrays.asList(traitColors));

    private boolean showNodeIndices = preferences.getBoolean("showNodeIndices", false);

    public TimeTreeComponent() {
    }

    public TimeTreeComponent(TimeTree tree) {
        this();

        setTimeTree(tree);
        int desktopWidth = Toolkit.getDefaultToolkit().getScreenSize().width;

        int maximumWidth = desktopWidth;
        int maximumHeight = MAX_FONT_SIZE * tree.n();
        int minimumWidth = 100;
        int minimumHeight = MIN_FONT_SIZE * tree.n();

        setMaximumSize(new Dimension(maximumWidth, maximumHeight));
        setMinimumSize(new Dimension(minimumWidth, minimumHeight));
        //setPreferredSize(new Dimension(minimumWidth+maximumWidth/2, (minimumHeight+maximumHeight)/2));

        if (tree.getRoot().getMetaData(StructuredCoalescent.populationLabel) != null) {
            setColorTraitName(StructuredCoalescent.populationLabel);
        }
    }

    public void setBorder(Border border) {
        super.setBorder(border);
        borderSet = true;
    }

    public void setTimeTree(TimeTree timeTree) {
        tree = timeTree;
        treeDrawing = new TimeTreeDrawing(this);
        treeDrawing.setRootHeightForCanonicalScaling(tree.getRoot().getAge());
    }

    private class Location {
        int[] loc;

        public Location(int[] loc) {
            this.loc = new int[loc.length];

            for (int i = 0; i < loc.length; i++)
                this.loc[i] = loc[i];
        }

        @Override
        public boolean equals(Object object) {

            if (!(object instanceof Location))
                return false;

            Location otherLocation = (Location) object;

            if (loc.length != otherLocation.loc.length)
                return false;

            for (int i = 0; i < loc.length; i++)
                if (loc[i] != otherLocation.loc[i])
                    return false;

            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 83 * hash + Arrays.hashCode(this.loc);
            return hash;
        }

    }

    Map<Location, Integer> locationColours;
    int nextLocationColour;

    public void setBounds(Rectangle2D bounds) {
        this.bounds = bounds;
        //setSize((int) bounds.getWidth(), (int) bounds.getHeight());
    }

    void setTipValues(TimeTreeNode node) {
        if (node.isLeaf()) {
            node.setMetaData("p", p);
            node.setMetaData("p_min", p);
            node.setMetaData("p_max", p);
            p += getCanonicalNodeSpacing(tree);
        } else {
            double pmin = Double.MAX_VALUE;
            double pmax = Double.MIN_VALUE;
            for (TimeTreeNode childNode : node.getChildren()) {
                setTipValues(childNode);

                double cpmin = (Double) childNode.getMetaData("p_min");
                double cpmax = (Double) childNode.getMetaData("p_max");

                if (cpmin < pmin) pmin = cpmin;
                if (cpmax > pmax) pmax = cpmax;
            }
            node.setMetaData("p_min", pmin);
            node.setMetaData("p_max", pmax);
        }
    }

    void positionInternalNodes(TimeTreeNode node) {
        if (!node.isLeaf()) {
            if (positioningRule.getTraversalOrder() == NodePositioningRule.TraversalOrder.PRE_ORDER) {
                positioningRule.setPosition(node, "p");
            }
            for (TimeTreeNode child : node.getChildren()) {
                positionInternalNodes(child);
            }
            if (positioningRule.getTraversalOrder() == NodePositioningRule.TraversalOrder.POST_ORDER) {
                positioningRule.setPosition(node, "p");
            }

        }
    }

    void drawNode(Point2D p, Graphics2D g, NodeDecorator decorator) {

        double nodeSize = decorator.getNodeSize();

        Shape shape = null;
        double halfSize = nodeSize / 2.0;

        switch (decorator.getNodeShape()) {
            case circle:
                shape = new Ellipse2D.Double(p.getX() - halfSize, p.getY() - halfSize, nodeSize, nodeSize);
                break;
            case square:
                shape = new Rectangle2D.Double(p.getX() - halfSize, p.getY() - halfSize, nodeSize, nodeSize);
                break;
            case triangle:
                Path2D path = new Path2D.Double();
                path.moveTo(p.getX(), p.getY() - halfSize);
                path.lineTo(p.getX() + halfSize, p.getY() + halfSize);
                path.lineTo(p.getX() - halfSize, p.getY() + halfSize);
                path.closePath();
                shape = path;
            default:
        }
        Color oldColor = g.getColor();
        g.setColor(decorator.getNodeColor());
        g.fill(shape);
        g.setColor(oldColor);
        if (decorator.drawNodeShape()) {
            g.draw(shape);
        }
    }

    void drawCanonicalString(String string, double x, double y, Graphics2D g) {

        Point2D p = getTransformedPoint2D(new Point2D.Double(x, y));

        treeDrawing.drawString(string, p.getX(), p.getY(), g);
    }

    private Point2D getCanonicalNodePoint2D(TimeTreeNode node) {
        return new Point2D.Double(getCanonicalNodeX(node), getCanonicalNodeY(node.getAge()));
    }

    private double getCanonicalNodeX(TimeTreeNode node) {
        return (Double) node.getMetaData("p");
    }

    private double getCanonicalNodeY(double height) {

        double h = height / rootHeightForScale;

        // is root aligned?
        //h = h + (1.0 - (tree.rootNode.getAge() / rootHeightForScale));

        return h;
    }

    private double getCanonicalNodeSpacing(TimeTree tree) {
        return 1.0 / (tree.n() - 1);
    }

    private Point2D getTransformedNodePoint2D(TimeTreeNode node) {
        return getTransformedPoint2D(getCanonicalNodePoint2D(node));
    }

    private Point2D getTransformedPoint2D(Point2D canonicalPoint2D) {
        return orientation.getTransform(bounds).transform(canonicalPoint2D, null);
    }

    final void drawBranch(TimeTreeNode node, TimeTreeNode childNode, Graphics2D g) {

        if (colorTraitName != null) {
            int childColorIndex = getIntegerTrait(childNode, colorTraitName);

            int parentColorIndex = getIntegerTrait(node, colorTraitName);

            if (childColorIndex == parentColorIndex && node.getChildren().size() == 1) {
                System.out.println("Parent and single child have same state!!");
                drawNode(getTransformedNodePoint2D(node), g, NodeDecorator.BLACK_DOT);
            }

            g.setColor(traitColorTable.getColor(childColorIndex));
        }

        Shape shape = branchStyle.getBranchShape(getCanonicalNodePoint2D(childNode), getCanonicalNodePoint2D(node));
        Shape transformed = orientation.getTransform(bounds).createTransformedShape(shape);

        g.draw(transformed);
    }

    private int getIntegerTrait(TimeTreeNode childNode, String traitName) {
        Object trait = childNode.getMetaData(traitName);
        if (trait instanceof Integer) return (Integer) trait;
        if (trait instanceof Double) return (int) Math.round((Double) trait);
        if (trait instanceof String) return (int) Math.round(Double.parseDouble((String) trait));

        if (trait instanceof int[]) {
            Location location = new Location((int[]) trait);
            if (locationColours.containsKey(location))
                return locationColours.get(location);
            else {
                locationColours.put(location, nextLocationColour);
                return nextLocationColour++;
            }
        }

        return -1;
    }

    final void drawLeafLabel(TimeTreeNode node, Graphics2D g) {

        Point2D nodePoint = getTransformedNodePoint2D(node);

        if (colorTraitName != null) {
            int colorIndex = getIntegerTrait(node, colorTraitName);
            if (colorIndex >= 0) g.setColor(traitColorTable.getColor(colorIndex));
        }
        treeDrawing.drawString(node.getId(), nodePoint.getX(), nodePoint.getY(), g);
    }

    /**
     * Draws the tree
     *
     * @param treeDrawing
     * @param node
     * @param g
     */
    void draw(TimeTreeDrawing treeDrawing, TimeTreeNode node, Graphics2D g) {

        TimeTree tree = treeDrawing.getTree();

        g.setStroke(new BasicStroke((float) treeDrawing.getLineThickness()));

        p = 0.0; // canonical positioning goes from 0 to 1.

        if (node.isRoot()) {
            setTipValues(node);
            positionInternalNodes(node);
        }

        if (node.isLeaf()) {
            if (treeDrawing.showLeafLabels()) {
                drawLeafLabel(node, g);
            }
        } else {

            for (TimeTreeNode childNode : node.getChildren()) {
                draw(treeDrawing, childNode, g);
            }

            for (TimeTreeNode childNode : node.getChildren()) {

                drawBranch(node, childNode, g);
            }
        }

        // finally draw all the node decorations
        if (node.isRoot()) {
            if (leafDecorator != null) {
                for (TimeTreeNode aNode : tree.getNodes()) {
                    if (leafDecorator != null && aNode.isLeaf()) {
                        drawNode(getTransformedPoint2D(getCanonicalNodePoint2D(aNode)), g, leafDecorator);
                    }
                    if (internalNodeDecorator != null && !aNode.isLeaf()) {
                        drawNode(getTransformedPoint2D(getCanonicalNodePoint2D(aNode)), g, internalNodeDecorator);
                    }

                }
            }
            if (showNodeIndices) {
                for (TimeTreeNode aNode : tree.getNodes()) {
                    if (!aNode.isLeaf()) {
                        Point2D p = getTransformedPoint2D(getCanonicalNodePoint2D(aNode));
                        g.setColor(Color.blue);
                        g.drawString(aNode.getIndex() + "", (int) p.getX(), (int) p.getY());
                        g.setColor(Color.black);
                    }
                }
            }
        }
    }

    public void paintComponent(Graphics g) {

        if (!borderSet) {
            int maxWidth = 0;
            FontMetrics metrics = g.getFontMetrics();
            for (TimeTreeNode node : tree.getNodes()) {
                if (node.getId() != null) {
                    int stringWidth = metrics.stringWidth(node.getId());
                    if (stringWidth > maxWidth) maxWidth = stringWidth;
                }
            }
            setBorder(BorderFactory.createEmptyBorder(metrics.getHeight() / 2 + 1, 1, metrics.getHeight() / 2 + 1, maxWidth));
        }

        Insets insets = getInsets();
        g.translate(insets.left, insets.top);
        int width = getWidth() - insets.left - insets.right;
        int height = getHeight() - insets.top - insets.bottom;

        setBounds(new Rectangle2D.Double(0, 0, width, height));

        Graphics2D g2d = (Graphics2D) g;

        TimeTree tree = treeDrawing.getTree();

        draw(treeDrawing, tree.getRoot(), g2d);
        g.translate(-insets.left, -insets.top);
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public void setColorTraitName(String colorTraitName) {
        this.colorTraitName = colorTraitName;
    }

    public void setTraitColorTable(ColorTable colorTable) {
        this.traitColorTable = colorTable;
    }
}

