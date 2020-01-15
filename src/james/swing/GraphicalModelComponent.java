package james.swing;

import james.graphicalModel.GenerativeDistribution;
import james.graphicalModel.RandomVariable;
import james.graphicalModel.Value;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

/**
 * Created by adru001 on 18/12/19.
 */
public class GraphicalModelComponent extends JComponent {

    RandomVariable variable;

    double HSPACE = 120;
    double VSPACE = 100;

    double FACTOR_SIZE = 7;
    double FACTOR_LABEL_GAP = 10;

    double VAR_WIDTH = 100;
    double VAR_HEIGHT = 60;

    double ARROWHEAD_WIDTH = 4;
    double ARROWHEAD_DEPTH = 10;

    double BORDER = 20;

    float STROKE_SIZE = 1.0f;

    Map<Value, JButton> buttonMap;

    List<GraphicalModelListener> listeners = new ArrayList<>();

    public GraphicalModelComponent(RandomVariable v) {
        this.variable = v;

        buttonMap = new HashMap<>();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                generateButtons();
            }
        });
    }

    public void addGraphicalModelListener(GraphicalModelListener listener) {
        listeners.add(listener);
    }

    private Point2D getStartPoint() {
        return new Point2D.Double(getWidth() / 2.0, getHeight() - BORDER - VAR_HEIGHT);
    }

    private void traverseGraphicalModel(Value value, Point2D currentP, Point2D prevP, NodeVisitor visitor) {

        visitor.visitValue(value, currentP, prevP);

        if (value instanceof RandomVariable) {
            // recursion
            Point2D newP = null;
            if (currentP != null) {
                newP = new Point2D.Double(currentP.getX(), currentP.getY() - VSPACE);
            }
            traverseGraphicalModel(((RandomVariable) value).getGenerativeDistribution(), newP, currentP, visitor);
        }
    }

    private void traverseGraphicalModel(GenerativeDistribution genDist, Point2D p, Point2D q, NodeVisitor visitor) {

        visitor.visitGenEdge(genDist, p, q);

        Map<String, Value> map = genDist.getParams();

        double width = (map.size() - 1) * HSPACE;
        double x = 0;
        if (p != null) x = p.getX() - width / 2.0;

        for (Map.Entry<String,Value> e : map.entrySet()) {
            Point2D p1 = null;
            if (p != null) p1 = new Point2D.Double(x, p.getY() - VSPACE);
            traverseGraphicalModel(e.getValue(), p1, p, visitor);
            x += HSPACE;
        }
    }

    public void paintComponent(Graphics g) {

        Graphics2D g2d = (Graphics2D) g;

        g.setColor(Color.white);
        g.fillRect(0,0,getWidth(), getHeight());
        g.setColor(Color.black);

        g2d.setStroke(new BasicStroke(STROKE_SIZE));
        
        traverseGraphicalModel(variable, getStartPoint(), null, new NodeVisitor() {
            @Override
            public void visitValue(Value value, Point2D p, Point2D q) {
                if (q != null) {

                    double x1 = p.getX();
                    double y1 = p.getY() + VAR_HEIGHT / 2;
                    double x2 = q.getX();
                    double y2 = q.getY() - FACTOR_SIZE;

                    drawArrowLine(g2d, x1, y1, x2, y2, 0, 0);
                }
            }

            @Override
            public void visitGenEdge(GenerativeDistribution genDist, Point2D p, Point2D q) {
                String str = genDist.getName();

                g2d.drawString(str, (float) (p.getX() + FACTOR_SIZE + FACTOR_LABEL_GAP), (float) (p.getY() + FACTOR_SIZE - STROKE_SIZE));

                double x1 = p.getX();
                double y1 = p.getY() + FACTOR_SIZE;
                double x2 = q.getX();
                double y2 = q.getY() - VAR_HEIGHT / 2;

                Rectangle2D rect = new Rectangle2D.Double(x1 - FACTOR_SIZE, y1 - FACTOR_SIZE * 2, FACTOR_SIZE * 2, FACTOR_SIZE * 2);

                g2d.fill(rect);

                drawArrowLine(g2d, x1, y1, x2, y2, ARROWHEAD_DEPTH, ARROWHEAD_WIDTH);

            }
        });
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

    private String displayString(Value v) {
        return "<html><font color=\"#808080\"><small>" + v.getId() + ":</small></font>" + v.value().toString() + "</html>";
    }

    private void generateButtons() {
        traverseGraphicalModel(variable, getStartPoint(), null, new NodeVisitor() {
            @Override
            public void visitValue(Value value, Point2D p, Point2D q) {
                String str = value.getId();
                Color backgroundColor = new Color(0.0f, 1.0f, 0.0f, 0.5f);
                Color borderColor = new Color(0.0f, 0.75f, 0.0f, 1.0f);

                if (!(value instanceof RandomVariable)) {
                    str = displayString(value);
                    backgroundColor = Color.white;
                    borderColor = Color.black;
                }

                JButton button = buttonMap.get(value);
                if (button == null) {
                    button = new CircleButton(str, backgroundColor, borderColor);
                    button.addActionListener(e1 -> {
                        for (GraphicalModelListener listener : listeners) {
                            listener.valueSelected(value);
                        }
                    });
                    buttonMap.put(value, button);
                    add(button);
                }
                button.setLocation((int) (p.getX() - VAR_WIDTH / 2), (int) (p.getY() - VAR_HEIGHT / 2));
                button.setSize((int) VAR_WIDTH, (int) VAR_HEIGHT);
            }

            @Override
            public void visitGenEdge(GenerativeDistribution genDist, Point2D p, Point2D q) {

            }
        });
    }

    private void removeButtons() {
        for (JButton button: buttonMap.values()) {
            remove(button);
        }
        buttonMap.clear();
    }

    public void setVariable(RandomVariable randomVariable) {
        variable = randomVariable;
        removeButtons();
        generateButtons();
        repaint();
    }
}