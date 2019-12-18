package james.swing;

import james.Coalescent;
import james.TimeTree;
import james.core.Exp;
import james.graphicalModel.GenerativeDistribution;
import james.graphicalModel.RandomVariable;
import james.graphicalModel.Value;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Random;

/**
 * Created by adru001 on 18/12/19.
 */
public class GraphicalModelComponent extends JComponent {

    RandomVariable variable;

    double HSPACE = 120;
    double VSPACE = 100;

    double FACTOR_SIZE = 7;
    double FACTOR_LABEL_GAP = 10;

    double VAR_SIZE = 15;

    double ARROWHEAD_WIDTH = 4;
    double ARROWHEAD_DEPTH = 10;

    double LINE_SPACING = 20;

    double BORDER = 20;

    float STROKE_SIZE = 1.0f;

    Color randomVariableColor = new Color(0.0f,1.0f,0.0f, 0.5f);

    public GraphicalModelComponent(RandomVariable variable) {
        this.variable = variable;
    }

    public void paintComponent(Graphics g) {

        Point2D p = new Point2D.Double(getWidth()/2.0,getHeight()-BORDER-VAR_SIZE);

        Graphics2D g2d = (Graphics2D)g;

        g2d.setStroke(new BasicStroke(STROKE_SIZE));

        paintValue(variable, p, null, g2d);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        variable.print(pw);
        pw.flush();

        drawStrings(sw.toString(), (float)BORDER, (float)LINE_SPACING, (Graphics2D)g);

    }

    private void drawStrings(String s, float x, float y, Graphics2D g) {
        String[] lines = s.split("\n");

        for (String line : lines) {
            g.drawString(line,x,y);
            y += LINE_SPACING;
        }
    }

    private void paintValue(Value value, Point2D p, Point2D q, Graphics2D g) {

        String str = value.getName();

        if (value instanceof RandomVariable) {
            g.setColor(randomVariableColor);

            Ellipse2D ellipse2D = new Ellipse2D.Double(p.getX()-VAR_SIZE, p.getY()-VAR_SIZE, VAR_SIZE*2, VAR_SIZE*2);
            g.fill(ellipse2D);
            g.setColor(Color.black);
            g.draw(ellipse2D);
            Point2D p1 = new Point2D.Double(p.getX(), p.getY()-VSPACE);
            paintGenerativeFunction(((RandomVariable) value).getGenerativeDistribution(), p1, p, g);
        } else {
            str = value.toString();
        }

        double width = g.getFontMetrics().stringWidth(str);

        double height = g.getFontMetrics().getAscent();

        g.drawString(str, (float)(p.getX()-(width/2.0)), (float)(p.getY()+height/2.0-STROKE_SIZE));

        if (q != null) {

            double x1 = p.getX();
            double y1 = p.getY()+VAR_SIZE;
            double x2 = q.getX();
            double y2 = q.getY()-FACTOR_SIZE;

            drawArrowLine(g,x1,y1,x2,y2,0, 0);
        }
    }

    private void paintGenerativeFunction(GenerativeDistribution generativeDistribution, Point2D p, Point2D q,  Graphics2D g) {

        String str = generativeDistribution.getName();

        g.drawString(str, (float)(p.getX()+FACTOR_SIZE+FACTOR_LABEL_GAP), (float)(p.getY()+FACTOR_SIZE-STROKE_SIZE));

        double x1 = p.getX();
        double y1 = p.getY()+FACTOR_SIZE;
        double x2 = q.getX();
        double y2 = q.getY()-VAR_SIZE;

        Rectangle2D rect = new Rectangle2D.Double(x1-FACTOR_SIZE, y1-FACTOR_SIZE*2, FACTOR_SIZE*2, FACTOR_SIZE * 2);

        g.fill(rect);

        drawArrowLine(g, x1, y1, x2, y2, ARROWHEAD_DEPTH, ARROWHEAD_WIDTH);

        List<Value> values = generativeDistribution.getParams();

        double width = (values.size()-1)*HSPACE;
        double x = p.getX()-width/2.0;

        for (Value value : values) {
            Point2D p1 = new Point2D.Double(x, p.getY()-VSPACE);
            paintValue(value, p1, p, g);
            x += HSPACE;
        }
    }

    /**
     * Draw an arrow line between two points.
     * @param g the graphics component.
     * @param d  the width of the arrow.
     * @param h  the height of the arrow.
     */
    private void drawArrowLine(Graphics2D g, double x1, double y1, double x2, double y2, double d, double h) {

        double dx = x2 - x1, dy = y2 - y1;
        double D = Math.sqrt(dx*dx + dy*dy);
        double xm = D - d, xn = xm, ym = h, yn = -h, x;
        double sin = dy / D, cos = dx / D;

        x = xm*cos - ym*sin + x1;
        ym = xm*sin + ym*cos + y1;
        xm = x;

        x = xn*cos - yn*sin + x1;
        yn = xn*sin + yn*cos + y1;
        xn = x;

        Line2D.Double line = new Line2D.Double(x1,y1,x2,y2);

        GeneralPath p = new GeneralPath();
        p.moveTo(x2,y2);
        p.lineTo(xm,ym);
        p.lineTo(xn,yn);
        p.closePath();


        g.draw(line);
        g.fill(p);
    }

    public static void main(String[] args) {

        Random random = new Random();

        Value<Double> thetaExpPriorRate = new Value<>("r", 20.0);
        Exp exp = new Exp(thetaExpPriorRate, random);

        RandomVariable<Double> theta = exp.sample("\u0398");
        Value<Integer> n = new Value<>("n", 20);

        Coalescent coalescent = new Coalescent(theta, n, random);

        RandomVariable<TimeTree> g = coalescent.sample();

        PrintWriter p = new PrintWriter(System.out);
        g.print(p);

        GraphicalModelComponent comp = new GraphicalModelComponent(g);
        comp.setPreferredSize(new Dimension(800,600));

        JFrame frame = new JFrame("Graphical Models");
        frame.getContentPane().add(comp,BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);

    }

}
