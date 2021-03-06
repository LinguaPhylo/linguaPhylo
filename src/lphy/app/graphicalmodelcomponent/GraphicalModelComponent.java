package lphy.app.graphicalmodelcomponent;

import lphy.app.GraphicalLPhyParser;
import lphy.app.GraphicalModelChangeListener;
import lphy.app.GraphicalModelListener;
import lphy.app.Symbols;
import lphy.app.graphicalmodelcomponent.interactive.LatticePoint;
import lphy.core.distributions.IID;
import lphy.core.distributions.VectorizedDistribution;
import lphy.core.functions.VectorizedFunction;
import lphy.core.narrative.LaTeXUtils;
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
import static lphy.graphicalModel.ValueUtils.isNumber;

/**
 * Created by adru001 on 18/12/19.
 */
public class GraphicalModelComponent extends JComponent implements GraphicalModelChangeListener {

    public static Preferences preferences = Preferences.userNodeForPackage(GraphicalModelComponent.class);

    GraphicalLPhyParser parser;

    float STROKE_SIZE = 1.0f;

    List<GraphicalModelListener> listeners = new ArrayList<>();

    LayeredNode selectedNode;

    boolean sizeChanged = true;

    private static final String SHOW_CONSTANT_NODES = "showConstantNodes";
    private static final String SHOW_ARGUMENT_LABELS = "showArgumentLabels";
    private static final String USE_STRAIGHT_EDGES = "useStraightEdges";
    private static boolean showArgumentLabels = preferences.getBoolean(SHOW_ARGUMENT_LABELS, false);
    private static boolean useStraightEdges = preferences.getBoolean(USE_STRAIGHT_EDGES, false);

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

    public static boolean getUseStraightEdges() {
        return useStraightEdges;
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
            for (GraphicalModelListener listener : listeners) {
                listener.layout();
            }
        }

        Graphics2D g2d = (Graphics2D) g;

        g.setColor(Color.white);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.black);

        g2d.setStroke(new BasicStroke(STROKE_SIZE));

        for (LayeredNode properNode : properLayeredGraph.getNodes()) {

            NodePaintUtils.paintNodeEdges(properNode,g2d,showArgumentLabels, useStraightEdges);
        }
    }

    public String toTikz() {

        return toTikz(1.0, 1.0, false, "");
    }

    public String toTikz(boolean inline) {

        return toTikz(1.0, 1.0, inline, "");
    }


    public String toTikz(double xScale, double yScale, boolean inline, String options) {

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

        String beginDocument = "\\documentclass[border=3mm]{standalone} % For LaTeX2e\n" +
                "\\usepackage{tikz}\n" +
                "\\usepackage{bm}\n" +
                "\\usetikzlibrary{bayesnet}\n" +
                "\n" +
                "\\begin{document}\n\n";

        if (options.length() > 0 && !options.endsWith(",")) {
            options = options + ",";
        }

        String preamble =
                "\\begin{tikzpicture}[" + options + "\n" +
                "dstyle/.style={draw=blue!50,fill=blue!20},\n" +
                "vstyle/.style={draw=green,fill=green!20},\n" +
                "cstyle/.style={font=\\small},\n" +
                "detstyle/.style={draw=red!50,fill=red!20}\n" +
                "]\n";

        String postamble = "\\end{tikzpicture}\n";

        String endDocument = " \\end{document}";

        StringBuilder builder = new StringBuilder();
        if (!inline) builder.append(beginDocument);
        builder.append(preamble);
        builder.append(nodes.toString());
        builder.append(factors.toString());
        builder.append(postamble);
        if (!inline) builder.append(endDocument);
        return builder.toString();
    }

    private String valueToTikz(LayeredGNode gNode, Value value, double xScale, double yScale) {

        String type = "const";
        String style = "cstyle";

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

        String uniqueId = getUniqueId(value);
        //uniqueId = uniqueId.replace("_", "."); // can't have underscore in these names.

        return "\\node[" + type + ((style != null) ? ", " + style : "") + "] at (" + latticePoint.x*xScale + ", -" + latticePoint.y*yScale + ") (" + uniqueId + ") {" + getTikzLabel(gNode) + "};";
    }

    private String getTikzLabel(LayeredGNode gNode) {
        Value value = (Value)gNode.value();
        String label = Symbols.getCanonical(gNode.name, "$\\", "$");
        if (!value.isAnonymous()) {
            label = LaTeXUtils.getMathId(value, true, true);
        }

        if (parser.isClamped(value.getId()) && parser.isNamedDataValue(value)) {
            label = "'" + label + "'";
        }

        if (value.isAnonymous() && isNumber(value)) {
            label = unbracket(gNode.name) + " = " + value.value().toString();
        }
        return label;
    }

    private String getUniqueId(Value value) {
        String uniqueId = value.getCanonicalId();
        if (uniqueId == null) uniqueId = value.getUniqueId();
        if (parser.isClamped(value.getId()) && parser.isNamedDataValue(value)) {
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

        String factorName = generator.getName() + getUniqueId(value);

        //factorName = factorName.replace('_', '.');

        StringBuilder predecessors = new StringBuilder();

        List<LayeredNode> pred = gNode.getPredecessors();

        if (pred.size() > 0) {
            predecessors = new StringBuilder(getUniqueId((Value) ((LayeredGNode) pred.get(0)).value()));
        }
        for (int i = 1; i < pred.size(); i++) {
            predecessors.append(", ").append(getUniqueId((Value) ((LayeredGNode) pred.get(i)).value()));
        }

        String generatorName = generator.getName();

        if (generator instanceof VectorizedDistribution) {
            Value replicates = ((VectorizedDistribution)generator).getReplicatesValue();
            if (replicates != null) generatorName = generatorName + "[" + getUniqueId(replicates) + "]";
        }

        if (generator instanceof VectorizedFunction) {
            Value replicates = ((VectorizedFunction)generator).getReplicatesValue();
            if (replicates != null) generatorName = generatorName + "[" + getUniqueId(replicates) + "]";
        }

        if (generator instanceof IID) {
            Value replicates = ((IID)generator).getReplicates();
            if (replicates != null) generatorName = generatorName + "[" + getUniqueId(replicates) + "]";
        }

        String factorString =  "\\factor[above=of " + getUniqueId(value) + "] {" + factorName + "} {left:\\scriptsize " + generatorName + "} {} {} ; %\n";
        String factorEdgeString =  "\\factoredge {" + predecessors + "} {" + factorName + "} {" + getUniqueId(value) + "}; %";

        return factorString + factorEdgeString;
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

    public void setUseStraightEdges(boolean useStraightEdges) {
        this.useStraightEdges = useStraightEdges;
    }
}