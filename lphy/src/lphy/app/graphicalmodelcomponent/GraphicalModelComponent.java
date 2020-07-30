package lphy.app.graphicalmodelcomponent;

import lphy.app.GraphicalLPhyParser;
import lphy.app.GraphicalModelChangeListener;
import lphy.app.GraphicalModelListener;
import lphy.graphicalModel.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;

import static lphy.app.graphicalmodelcomponent.LayeredGNode.*;

/**
 * Created by adru001 on 18/12/19.
 */
public class GraphicalModelComponent extends JComponent implements GraphicalModelChangeListener {

    public static Preferences preferences = Preferences.userNodeForPackage(GraphicalModelComponent.class);

    GraphicalLPhyParser parser;

    double ARROWHEAD_WIDTH = 4;
    double ARROWHEAD_DEPTH = 10;

    float STROKE_SIZE = 1.0f;

    List<GraphicalModelListener> listeners = new ArrayList<>();

    LayeredNode selectedNode;

    boolean sizeChanged = true;

    private static final String SHOW_CONSTANT_NODES = "showConstantNodes";
    private static final String SHOW_ARGUMENT_LABELS = "showArgumentLabels";
    private static boolean showArgumentLabels = preferences.getBoolean(SHOW_ARGUMENT_LABELS, false);

    LayeredGraph layeredGraph = null;
    ProperLayeredGraph properLayeredGraph = null;
    Layering layering = new Layering.LongestPathFromSinks();
    Ordering ordering = new Ordering();
    Positioning positioning = new Positioning();
    int BORDER = 20;
    Insets insets = new Insets((int) VAR_HEIGHT / 2 + BORDER,
            (int) VAR_WIDTH / 2 + BORDER, (int) VAR_HEIGHT / 2 + BORDER, (int) VAR_WIDTH / 2 + BORDER);

    public GraphicalModelComponent(GraphicalLPhyParser parser) {
        this.parser = parser;

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                sizeChanged = true;
                repaint();
            }
        });

        setup();
        parser.addGraphicalModelChangeListener(this::setup);
    }

    private void setup() {

        removeAll();
        layeredGraph = LayeredGraphFactory.createLayeredGraph(parser, getShowConstantNodes());

        for (LayeredNode lnode : layeredGraph.getNodes()) {

            LayeredGNode node = (LayeredGNode) lnode;

            if (node.hasButton()) {
                JButton button = node.getButton();
                button.addActionListener(e -> {
                    if (node.value() instanceof Value) {
                        for (GraphicalModelListener listener : listeners) {
                            listener.valueSelected((Value) node.value());
                        }
                        selectedNode = node;
                    }
                    if (node.value() instanceof GenerativeDistribution) {
                        for (GraphicalModelListener listener : listeners) {
                            listener.generativeDistributionSelected((GenerativeDistribution) node.value());
                        }
                    }
                    if (node.value() instanceof DeterministicFunction) {
                        for (GraphicalModelListener listener : listeners) {
                            listener.functionSelected((DeterministicFunction) node.value());
                        }
                    }
                });
                add(button);
            }
        }
        sizeChanged = true;
    }

    public void addGraphicalModelListener(GraphicalModelListener listener) {
        listeners.add(listener);
    }

    public void setLayering(Layering layering) {
        this.layering = layering;
        sizeChanged = true;
        repaint();
    }

    public void paintComponent(Graphics g) {

        double delta = g.getFontMetrics().getAscent() / 2.0;

        if (sizeChanged) {
            layeredGraph.applyLayering(layering);
            properLayeredGraph = new ProperLayeredGraph(layeredGraph, layering);
            ordering.order(properLayeredGraph);
            new BrandesKopfHorizontalCoordinateAssignment(properLayeredGraph);
            positioning.position(properLayeredGraph, getSize(), insets);
            sizeChanged = false;
        }

        Graphics2D g2d = (Graphics2D) g;

        g.setColor(Color.white);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.black);

        g2d.setStroke(new BasicStroke(STROKE_SIZE));

        for (LayeredNode properNode : properLayeredGraph.getNodes()) {

            double x1 = properNode.getX();
            double y1 = properNode.getY();

            if (properNode.isDummy()) {

                for (LayeredNode successor : properNode.getSuccessors()) {
                    double x2 = successor.getX();
                    double y2 = successor.getY();
                    if (isWrappedParameterized(successor)) {
                        y2 -= FACTOR_SIZE;
                    } else if (isWrappedValue(successor)) {
                        y2 -= VAR_HEIGHT / 2;
                    }
                    drawLine(g2d, x1, y1, x2, y2);
                }
            } else {

                y1 += VAR_HEIGHT / 2;

                NodeWrapper nodeWrapper = (NodeWrapper) properNode;
                LayeredGNode node = (LayeredGNode) nodeWrapper.wrappedNode();

                if (node.value() instanceof Value) {

                    for (LayeredNode successor : properNode.getSuccessors()) {

                        double x2 = successor.getX();
                        double y2 = successor.getY() - (successor.isDummy() ? 0.0 : FACTOR_SIZE);
                        drawLine(g2d, x1, y1, x2, y2);
                        if (showArgumentLabels) {
                            String label = ((Generator) (getUnwrappedNonDummySuccessor(successor)).value()).getParamName((Value) node.value());
                            g.setColor(Color.gray);
                            g.drawString(label, (int) Math.round((x1 + x2) / 2.0 - g.getFontMetrics().stringWidth(label) / 2.0), (int) Math.round((y1 + y2) / 2.0 + delta));
                            g.setColor(Color.black);
                        }
                    }
                } else if (node.value() instanceof Generator) {
                    Generator gen = (Generator) node.value();

                    String str = gen.getName();

                    LayeredNode properSuccessor = properNode.getSuccessors().get(0);

                    Point2D q = properSuccessor.getPosition();

                    g2d.drawString(str, (float) (node.getX() + FACTOR_SIZE + FACTOR_LABEL_GAP), (float) (node.getY() + FACTOR_SIZE - STROKE_SIZE));

                    x1 = node.getX();
                    y1 = node.getY() + (properSuccessor.isDummy() ? 0.0 : FACTOR_SIZE);
                    double x2 = q.getX();
                    double y2 = q.getY() - VAR_HEIGHT / 2;
                    
                    drawArrowLine(g2d, x1, y1, x2, y2, ARROWHEAD_DEPTH, ARROWHEAD_WIDTH);
                }
            }
        }
    }

    private LayeredGNode getUnwrappedNonDummySuccessor(LayeredNode successor) {
        if (successor.isDummy()) return getUnwrappedNonDummySuccessor(successor.getSuccessors().get(0));
        return (LayeredGNode) ((NodeWrapper) successor).wrappedNode();
    }

    private boolean isWrappedParameterized(LayeredNode v) {
        return !v.isDummy() && v instanceof NodeWrapper && ((NodeWrapper) v).wrappedNode() instanceof LayeredGNode &&
                ((LayeredGNode) ((NodeWrapper) v).wrappedNode()).value() instanceof Generator;
    }

    private boolean isWrappedValue(LayeredNode v) {
        return !v.isDummy() && v instanceof NodeWrapper && ((NodeWrapper) v).wrappedNode() instanceof LayeredGNode &&
                ((LayeredGNode) ((NodeWrapper) v).wrappedNode()).value() instanceof Value;
    }

    private void drawLine(Graphics2D g, double x1, double y1, double x2, double y2) {
        Line2D line = new Line2D.Double(x1, y1, x2, y2);
        g.draw(line);
    }

    /**
     * Draw an arrow line between two points.
     *
     * @param g the graphics component.
     * @param d the width of the arrow.
     * @param h the height of the arrow.
     */
    private void drawArrowLine(Graphics2D g, double x1, double y1, double x2, double y2, double d, double h) {

        if (!Double.isNaN(x1) && !Double.isNaN(x2)) {

            double dx = x2 - x1, dy = y2 - y1;
            double D = Math.sqrt(dx * dx + dy * dy);
            double xm = D - d, xn = xm, ym = h, yn = -h, x;
            double sin = dy / D, cos = dx / D;

            x = xm * cos - ym * sin + x1;
            ym = xm * sin + ym * cos + y1;
            xm = x;

            x = xn * cos - yn * sin + x1;
            yn = xn * sin + yn * cos + y1;
            xn = x;

            Line2D.Double line = new Line2D.Double(x1, y1, x2, y2);

            GeneralPath p = new GeneralPath();
            p.moveTo(x2, y2);
            p.lineTo(xm, ym);
            p.lineTo(xn, yn);
            p.closePath();


            g.draw(line);
            g.fill(p);
        }
    }

    @Override
    public void modelChanged() {
        setup();
        repaint();
    }

    public void setShowConstantNodes(boolean showConstantNodes) {
        preferences.putBoolean(SHOW_CONSTANT_NODES, showConstantNodes);
        setup();
        repaint();
    }

    public boolean getShowConstantNodes() {
        return preferences.getBoolean(SHOW_CONSTANT_NODES, true);
    }

    public void setShowValueInNode(boolean showValues) {
        LayeredGNode.setShowValueInNode(showValues);
        setup();
        repaint();
    }

    public static boolean getShowArgumentLabels() {
        return showArgumentLabels;
    }

    public void setShowArgumentLabels(boolean show) {
        preferences.putBoolean(SHOW_ARGUMENT_LABELS, show);
        showArgumentLabels = show;
        repaint();
    }
}