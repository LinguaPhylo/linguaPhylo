package lphy.toroidalDiffusion;

import org.apache.commons.math3.optim.*;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.MultivariateOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizer;
import org.apache.commons.math3.optimization.BaseMultivariateSimpleBoundsOptimizer;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;

public class ToroidalPlot extends JComponent {

    double torusSize = 2.0 * Math.PI;

    Point2D mu;
    Point2D[] x;

    int cellWidth = 768;
    double ellipseWidth = 8;

    WrappedBivariateDiffusion diffusion;

    boolean plotStationaryDistribution = true;
    boolean plotTPDistribution = true;

    public ToroidalPlot(WrappedBivariateDiffusion diffusion) {

        this.diffusion = diffusion;

        mu = new Point2D.Double(diffusion.mu.get(0), diffusion.mu.get(1));

        x = new Point2D[1];
        for (int i = 0; i < x.length; i++) {
            x[i] = new Point2D.Double(Math.PI*2*Math.random(), Math.PI*2*Math.random());
        }

        setPreferredSize(new Dimension(cellWidth, cellWidth));
    }

    double toroidalDistance (double x1, double y1, double x2, double y2) {
        double dx = Math.abs(x2 - x1);
        double dy = Math.abs(y2 - y1);

        if (dx > 0.5) {
            dx = 1.0 - dx;
        }

        if (dy > 0.5) {
            dy = 1.0 - dy;
        }

        return Math.sqrt(dx*dx + dy*dy);
    }

    /**
     * Pick the shortest line in toroidal space between these two points, keep first point unvaried and trying all wrap-arounds of second point
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return
     */
    public Line2D shortestLine(double x1, double y1, double x2, double y2) {

        double shortestDistance = Double.MAX_VALUE;
        Point2D best = null;

        Point2D p1 = new Point2D.Double(x1,y1);

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {

                Point2D n2 = new Point2D.Double(x2+i*torusSize, y2+j*torusSize);

                Line2D line = new Line2D.Double(x1, y1, x2+i*torusSize, y2+j*torusSize);

                double dist = p1.distance(n2);

                if (dist < shortestDistance) {
                    shortestDistance = dist;
                    best = n2;
                }
            }
        }
        return new Line2D.Double(p1, best);
    }


    public void paintComponent(Graphics g) {

        Graphics2D g2d = (Graphics2D)g;
        if (plotStationaryDistribution) {
            paintStationaryDistribution(g2d);
        }

        if (plotTPDistribution) {
            paintTPDistribution(g2d);
        }

        double mx1 = getPixelX(mu.getX());
        double my1 = getPixelY(mu.getY());

        Rectangle2D rectangle2D = new Rectangle2D.Double(getPixelX(0.0), getPixelY(torusSize), cellWidth, cellWidth);
        g2d.setColor(Color.black);
        g2d.draw(rectangle2D);
//
//        for (int i = -1; i <= 1; i++) {
//            int offSetX = cellWidth * i;
//            for (int j = -1; j <= 1; j++) {
//                int offSetY = cellWidth * j;
//
//                Point2D tmu = new Point2D.Double(mu.getX()+i*torusSize, mu.getY()+j*torusSize);
//
//                double sx2 = getPixelX(tmu.getX());
//                double sy2 = getPixelY(tmu.getY());
//
//                Line2D line = new Line2D.Double(sx1, sy1, sx2, sy2);
//
//                g2d.draw(line);
//            }
//        }

        for (int i = 0; i < x.length; i++) {
            g2d.setColor(Color.yellow);
            g2d.draw(transform(shortestLine(x[i].getX(), x[i].getY(), mu.getX(), mu.getY())));
            g2d.draw(transform(shortestLine(mu.getX(), mu.getY(), x[i].getX(), x[i].getY())));

        }

        for (int i = 0; i < x.length; i++) {
            double sx1 = getPixelX(x[i].getX());
            double sy1 = getPixelY(x[i].getY());
            Ellipse2D xEllipse = new Ellipse2D.Double(sx1 - ellipseWidth / 2.0, sy1 - ellipseWidth / 2.0, ellipseWidth, ellipseWidth);
            g2d.setColor(Color.green);
            g2d.fill(xEllipse);
        }

        Ellipse2D muEllipse = new Ellipse2D.Double(mx1 - ellipseWidth / 2.0, my1 - ellipseWidth / 2.0, ellipseWidth, ellipseWidth);
        g2d.setColor(Color.red);
        g2d.fill(muEllipse);

    }

    private void paintStationaryDistribution(Graphics2D g2d) {

        int resolution = 360;
        double increment = 1.0/(double)resolution* torusSize;

        double[][] density = new double[resolution][resolution];
        double maxDensity = 0.0;


        for (int i = 0; i < resolution; i++) {
            double worldX = (double)i/(double)resolution * torusSize;
            for (int j = 0; j < resolution; j++) {
                double worldY = (double)j/(double)resolution * torusSize;
                density[i][j] = Math.exp(diffusion.loglikwndstat(worldX, worldY));
                if (density[i][j] > maxDensity) {
                    maxDensity = density[i][j];
                }
            }
        }

        for (int i = 0; i < resolution; i++) {
            double worldX = (double)i/(double)resolution * torusSize;
            for (int j = 0; j < resolution; j++) {
                double worldY = (double)j/(double)resolution * torusSize;
                Rectangle2D rectangle2D = new Rectangle2D.Double(getPixelX(worldX), getPixelY(worldY+increment), increment*cellWidth/torusSize, increment*cellWidth/torusSize);
                g2d.setColor(getColor(density[i][j], maxDensity));
                g2d.fill(rectangle2D);
            }
        }
    }

    private void paintTPDistribution(Graphics2D g2d) {

        int resolution = 360;
        double increment = (torusSize/(double)resolution);

        double[][] density = new double[resolution][resolution];
        double maxDensity = 0.0;
        double totalProb = 0.0;

        for (int i = 0; i < resolution; i++) {
            double worldX = (double)i/(double)resolution * torusSize;
            for (int j = 0; j < resolution; j++) {
                double worldY = (double)j/(double)resolution * torusSize;
                density[i][j] = Math.exp(diffusion.loglikwndtpd(x[0].getX(), x[0].getY(), worldX, worldY));
                if (density[i][j] > maxDensity) {
                    maxDensity = density[i][j];
                }
                totalProb += density[i][j];
            }
        }

        for (int i = 0; i < resolution; i++) {
            double worldX = (double)i/(double)resolution * torusSize;
            for (int j = 0; j < resolution; j++) {
                double worldY = (double)j/(double)resolution * torusSize;
                Rectangle2D rectangle2D = new Rectangle2D.Double(getPixelX(worldX), getPixelY(worldY+increment), increment*cellWidth/torusSize, increment*cellWidth/torusSize);
                g2d.setColor(getColor(density[i][j], maxDensity));
                g2d.fill(rectangle2D);
            }
        }
        g2d.setColor(Color.blue);
        //g2d.fill(getEllipse(p[0], p[1], ellipseWidth));

        System.out.println("Total prob = " + totalProb);

    }

    private Ellipse2D getEllipse(double worldX, double worldY, double ellipseWidth) {
        return new Ellipse2D.Double(getPixelX(worldX) - ellipseWidth / 2.0, getPixelY(worldY) - ellipseWidth / 2.0, ellipseWidth, ellipseWidth);
    }

    private Color getColor(double density, double maxDensity) {

        float intensity = (float)(density/maxDensity);

        if (intensity < 0.0f || intensity>1.0f) {
            System.out.println(density + " / " + maxDensity + " = " + intensity);
        }

        return new Color(intensity, intensity, intensity);
    }

    private Line2D transform(Line2D worldLine) {
        return new Line2D.Double(transform(worldLine.getP1()), transform(worldLine.getP2()));
    }

    private Point2D transform(Point2D worldPoint) {
        return new Point2D.Double(getPixelX(worldPoint.getX()), getPixelY(worldPoint.getY()));
    }


    double getPixelX(double worldX) {
        return worldX*(double)cellWidth/torusSize;
    }

    double getPixelY(double worldY) {
        return cellWidth - (worldY*(double)cellWidth/torusSize);
    }

    public static void main(String[] args) {

        JFrame frame = new JFrame("Torus world");

        WrappedBivariateDiffusion diff = new WrappedBivariateDiffusion();
        double[] muarr = {Math.PI * 0.65, Math.PI * 0.8}; // mean of the diffusion
        double[] sigmaarr = {0.5, 0.75}; // variance term
        double[] alphaarr = {1.0, 1.0, 0.5}; // drift term
        diff.setParameters(muarr, alphaarr, sigmaarr); // set the diffusion parameters


        frame.getContentPane().add(new ToroidalPlot(diff));
        frame.pack();

        frame.setVisible(true);

    }
}
