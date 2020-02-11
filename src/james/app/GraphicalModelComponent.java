package james.app;

import james.graphicalModel.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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
        parser.addGraphicalModelChangeListener(() -> setup());
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



    private void setup() {
        removeAll();
        pool = new RenderNodePool();

        for (Value val : parser.getRoots()) {
            pool.addRoot(val);
        }
        
        for (RenderNode node : pool.getRenderNodes()) {

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
        sizeChanged = true;
    }

    public void addGraphicalModelListener(GraphicalModelListener listener) {
        listeners.add(listener);
    }

    public void paintComponent(Graphics g) {

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

                    double x2 = parent.point.getX();
                    double y2 = parent.point.getY() - FACTOR_SIZE;

                    drawArrowLine(g2d, x1, y1, x2, y2, 0, 0);
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

//    private void generateButtons() {
//        List<Value> valueList = new ArrayList<>(parser.getRoots());
//
//        for (int i = 0; i < valueList.size(); i++) {
//            traverseGraphicalModel(valueList.get(i), getStartPoint((i + 1.0) / (valueList.size() + 1.0)), null, 1, new NodeVisitor() {
//                @Override
//                public void visitValue(Value value, Point2D p, Point2D q, int level) {
//                    Color backgroundColor = new Color(0.0f, 1.0f, 0.0f, 0.5f);
//                    Color borderColor = new Color(0.0f, 0.75f, 0.0f, 1.0f);
//
//                    if (ValueUtils.isFixedValue(value)) {
//                        backgroundColor = Color.white;
//                        borderColor = Color.black;
//                    } else if (ValueUtils.isValueOfFunction(value)) {
//                        backgroundColor = new Color(1.0f, 0.0f, 0.0f, 0.5f);
//                        borderColor = new Color(0.75f, 0.0f, 0.0f, 1.0f);
//                    }
//                    if (!parser.getDictionary().values().contains(value)) {
//                        backgroundColor = backgroundColor.darker();
//                        borderColor = Color.black;
//                    }
//
//
//                    String str = getButtonString(value);
//
//                    JButton button = buttonMap.get(value);
//                    if (button == null) {
//                        if (value.getFunction() != null) {
//                            button = new DiamondButton(str, backgroundColor, borderColor);
//                        } else {
//
//                            button = new CircleButton(str, backgroundColor, borderColor);
//                        }
//                        button.addActionListener(e1 -> {
//                            for (GraphicalModelListener listener : listeners) {
//                                listener.valueSelected(value);
//                            }
//                        });
//
//                        buttonMap.put(value, button);
//                        add(button);
//
//                        JButton finalButton = button;
//                        value.addValueListener(() -> finalButton.setText(getButtonString(value)));
//                    }
//                    button.setSize((int) VAR_WIDTH, (int) VAR_HEIGHT);
//                    button.setLocation((int) (p.getX() - VAR_WIDTH / 2), (int) (p.getY() - VAR_HEIGHT / 2));
//                }
//
//                @Override
//                public void visitGenEdge(GenerativeDistribution genDist, Point2D p, Point2D q, int level) {
//                    String dtr = genDist.getName();
//
//                    JButton button = buttonMap.get(genDist.codeString());
//                    if (button == null) {
//                        button = new JButton("");
//
//                        button.addActionListener(e -> {
//                            for (GraphicalModelListener listener : listeners) {
//                                listener.generativeDistributionSelected(genDist);
//                            }
//                        });
//
//                        buttonMap.put(genDist.codeString(), button);
//
//                        add(button);
//                    }
//
//                    button.setLocation((int) (p.getX() - FACTOR_SIZE), (int) (p.getY() - FACTOR_SIZE));
//                    button.setSize((int) FACTOR_SIZE * 2, (int) FACTOR_SIZE * 2);
//
//
//                }
//
//                @Override
//                public void visitFunctionEdge(DeterministicFunction function, Point2D p, Point2D q, int level) {
//                    JButton button = buttonMap.get(function.codeString());
//                    if (button == null) {
//                        button = new JButton("");
//
//                        button.addActionListener(e -> {
//                            for (GraphicalModelListener listener : listeners) {
//                                listener.functionSelected(function);
//                            }
//                        });
//
//                        buttonMap.put(function.codeString(), button);
//
//                        add(button);
//                    }
//
//                    button.setLocation((int) (p.getX() - FACTOR_SIZE), (int) (p.getY() - FACTOR_SIZE));
//                    button.setSize((int) FACTOR_SIZE * 2, (int) FACTOR_SIZE * 2);
//                }
//            });
//        }
//    }

//    private void removeButtons() {
//        for (JButton button : buttonMap.values()) {
//            remove(button);
//            for (ActionListener al : button.getActionListeners()) {
//                button.removeActionListener(al);
//            }
//        }
//        buttonMap.clear();
//    }

    @Override
    public void modelChanged() {
//        recomputeSizes();
//        removeButtons();
//        generateButtons();
//        repaint();
    }
}