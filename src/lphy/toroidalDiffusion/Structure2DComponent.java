package lphy.toroidalDiffusion;

import lphy.core.distributions.Normal;
import lphy.core.distributions.OrnsteinUhlenbeck;
import lphy.core.distributions.Utils;
import lphy.graphicalModel.Value;
import org.graphstream.graph.Structure;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Random;

public class Structure2DComponent extends JComponent {

    final double nodeRadius = 5;

    Structure2D initialStructure;
    Structure2D[] angularStructures = new Structure2D[4];
    Structure2D[] cartesianStructures = new Structure2D[4];

    OrnsteinUhlenbeck[] ornsteinUhlenbeck;

    Random random = new Random(777);

    public Structure2DComponent(Structure2D initialStructure) {
        this.initialStructure = initialStructure;

        ornsteinUhlenbeck = new OrnsteinUhlenbeck[initialStructure.angles.length];

        for (int i = 0; i < ornsteinUhlenbeck.length; i++) {

            Value<Double> startAngle = new Value<>("x", Math.PI);
            Value<Double> time = new Value<>("time", 0.5);
            Value<Double> diffRate = new Value<>("diffRate", 0.5);
            Value<Double> theta = new Value<>("theta", initialStructure.angles[i]);
            Value<Double> alpha = new Value<>("alpha", 1.0);

            ornsteinUhlenbeck[i] = new OrnsteinUhlenbeck(startAngle, time, diffRate, theta, alpha);
        }

        double sd = initialStructure.bondLength[0]*0.5;

        double t = 0.2;

        for (int steps = 0; steps < angularStructures.length; steps++) {

            angularStructures[steps] = evolveAngular(steps == 0 ? initialStructure : angularStructures[steps-1], ornsteinUhlenbeck, t);

            cartesianStructures[steps] = evolveCartesian(initialStructure, steps == 0 ? initialStructure : cartesianStructures[steps-1], sd*sd, t);
        }
    }

    private Structure2D evolveCartesian(Structure2D original, Structure2D initial, double diff, double t) {

        ArrayList<Point2D> newPoints = new ArrayList<>();

        for (int i = 0; i < initial.coordinates.size(); i++) {
            newPoints.add(OUPoint(original.coordinates.get(i), initial.coordinates.get(i), diff, t));
        }
        return new Structure2D(newPoints);
    }

    private Point2D OUPoint(Point2D o, Point2D p, double diff, double t) {

        Value<Double> start = new Value<>("x", p.getX());
        Value<Double> time = new Value<>("time", t);
        Value<Double> diffRate = new Value<>("diffRate", diff);
        Value<Double> theta = new Value<>("theta", o.getX());
        Value<Double> alpha = new Value<>("alpha", 0.2);

        OrnsteinUhlenbeck ou = new OrnsteinUhlenbeck(start, time, diffRate, theta, alpha);

        double nx = ou.sample().value();

        start = new Value<>("x", p.getY());
        theta = new Value<>("theta", o.getY());

        ou = new OrnsteinUhlenbeck(start, time, diffRate, theta, alpha);

        double ny = ou.sample().value();

        return new Point2D.Double(nx, ny);
    }

    private Structure2D evolveAngular(Structure2D structure, OrnsteinUhlenbeck[] ornsteinUhlenbeck, double time) {

        double[] newAngles = new double[structure.angles.length];
        for (int i = 0; i < newAngles.length; i++) {
            ornsteinUhlenbeck[i].setParam("time", new Value<>("x", time));
            ornsteinUhlenbeck[i].setParam("y0", new Value<>("x", structure.angles[i]));
            newAngles[i] = ornsteinUhlenbeck[i].sample().value();
        }
        return new Structure2D(newAngles, structure.bondLength);
    }

    public void paintComponent(Graphics g) {

        int width = getWidth();
        int height = getHeight();

        g.setColor(Color.white);
        g.fillRect(0,0,width,height);

        double dx = width / 6.0;
        double x = dx/2.0;

        Point2D point = new Point2D.Double(x, height / 2.0 - initialStructure.bondLength[0]);

        initialStructure.setOrigin(point);

        double angularY = height / 3.0 - initialStructure.bondLength[0];
        double cartesianY = 2 * height / 3.0 - initialStructure.bondLength[0];

        Graphics2D g2d = (Graphics2D) g;

        drawStructure(g2d, initialStructure, Color.black, new BasicStroke(3.0f));
        g2d.drawString("t = 0", (int)point.getX(), (int)point.getY()-20);

        g2d.drawString("Evolution of bond angles", (int)(x +dx), (int)(angularY-initialStructure.bondLength[0]));

        g2d.drawString("Evolution of Cartesian coordinates", (int)(x +dx), (int)(cartesianY-initialStructure.bondLength[0]));

        for (int i = 0; i < angularStructures.length; i++) {

            x += dx;

            Structure2D prevStructure;
            if (i == 0) {
                prevStructure = initialStructure;
                prevStructure.setOrigin(new Point2D.Double(x, angularY));
            } else {
                prevStructure = angularStructures[i-1];
                prevStructure.shiftX(dx);
            }

            drawStructure(g2d, prevStructure, Color.lightGray, new BasicStroke(1.0f));

            angularStructures[i].setOrigin(new Point2D.Double(x, angularY));
            angularStructures[i].minimizeDistance(prevStructure);
            drawStructure(g2d, angularStructures[i], Color.blue, new BasicStroke(2.0f));
            g2d.drawString("t = " + (i+1), (int)x, (int)angularY-20);

            if (i == 0) {
                prevStructure = initialStructure;
                prevStructure.setOrigin(new Point2D.Double(x, cartesianY));
            } else {
                prevStructure = cartesianStructures[i-1];
                prevStructure.shiftX(dx);
            }

            drawStructure(g2d, prevStructure, Color.lightGray, new BasicStroke(1.0f));
            //}

            cartesianStructures[i].setOrigin(new Point2D.Double(x, cartesianY));
            cartesianStructures[i].minimizeDistance(prevStructure);
            drawStructure(g2d, cartesianStructures[i], Color.red, new BasicStroke(2.0f));
            g2d.drawString("t = " + (i+1), (int)x, (int)cartesianY-20);
        }

    }

    private void drawStructure(Graphics2D g2d, Structure2D structure2d, Color color, Stroke stroke) {

        g2d.setStroke(stroke);
        g2d.setColor(color);
        g2d.draw(createPath(structure2d));
        for (Point2D p : structure2d.coordinates) {
            g2d.fill(new Ellipse2D.Double(p.getX()-nodeRadius, p.getY()-nodeRadius, nodeRadius*2, nodeRadius*2));
        }
    }

    private GeneralPath createPath(Structure2D structure2D) {
        GeneralPath path = new GeneralPath();

        Point2D origin = structure2D.origin;

        path.moveTo(origin.getX(), origin.getY());
        for (Point2D point : structure2D.coordinates) {
            path.lineTo(point.getX(), point.getY());
        }
        return path;
    }

    public static void main(String[] args) {

        Utils.getRandom().setSeed(777);

        double blength = 60;

        double[] angles = new double[] {0, Math.PI/2.0, Math.PI/2.0, -Math.PI/2.0, -Math.PI/2.0};
        double[] bondLength = new double[] {blength, blength, blength, blength, blength};

        Structure2D structure = new Structure2D(angles, bondLength);

        Structure2DComponent structure2D = new Structure2DComponent(structure);

        JFrame frame = new JFrame("Structure 2D");
        frame.getContentPane().add(structure2D);
        frame.setSize(1000, 800);
        frame.setVisible(true);

    }
}
