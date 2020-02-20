package james.app;

import james.graphicalModel.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

import static james.app.RenderNode.*;

/**
 * Created by adru001 on 18/12/19.
 */
public class GraphicalModelComponent extends JComponent implements GraphicalModelChangeListener {

    GraphicalModelParser parser;

    double ARROWHEAD_WIDTH = 4;
    double ARROWHEAD_DEPTH = 10;

    float STROKE_SIZE = 1.0f;

    List<GraphicalModelListener> listeners = new ArrayList<>();

    RenderNodePool pool;

    RenderNode selectedNode;

    boolean sizeChanged = true;
    boolean showArgumentLabels = false;

    public GraphicalModelComponent(GraphicalModelParser parser) {
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

    void shiftLeft() {

        pool.shiftLeft(selectedNode);
        sizeChanged = true;
        repaint();

    }

    public void shiftRight() {
        pool.shiftRight(selectedNode);
        sizeChanged = true;
        repaint();
    }

    public void setShowArgumentLabels(boolean show) {
        showArgumentLabels = show;
        repaint();
    }

    private void setup() {

        removeAll();
        pool = new RenderNodePool();

        for (Value val : parser.getRoots()) {
            pool.addRoot(val);
        }
        
        for (RenderNode node : pool.getRenderNodes()) {

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

    public void paintComponent(Graphics g) {

        double delta = g.getFontMetrics().getAscent()/2.0;

        if (sizeChanged) {
            pool.locateAll(getWidth(), getHeight());
            sizeChanged = false;
        }

        Graphics2D g2d = (Graphics2D) g;

        g.setColor(Color.white);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.black);

        g2d.setStroke(new BasicStroke(STROKE_SIZE));

        for (RenderNode node : pool.getRenderNodes()) {

            if (node.value() instanceof Value) {

                double x1 = node.point.getX();
                double y1 = node.point.getY() + VAR_HEIGHT / 2;

                for (RenderNode parent : (List<RenderNode>) node.outputs) {

                    if (parent != pool.rootNode) {
                        double x2 = parent.point.getX();
                        double y2 = parent.point.getY() - FACTOR_SIZE;
                        drawArrowLine(g2d, x1, y1, x2, y2, 0, 0);
                        if (showArgumentLabels) {
                            String label = ((Parameterized)parent.value()).getParamName((Value)node.value());
                            g.setColor(Color.gray);
                            g.drawString(label, (int)Math.round((x1+x2)/2.0 - g.getFontMetrics().stringWidth(label)/2.0), (int)Math.round((y1+y2)/2.0+delta));
                            g.setColor(Color.black);
                        }
                    }
                }
            } else if (node.value() instanceof Parameterized) {
                Parameterized gen = (Parameterized) node.value();

                String str = gen.getName();
                Point2D p = node.point;
                Point2D q = ((List<RenderNode>) node.outputs).get(0).point;


                g2d.drawString(str, (float) (p.getX() + FACTOR_SIZE + FACTOR_LABEL_GAP), (float) (p.getY() + FACTOR_SIZE - STROKE_SIZE));

                double x1 = p.getX();
                double y1 = p.getY() + FACTOR_SIZE;
                double x2 = q.getX();
                double y2 = q.getY() - VAR_HEIGHT / 2;

                //Rectangle2D rect = new Rectangle2D.Double(x1 - FACTOR_SIZE, y1 - FACTOR_SIZE * 2, FACTOR_SIZE * 2, FACTOR_SIZE * 2);
                //g2d.fill(rect);

                drawArrowLine(g2d, x1, y1, x2, y2, ARROWHEAD_DEPTH, ARROWHEAD_WIDTH);
            }
        }
    }

    /**
     * Draw an arrow line between two points.
     *
     * @param g the graphics component.
     * @param d the width of the arrow.
     * @param h the height of the arrow.
     */
    private void drawArrowLine(Graphics2D g, double x1, double y1, double x2, double y2, double d, double h) {

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

    @Override
    public void modelChanged() {
        setup();
        repaint();
    }
}