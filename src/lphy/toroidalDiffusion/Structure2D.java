package lphy.toroidalDiffusion;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.MultivariateOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.graphstream.graph.Structure;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Structure2D {

    Point2D origin;
    double[] angles;
    double[] bondLength;

    List<Point2D> coordinates;


    public Structure2D(double[] angles, double bondLength) {
        this.angles = new double[angles.length];
        System.arraycopy(angles, 0, this.angles, 0, angles.length);

        this.bondLength = new double[angles.length];
        Arrays.fill(this.bondLength, bondLength);
        origin = new Point2D.Double(0.0, 0.0);
        coordinates = createCoordinates();
    }

    public Structure2D(double[] angles, double[] bondLength) {
        this.angles = new double[angles.length];
        System.arraycopy(angles, 0, this.angles, 0, angles.length);
        this.bondLength = new double[bondLength.length];
        System.arraycopy(bondLength, 0, this.bondLength, 0, bondLength.length);
        origin = new Point2D.Double(0.0, 0.0);
        coordinates = createCoordinates();
    }

    public Structure2D(ArrayList<Point2D> coordinates) {
        this.coordinates = coordinates;
        coordinatesToAnglesAndBondLengths();
    }

    public void setOrigin(Point2D point) {
        origin = new Point2D.Double(point.getX(), point.getY());
        coordinates = createCoordinates();
    }

    double distance(double dx, double dy, double dtheta, Structure2D other) {

        List<Point2D> transformed = transformedCoordinates(dx, dy, dtheta);

        double ssd = 0.0;
        for (int i = 0; i < transformed.size(); i++) {
            Point2D p = transformed.get(i);
            Point2D q = other.coordinates.get(i);
            ssd += p.distanceSq(q);
        }
        return ssd;
    }

    List<Point2D> transformedCoordinates(double dx, double dy, double dtheta) {

       Structure2D copy = new Structure2D(angles, bondLength);
       copy.setOrigin(new Point2D.Double(origin.getX()+dx, origin.getY()+dy));
       copy.angles[0] += dtheta;
       return copy.createCoordinates();
    }

    double distance(Structure2D other) {
        return distance(0, 0, 0, other);
    }

    void minimizeDistance(Structure2D other) {

        System.out.println("Starting distance = " + distance(other));

        MultivariateFunction multivariateFunction = d -> distance(d[0], d[1], d[2], other);

        MultivariateOptimizer optimizer = new SimplexOptimizer(1e-6, 1e-8);
        final PointValuePair optimum =
                optimizer.optimize(
                        new MaxEval(10000),
                        new ObjectiveFunction(multivariateFunction),
                        GoalType.MINIMIZE,
                        new InitialGuess(new double[]{ 0, 0, 0}),
                        new NelderMeadSimplex(new double[]{ 0.1, 0.1, 0.2 }));

        double[] optimumPoint = optimum.getPoint();

        setTransform(optimumPoint[0], optimumPoint[1], optimumPoint[2]);

        System.out.println("Optimized dx = " + optimumPoint[0]);
        System.out.println("Optimized dy = " + optimumPoint[1]);
        System.out.println("Optimized dtheta = " + optimumPoint[2]);

        System.out.println("Optimized distance = " + distance(other));
    }

    private void setTransform(double dx, double dy, double dtheta) {
        angles[0] += dtheta;
        setOrigin(new Point2D.Double(origin.getX()+dx, origin.getY()+dy));
    }

    private List<Point2D> createCoordinates() {

        List<Point2D> newCoordinates = new ArrayList<>();
        newCoordinates.add(new Point2D.Double(origin.getX(), origin.getY()));
        double angle = 0.0;
        for (int i = 0; i < angles.length; i++) {
            angle += angles[i];
            Point2D point = nextPoint(newCoordinates.get(i), angle, bondLength[i]);
            newCoordinates.add(point);
        }
        return newCoordinates;
    }

    private void coordinatesToAnglesAndBondLengths() {

        origin = new Point2D.Double(coordinates.get(0).getX(), coordinates.get(0).getY());
        angles = new double[coordinates.size()-1];
        bondLength = new double[coordinates.size()-1];
        Point2D current = origin;
        double lastAngle = 0.0;
        for (int i = 0; i < angles.length; i++) {
            Point2D next = coordinates.get(i+1);
            double dx = next.getX() - current.getX();
            double dy = next.getY() - current.getY();

            double H = current.distance(next);

            //System.out.println("dx = " + dx);
            //System.out.println("dy = " + dy);

            double totalAngle = Math.asin(dy/H);

            if (dx < 0) totalAngle = Math.PI - totalAngle;

            //System.out.println("angle = " + totalAngle);

            angles[i] = totalAngle - lastAngle;

            //System.out.println("angles[" + i + "] = " + angles[i]);


            bondLength[i] = H;
            //System.out.println("bondLength[" + i + "] = " + bondLength[i]);
            current = next;
            lastAngle = totalAngle;
        }

        List<Point2D> newCoords = createCoordinates();

        for (int i = 0; i < newCoords.size(); i++) {
            if (newCoords.get(i).distance(coordinates.get(i)) > 1e-8) {
                System.err.println("Transformation failed at coord " + i + ": " + newCoords.get(i) + ", " + coordinates.get(i));
            }
        }
    }

    private Point2D nextPoint(Point2D current, double angle, double length) {

        double dy = Math.sin(angle) * length;
        double dx = Math.cos(angle) * length;
        return new Point2D.Double(current.getX() + dx, current.getY() + dy);
    }

    public static void main(String[] args) {

        double[] angles = new double[] {Math.PI/4.0, Math.PI/4.0, Math.PI/4.0, Math.PI/4.0, Math.PI/4.0, Math.PI/4.0, Math.PI/4.0, Math.PI/4.0};
        double[] bondLength = new double[] {100, 100, 100, 100, 100, 100, 100, 100};

        Structure2D structure = new Structure2D(angles, bondLength);
        structure.setOrigin(new Point2D.Double(0,0));

        for (int i = 0; i < structure.coordinates.size(); i++) {
            System.out.println(structure.coordinates.get(i));
        }

        structure.coordinatesToAnglesAndBondLengths();

        for (int i = 0; i < angles.length; i++) {
            System.out.println(structure.angles[i] + "\t" + structure.bondLength[i]);
        }
    }

    public void shiftX(double dx) {
        setOrigin(new Point2D.Double(origin.getX()+dx, origin.getY()));
    }
}
