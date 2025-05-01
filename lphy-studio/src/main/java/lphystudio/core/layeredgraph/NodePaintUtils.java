package lphystudio.core.layeredgraph;

import lphy.base.math.MathUtils;
import lphy.core.model.Generator;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.ValueUtils;
import lphy.core.parser.LPhyParserDictionary;
import lphy.core.vectorization.IID;
import lphy.core.vectorization.VectorizedDistribution;
import lphy.core.vectorization.VectorizedFunction;
import lphystudio.core.theme.ThemeColours;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

public class NodePaintUtils {

//    static DecimalFormat format = new DecimalFormat("0.00E0");
    private static int SIGNIFICANT_FIGURE = 3;

    private static float FACTOR_LABEL_FONT_SIZE = 11.0f;
    private static double ARROWHEAD_WIDTH = 4;
    private static double ARROWHEAD_DEPTH = 10;

    private static JLabel renderer = new JLabel("", JLabel.CENTER);
    private static CellRendererPane crp = new CellRendererPane();


    public static void paintNode(LayeredNode properNode, Graphics2D g2d, JComponent component, LPhyParserDictionary parser) {
        NodeWrapper nodeWrapper = (NodeWrapper) properNode;

        if (!nodeWrapper.isDummy()) {
            LayeredGNode node = (LayeredGNode) nodeWrapper.wrappedNode();

            if (node.value() instanceof Value) {
                paintValueNode((Value) node.value(), node, g2d, component, parser);
            } else if (node.value() instanceof Generator) {
                paintGeneratorNode((Generator) node.value(), node, properNode, g2d);
            }
        }
    }

    private static void paintValueNode(Value value, LayeredGNode gNode, Graphics2D g2d, JComponent component, LPhyParserDictionary parser) {

        Shape shape = null;
        if (value instanceof RandomVariable) {
            shape = nodeCircle(gNode);
        } else if (value.getGenerator() != null) {
            shape = nodeDiamond(gNode);
        } else shape = nodeSquare(gNode);

        Color fillColor = ThemeColours.getFillColor(value, parser);
        Color borderColor = ThemeColours.getBorderColor(value, parser);

        g2d.setColor(fillColor);
        g2d.fill(shape);
        g2d.setColor(borderColor);
        g2d.draw(shape);

        String s = getNodeString(gNode, value, false);
        renderer.setText(s);
        crp.paintComponent(g2d, renderer, component,
                (int) (gNode.getX() - LayeredGNode.VAR_HEIGHT / 2.0), (int) (gNode.getY() - LayeredGNode.VAR_HEIGHT / 2.0), (int) LayeredGNode.VAR_HEIGHT, (int) LayeredGNode.VAR_HEIGHT);
    }

    private static Shape nodeCircle(LayeredNode node) {
        return new Ellipse2D.Double(node.getX() - LayeredGNode.VAR_HEIGHT / 2.0, node.getY() - LayeredGNode.VAR_HEIGHT / 2.0, LayeredGNode.VAR_HEIGHT, LayeredGNode.VAR_HEIGHT);
    }

    private static Shape nodeSquare(LayeredNode node) {
        return new Rectangle2D.Double(node.getX() - LayeredGNode.VAR_HEIGHT / 2.0, node.getY() - LayeredGNode.VAR_HEIGHT / 2.0, LayeredGNode.VAR_HEIGHT, LayeredGNode.VAR_HEIGHT);
    }

    private static Shape nodeDiamond(LayeredNode node) {
        GeneralPath path = new GeneralPath();

        double radius = LayeredGNode.VAR_HEIGHT / 2.0;

        path.moveTo(node.getX()-radius, node.getY());
        path.lineTo(node.getX(), node.getY() - radius);
        path.lineTo(node.getX() + radius, node.getY());
        path.lineTo(node.getX(), node.getY() + radius);
        path.closePath();

        return path;
    }

    private static void paintGeneratorNode(Generator generator, LayeredGNode node, LayeredNode properNode, Graphics2D g2d) {
        Rectangle2D rectangle2D = new Rectangle2D.Double(node.getX() - LayeredGNode.FACTOR_SIZE, node.getY() - LayeredGNode.FACTOR_SIZE, LayeredGNode.FACTOR_SIZE * 2.0, LayeredGNode.FACTOR_SIZE * 2.0);
        g2d.setColor(Color.white);
        g2d.fill(rectangle2D);
        g2d.setColor(Color.black);
        g2d.draw(rectangle2D);
    }

    public static void paintNodeEdges(LayeredNode properNode, Graphics2D g2d, boolean showArgumentLabels, boolean straightEdges) {
        NodeWrapper nodeWrapper = (NodeWrapper) properNode;

        if (nodeWrapper.isDummy()) {
            if (!straightEdges) paintDummyNodeEdges(properNode, g2d);
        } else {

            LayeredGNode node = (LayeredGNode) nodeWrapper.wrappedNode();

            if (node.value() instanceof Value) {
                paintValueNodeEdges((Value) node.value(), properNode, g2d, showArgumentLabels, straightEdges);
            } else if (node.value() instanceof Generator) {
                paintGeneratorNodeEdges((Generator) node.value(), node, properNode, g2d);
            }
        }
    }

    private static void paintDummyNodeEdges(LayeredNode properNode, Graphics2D g2d) {
        double x1 = properNode.getX();
        double y1 = properNode.getY();

        if (properNode.isDummy()) {

            for (LayeredNode successor : properNode.getSuccessors()) {
                double x2 = successor.getX();
                double y2 = successor.getY();
                if (isWrappedParameterized(successor)) {
                    y2 -= LayeredGNode.FACTOR_SIZE;
                } else if (isWrappedValue(successor)) {
                    y2 -= LayeredGNode.VAR_HEIGHT / 2;
                }
                drawLine(g2d, x1, y1, x2, y2);
            }
        }
    }

    /**
     * @param value      the value to paint the node of
     * @param properNode the proper node representing this generator
     * @param g2d        the Graphics2D context to paint to
     */
    private static void paintValueNodeEdges(Value value, LayeredNode properNode, Graphics2D g2d, boolean showArgumentLabels, boolean straightLines) {

        double x1 = properNode.getX();
        double y1 = properNode.getY() + LayeredGNode.VAR_HEIGHT / 2;

        double delta = g2d.getFontMetrics().getAscent() / 2.0;

        for (LayeredNode successor : properNode.getSuccessors()) {

            if (straightLines) {
                successor = getUnwrappedNonDummySuccessor(successor);
            }

            double x2 = successor.getX();
            double y2 = successor.getY() - (successor.isDummy() ? 0.0 : LayeredGNode.FACTOR_SIZE);
            drawLine(g2d, x1, y1, x2, y2);
            if (showArgumentLabels) {
                Generator generator = null;
                if (straightLines) {
                    generator = (Generator) ((LayeredGNode) successor).value();
                } else generator = (Generator) getUnwrappedNonDummySuccessor(successor).value();

                String label = generator.getParamName(value);
                g2d.setColor(Color.gray);
                g2d.drawString(label, (int) Math.round((x1 + x2) / 2.0 - g2d.getFontMetrics().stringWidth(label) / 2.0), (int) Math.round((y1 + y2) / 2.0 + delta));
                g2d.setColor(Color.black);
            }
        }
    }

    /**
     * @param generator  the generator to paint the node of
     * @param node       the LayerdNode representing this Generator
     * @param properNode the proper node representing this generator
     * @param g2d        the Graphics2D context to paint to
     */
    private static void paintGeneratorNodeEdges(Generator generator, LayeredNode node, LayeredNode properNode, Graphics2D g2d) {

        // is this a vectorized Generator?
        boolean vectorized = (generator instanceof VectorizedDistribution || generator instanceof VectorizedFunction || generator instanceof IID);

        String str = generator.getName();
        Integer size = 0;
        if (vectorized) {
            str += "[";

            Value<Integer> replicatesValue = null;
            if (generator instanceof VectorizedDistribution) {
                replicatesValue = ((VectorizedDistribution)generator).getReplicatesValue();
                size = ((VectorizedDistribution)generator).getComponentDistributions().size();
            } else if (generator instanceof VectorizedFunction) {
                replicatesValue = ((VectorizedFunction)generator).getReplicatesValue();
                size = ((VectorizedFunction)generator).getComponentFunctions().size();
            } else if (generator instanceof IID) {
                replicatesValue = ((IID)generator).getReplicates();
                size = ((IID)generator).size();

            }
            if (replicatesValue != null) {
                str += replicatesValue.isAnonymous() ? replicatesValue.codeString() : replicatesValue.getId();
            } else {
                str += size;
            }

            str += "]";
        }

        LayeredNode properSuccessor = properNode.getSuccessors().get(0);

        Point2D q = properSuccessor.getPosition();

        Font font = g2d.getFont();
        Font newFont = vectorized ? font.deriveFont(Font.BOLD, FACTOR_LABEL_FONT_SIZE) : font.deriveFont(FACTOR_LABEL_FONT_SIZE);
        g2d.setFont(newFont);

        double delta = g2d.getFontMetrics().getAscent() / 2.0;

        g2d.drawString(str, (float) (node.getX() + LayeredGNode.FACTOR_SIZE + LayeredGNode.FACTOR_LABEL_GAP), (float) (node.getY() + delta));
        if (vectorized) g2d.setFont(font);

        double x1 = node.getX();
        double y1 = node.getY() + (properSuccessor.isDummy() ? 0.0 : LayeredGNode.FACTOR_SIZE);
        double x2 = q.getX();
        double y2 = q.getY() - LayeredGNode.VAR_HEIGHT / 2;

        drawArrowLine(g2d, x1, y1, x2, y2, ARROWHEAD_DEPTH, ARROWHEAD_WIDTH);
    }

    private static void drawLine(Graphics2D g, double x1, double y1, double x2, double y2) {
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
    private static void drawArrowLine(Graphics2D g, double x1, double y1, double x2, double y2, double d, double h) {

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

    private static LayeredGNode getUnwrappedNonDummySuccessor(LayeredNode successor) {
        if (successor.isDummy()) return getUnwrappedNonDummySuccessor(successor.getSuccessors().get(0));
        return (LayeredGNode) ((NodeWrapper) successor).wrappedNode();
    }

    private static boolean isWrappedParameterized(LayeredNode v) {
        return !v.isDummy() && v instanceof NodeWrapper && ((NodeWrapper) v).wrappedNode() instanceof LayeredGNode &&
                ((LayeredGNode) ((NodeWrapper) v).wrappedNode()).value() instanceof Generator;
    }

    private static boolean isWrappedValue(LayeredNode v) {
        return !v.isDummy() && v instanceof NodeWrapper && ((NodeWrapper) v).wrappedNode() instanceof LayeredGNode &&
                ((LayeredGNode) ((NodeWrapper) v).wrappedNode()).value() instanceof Value;
    }

    /**
     * @param node        A node in the graph. It will be drawn in GUI, e.g. a DataButton or SquareButton.
     * @param v           {@link Value}
     * @param showValue   whether to show the value
     * @return    A nice-look string or number, if it is too long,
     *            then either truncate or round or convert to scientific notation.
     */
    public static String getNodeString(LayeredGNode node, Value v, boolean showValue) {

        Object value = v.value();

        String name = v.getId();
        if (node.getSuccessors().size() == 1 && v.isAnonymous()) {
//            name = "[" + ((Generator) ((LayeredGNode) node.getSuccessors().get(0)).value()).getParamName(v) + "]";
            // https://github.com/LinguaPhylo/linguaPhylo/issues/249
            name = ((Generator) ((LayeredGNode) node.getSuccessors().get(0)).value()).getParamName(v);
        }
        if (name == null)
            name = "null";
        else if (name.trim().equals("0") || name.trim().equals("1") || name.trim().equals("2"))
            name = ""; // suppress unnamed argument

        String displayName = name;

        if (displayName.length() > 10)
            displayName = displayName.substring(0, 8) + "...";
        if (displayName.length() > 7)
            displayName = "<small>" + displayName + "</small>";

        if (ValueUtils.isMultiDimensional(value)) {
            if (displayName.isEmpty()) // if name is empty, then use code string
                displayName = v.codeString();
            return "<html><center><p><b>" + displayName + "</b></p></center></html>";
        }

        String valueString = "";
        if (showValue) {
            // if id is the value, then not show id
            if (name.equals(value.toString()))
                displayName = "";
            // process value in string
            if (value instanceof Number number) {
//                valueString = format.format(value);
                valueString = MathUtils.formatScientific(number.doubleValue(), SIGNIFICANT_FIGURE);
                // more work on int, e.g. 1000 to 1E3, not 1.00E3
                if (value instanceof Integer)
                    valueString = valueString.replaceAll("\\.0+E", "E");
            } else {
                valueString = value.toString();
//                if (value instanceof String) {
                    if (valueString.length() > 8) {
                        valueString = valueString.length() + " chars";
                        if (valueString.length() > 10) {
                            valueString = "string";
                        }
                    }
//                }
            }
            valueString = "<p><font color=\"#808080\" ><small>" + valueString + "</small></font></p>";
        }

        return "<html><center><p>" + displayName + "</p>" + valueString + "</center></html>";
    }


}
