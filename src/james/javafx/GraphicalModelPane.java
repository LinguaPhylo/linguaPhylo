package james.javafx;

import james.graphicalModel.GenerativeDistribution;
import james.graphicalModel.RandomVariable;
import james.graphicalModel.Value;
import james.swing.NodeVisitor;
import javafx.beans.value.ChangeListener;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by adru001 on 18/12/19.
 */
public class GraphicalModelPane extends AnchorPane {

    RandomVariable variable;

    double HSPACE = 120;
    double VSPACE = 100;

    double VAR_SIZE = 68;

    double FACTOR_SIZE = 7;
    double FACTOR_LABEL_GAP = 10;
    double STROKE_SIZE = 1;
    double ARROWHEAD_DEPTH = 8;
    double ARROWHEAD_WIDTH = 5;

    double WIDTH = 800;
    double HEIGHT = 800;

    double BORDER = 40;

    Map<Value, Button> buttonMap;

    Canvas canvas;

    public GraphicalModelPane(RandomVariable variable) {
        this.variable = variable;

        buttonMap = new HashMap<>();

        canvas = new Canvas();
        setLeftAnchor(canvas, 0.0);
        setTopAnchor(canvas, 0.0);
        getChildren().add(canvas);

        canvas.setHeight(getHeight());
        canvas.setWidth(getWidth());

        NodeVisitor canvasVisitor = new NodeVisitor() {
            @Override
            public void visitValue(Value value, Point2D p, Point2D q) {
                if (q != null) {

                    double x1 = p.getX();
                    double y1 = p.getY() + VAR_SIZE / 2;
                    double x2 = q.getX();
                    double y2 = q.getY() - FACTOR_SIZE;

                    drawArrowLine(canvas.getGraphicsContext2D(), x1, y1, x2, y2, 0, 0);
                }
            }

            @Override
            public void visitGenEdge(GenerativeDistribution genDist, Point2D p, Point2D q) {
                String str = genDist.getName();

                GraphicsContext g2d = canvas.getGraphicsContext2D();

                g2d.fillText(str, (float) (p.getX() + FACTOR_SIZE + FACTOR_LABEL_GAP), (float) (p.getY() + FACTOR_SIZE - STROKE_SIZE));

                double x1 = p.getX();
                double y1 = p.getY() + FACTOR_SIZE;
                double x2 = q.getX();
                double y2 = q.getY() - VAR_SIZE / 2;

                g2d.fillRect(x1 - FACTOR_SIZE, y1 - FACTOR_SIZE * 2, FACTOR_SIZE * 2, FACTOR_SIZE * 2);

                drawArrowLine(g2d, x1, y1, x2, y2, ARROWHEAD_DEPTH, ARROWHEAD_WIDTH);

            }
        };

        NodeVisitor buttonVisitor = new NodeVisitor() {
            @Override
            public void visitValue(Value value, Point2D p, Point2D q) {
                String str = value.getId();

                if (!(value instanceof RandomVariable)) {
                    str = value.toString();
                }
                Button button = buttonMap.get(value);
                if (button == null) {

                    button = new Button(str);

                    button.setShape(new Circle(VAR_SIZE/2.0));
                    button.setPrefSize((int) VAR_SIZE, (int) VAR_SIZE);
                    //button.setMaxSize(3,3);

                    buttonMap.put(value, button);

                    if (value instanceof RandomVariable) {
                        button.getStyleClass().add("random-variable-button");
                    }

                    getChildren().add(button);
                }
                setTopAnchor(button, (p.getY() - VAR_SIZE / 2));
                setLeftAnchor(button, (p.getX() - VAR_SIZE / 2));
            }

            @Override
            public void visitGenEdge(GenerativeDistribution genDist, Point2D p, Point2D q) {

            }
        };

        traverseGraphicalModel(variable, getStartPoint(), null, buttonVisitor);

        ChangeListener<Number> sizeListener = (observable, oldValue, newValue) -> {
            canvas.setWidth(getWidth());
            canvas.setHeight(getHeight());
            WIDTH = canvas.getWidth();
            HEIGHT = canvas.getHeight();

            canvas.getGraphicsContext2D().setFill(Color.WHITE);
            canvas.getGraphicsContext2D().fillRect(0,0,WIDTH,HEIGHT);
            canvas.getGraphicsContext2D().setFill(Color.BLACK);
            traverseGraphicalModel(variable, getStartPoint(), null, buttonVisitor);
            traverseGraphicalModel(variable, getStartPoint(), null, canvasVisitor);
        };

        widthProperty(). addListener(sizeListener);

        heightProperty().addListener(sizeListener);

    }

    private Point2D getStartPoint() {
        return new Point2D.Double(WIDTH / 2.0, HEIGHT - BORDER - VAR_SIZE/2);
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

        for (Value value : map.values()) {
            Point2D p1 = null;
            if (p != null) p1 = new Point2D.Double(x, p.getY() - VSPACE);
            traverseGraphicalModel(value, p1, p, visitor);
            x += HSPACE;
        }
    }

    /**
     * Draw an arrow line between two points.
     *
     * @param g the graphics component.
     * @param d the width of the arrow.
     * @param h the height of the arrow.
     */
    private void drawArrowLine(GraphicsContext g, double x1, double y1, double x2, double y2, double d, double h) {

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

        g.setStroke(Color.BLACK);
        g.setLineWidth(STROKE_SIZE);
        g.strokeLine(x1,y1,x2,y2);


        g.beginPath();
        g.moveTo(x2, y2);
        g.lineTo(xm, ym);
        g.lineTo(xn, yn);
        g.closePath();
        g.fill();
    }
}