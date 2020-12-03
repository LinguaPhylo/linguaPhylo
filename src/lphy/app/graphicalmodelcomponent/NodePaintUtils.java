package lphy.app.graphicalmodelcomponent;

import lphy.core.distributions.VectorizedDistribution;
import lphy.core.functions.VectorizedFunction;
import lphy.graphicalModel.Generator;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.Vector;

import java.awt.*;
import java.awt.geom.*;

import static lphy.app.graphicalmodelcomponent.LayeredGNode.*;

public class NodePaintUtils {

    private static float FACTOR_LABEL_FONT_SIZE = 11.0f;
    private static double ARROWHEAD_WIDTH = 4;
    private static double ARROWHEAD_DEPTH = 10;


    public static void paintNode(LayeredNode properNode, Graphics2D g2d) {
        NodeWrapper nodeWrapper = (NodeWrapper) properNode;

        if (!nodeWrapper.isDummy()) {
            LayeredGNode node = (LayeredGNode) nodeWrapper.wrappedNode();

            if (node.value() instanceof Value) {
                paintValueNode((Value) node.value(), properNode, g2d);
            } else if (node.value() instanceof Generator) {
                paintGeneratorNode((Generator) node.value(), node, properNode, g2d);
            }
        }
    }

    private static void paintValueNode(Value value, LayeredNode properNode, Graphics2D g2d) {
        Ellipse2D ellipse2D = new Ellipse2D.Double(properNode.getX() - VAR_HEIGHT/2.0, properNode.getY() - VAR_HEIGHT/2.0, VAR_HEIGHT, VAR_HEIGHT);
        g2d.setColor(Color.white);
        g2d.fill(ellipse2D);
        g2d.setColor(Color.black);
        g2d.draw(ellipse2D);
    }

    private static void paintGeneratorNode(Generator generator, LayeredGNode node, LayeredNode properNode, Graphics2D g2d) {
        Rectangle2D rectangle2D = new Rectangle2D.Double(node.x - FACTOR_SIZE, node.y - FACTOR_SIZE, FACTOR_SIZE*2.0, FACTOR_SIZE*2.0);
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
                    y2 -= FACTOR_SIZE;
                } else if (isWrappedValue(successor)) {
                    y2 -= VAR_HEIGHT / 2;
                }
                drawLine(g2d, x1, y1, x2, y2);
            }
        }
    }

    /**
     * @param value the value to paint the node of
     * @param properNode the proper node representing this generator
     * @param g2d the Graphics2D context to paint to
     */
    private static void paintValueNodeEdges(Value value, LayeredNode properNode, Graphics2D g2d, boolean showArgumentLabels, boolean straightLines) {

        double x1 = properNode.getX();
        double y1 = properNode.getY() + VAR_HEIGHT / 2;

        double delta = g2d.getFontMetrics().getAscent() / 2.0;

        for (LayeredNode successor : properNode.getSuccessors()) {

            if (straightLines) {
                successor = getUnwrappedNonDummySuccessor(successor);
            }

            double x2 = successor.getX();
            double y2 = successor.getY() - (successor.isDummy() ? 0.0 : FACTOR_SIZE);
            drawLine(g2d, x1, y1, x2, y2);
            if (showArgumentLabels) {
                Generator generator = null;
                if (straightLines) {
                    generator = (Generator)successor;
                } else generator = (Generator)getUnwrappedNonDummySuccessor(successor);


                String label = generator.getParamName(value);
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
    private static void paintGeneratorNodeEdges(Generator generator, LayeredNode node, LayeredNode properNode, Graphics2D g2d) {

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

}
