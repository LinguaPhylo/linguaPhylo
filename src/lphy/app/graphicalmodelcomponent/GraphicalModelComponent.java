package lphy.app.graphicalmodelcomponent;

import lphy.app.GraphicalLPhyParser;
import lphy.app.GraphicalModelChangeListener;
import lphy.app.GraphicalModelListener;
import lphy.app.GraphicalModelPanel;
import lphy.app.graphicalmodelcomponent.interactive.LatticePoint;
import lphy.core.distributions.VectorizedDistribution;
import lphy.core.functions.VectorizedFunction;
import lphy.graphicalModel.*;
import lphy.graphicalModel.Vector;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;

import static lphy.app.graphicalmodelcomponent.LayeredGNode.*;
import static lphy.graphicalModel.ValueUtils.isNumber;

/**
 * Created by adru001 on 18/12/19.
 */
public class GraphicalModelComponent extends JComponent implements GraphicalModelChangeListener {

    public static Preferences preferences = Preferences.userNodeForPackage(GraphicalModelComponent.class);

    GraphicalLPhyParser parser;

    double ARROWHEAD_WIDTH = 4;
    double ARROWHEAD_DEPTH = 10;

    private static float FACTOR_LABEL_FONT_SIZE = 11.0f;

    float STROKE_SIZE = 1.0f;

    List<GraphicalModelListener> listeners = new ArrayList<>();

    LayeredNode selectedNode;

    boolean sizeChanged = true;

    private static final String SHOW_CONSTANT_NODES = "showConstantNodes";
    private static final String SHOW_ARGUMENT_LABELS = "showArgumentLabels";
    private static boolean showArgumentLabels = preferences.getBoolean(SHOW_ARGUMENT_LABELS, false);

    LayeredGraph layeredGraph = null;
    public ProperLayeredGraph properLayeredGraph = null;
    Layering layering = new Layering.LongestPathFromSinks();
    Ordering ordering = new Ordering();
    public Positioning positioning = new Positioning();
    int BORDER = 20;
    public Insets insets = new Insets((int) VAR_HEIGHT / 2 + BORDER,
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

    public LayeredGraph getLayeredGraph() {
        return layeredGraph;
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

                NodeWrapper nodeWrapper = (NodeWrapper) properNode;
                LayeredGNode node = (LayeredGNode) nodeWrapper.wrappedNode();

                if (node.value() instanceof Value) {
                    paintValueNode((Value)node.value(), properNode, g2d);
                } else if (node.value() instanceof Generator) {
                    paintGeneratorNode((Generator)node.value(), node, properNode, g2d);
                }
            }
        }
    }

    /**
     * @param value the value to paint the node of
     * @param properNode the proper node representing this generator
     * @param g2d the Graphics2D context to paint to
     */
    private void paintValueNode(Value value, LayeredNode properNode, Graphics2D g2d) {

        double x1 = properNode.getX();
        double y1 = properNode.getY() + VAR_HEIGHT / 2;
        
        double delta = g2d.getFontMetrics().getAscent() / 2.0;

        for (LayeredNode successor : properNode.getSuccessors()) {

            double x2 = successor.getX();
            double y2 = successor.getY() - (successor.isDummy() ? 0.0 : FACTOR_SIZE);
            drawLine(g2d, x1, y1, x2, y2);
            if (showArgumentLabels) {
                String label = ((Generator) (getUnwrappedNonDummySuccessor(successor)).value()).getParamName(value);
                g2d.setColor(Color.gray);
                g2d.drawString(label, (int) Math.round((x1 + x2) / 2.0 - g2d.getFontMetrics().stringWidth(label) / 2.0), (int) Math.round((y1 + y2) / 2.0 + delta));
                g2d.setColor(Color.black);
            }
        }
    }

    /**
     * @param generator the generator to paint the node of
     * @param node the LayerdNode representing this Generator
     * @param properNode the proper node representing this generator
     * @param g2d the Graphics2D context to paint to
     */
    private void paintGeneratorNode(Generator generator, LayeredNode node, LayeredNode properNode, Graphics2D g2d) {

        // is this a vectorized Generator?
        boolean vectorized = (generator instanceof VectorizedDistribution || generator instanceof VectorizedFunction);

        String str = generator.getName();
        if (vectorized) {
            Value value = (Value)((LayeredGNode)node.getSuccessors().get(0)).value();
            str += "[";
            if (value instanceof Vector) str += ((Vector)value).size();
            str += "]";
        }

        LayeredNode properSuccessor = properNode.getSuccessors().get(0);

        Point2D q = properSuccessor.getPosition();

        Font font = g2d.getFont();
        Font newFont = vectorized ? font.deriveFont(Font.BOLD,FACTOR_LABEL_FONT_SIZE) : font.deriveFont(FACTOR_LABEL_FONT_SIZE);
        g2d.setFont(newFont);

        double delta = g2d.getFontMetrics().getAscent() / 2.0;

        g2d.drawString(str, (float) (node.getX() + FACTOR_SIZE + FACTOR_LABEL_GAP), (float) (node.getY() + delta));
        if (vectorized) g2d.setFont(font);

        double x1 = node.getX();
        double y1 = node.getY() + (properSuccessor.isDummy() ? 0.0 : FACTOR_SIZE);
        double x2 = q.getX();
        double y2 = q.getY() - VAR_HEIGHT / 2;

        drawArrowLine(g2d, x1, y1, x2, y2, ARROWHEAD_DEPTH, ARROWHEAD_WIDTH);
    }

    public String toTikz() {

        return toTikz(1.0, 1.0);
    }

    private String toTikz(double xScale, double yScale) {

        StringBuilder nodes = new StringBuilder();
        StringBuilder factors = new StringBuilder();

        for (LayeredNode properNode : properLayeredGraph.getNodes()) {

            double x1 = properNode.getX();
            double y1 = properNode.getY();

            if (!properNode.isDummy()) {

                y1 += VAR_HEIGHT / 2;

                NodeWrapper nodeWrapper = (NodeWrapper) properNode;
                LayeredGNode node = (LayeredGNode) nodeWrapper.wrappedNode();

                if (node.value() instanceof Value) {

                    nodes.append(valueToTikz(node, (Value)node.value(), xScale, yScale)).append("\n");

                } else if (node.value() instanceof Generator) {
                    factors.append(generatorToTikz(node, (Generator)node.value())).append("\n");

                }
            }
        }

        String preamble = "\\documentclass[border=3mm]{standalone} % For LaTeX2e\n" +
                "\\usepackage{tikz}\n" +
                "\\usepackage{bm}\n" +
                "\\usetikzlibrary{bayesnet}\n" +
                "\n" +
                "\\begin{document}\n" +
                "\n" +
                "\\begin{tikzpicture}[\n" +
                "dstyle/.style={draw=blue!50,fill=blue!20},\n" +
                "vstyle/.style={draw=green,fill=green!20},\n" +
                "detstyle/.style={draw=red!50,fill=red!20}\n" +
                "]\n";

        String postamble = "\\end{tikzpicture}\n" +
                " \\end{document}";

        return preamble + nodes.toString() + factors.toString() + postamble;
    }

    private String valueToTikz(LayeredGNode gNode, Value value, double xScale, double yScale) {

        String type = "const";
        String style = null;

        if (parser.isClampedVariable(value)) {
            type = "obs";
            style = "dstyle";
        } else if (value instanceof RandomVariable) {
            type = "latent";
            style = "vstyle";
        } else if (value.getGenerator() != null) {
            type = "det";
            style = "detstyle";
        }

        LatticePoint latticePoint = (LatticePoint)gNode.getMetaData(LatticePoint.KEY);

        return "\\node[" + type + ((style != null) ? ", " + style : "") + "] at (" + latticePoint.x*xScale + ", -" + latticePoint.y*yScale + ") (" + getUniqueId(value) + ") {" + getLabel(gNode) + "};";
    }

    private String getLabel(LayeredGNode gNode) {
        Value value = (Value)gNode.value();
        String label = gNode.name;
        if (parser.isClamped(value.getId()) && parser.isDataValue(value)) {
            label = "'" + label + "'";
        }

        if (value.isAnonymous() && isNumber(value)) {
            label = unbracket(gNode.name) + " = " + value.value().toString();
        }
        return label;
    }

    private String getUniqueId(Value value) {
        String uniqueId = value.getUniqueId();
        if (parser.isClamped(value.getId()) && parser.isDataValue(value)) {
            uniqueId = "'" + uniqueId + "'";
        }
        return uniqueId;
    }

    private String unbracket(String str) {
        if (str.startsWith("[") && str.endsWith("]")) return str.substring(1, str.indexOf(']'));
        return str;
    }

    private String generatorToTikz(LayeredGNode gNode, Generator generator) {

        Value value = (Value)((LayeredGNode)gNode.getSuccessors().get(0)).value();

        String factorName = generator.getName() + value.getUniqueId();

        StringBuilder predecessors = new StringBuilder();

        List<LayeredNode> pred = gNode.getPredecessors();

        if (pred.size() > 0) {
            predecessors = new StringBuilder(getUniqueId((Value) ((LayeredGNode) pred.get(0)).value()));
        }
        for (int i = 1; i < pred.size(); i++) {
            predecessors.append(", ").append(getUniqueId((Value) ((LayeredGNode) pred.get(i)).value()));
        }

        String factorString =  "\\factor[above=of " + getUniqueId(value) + "] {" + factorName + "} {left:" + generator.getName() + "} {} {} ; %\n";
        String factorEdgeString =  "\\factoredge {" + predecessors + "} {" + factorName + "} {" + getUniqueId(value) + "}; %";

        return factorString + factorEdgeString;
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