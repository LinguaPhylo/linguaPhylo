package lphy.base.evolution.coalescent.populationmodel;

import lphy.base.evolution.coalescent.PopulationFunction;
import org.apache.commons.math3.analysis.integration.IterativeLegendreGaussIntegrator;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

public class ExponentialPopulation implements PopulationFunction {

    private double GrowthRate;
    private double N0;

    /**
     * Initializes an IterativeLegendreGaussIntegrator with predefined settings for numerical integration.
     * This setup is optimized for accuracy and efficiency in logistic population model computations.
     *
     * @return Configured IterativeLegendreGaussIntegrator with:
     * - 5 Legendre-Gauss points for quadrature precision.
     * - Relative accuracy of 1.0e-12 and absolute accuracy of 1.0e-8.
     * - A minimum of 2 iterations and a maximum of 10,000 iterations.
     */
    private IterativeLegendreGaussIntegrator createIntegrator() {
        int numberOfPoints = 5; // Legendre-Gauss points
        double relativeAccuracy = 1.0e-12; // relative precision
        double absoluteAccuracy = 1.0e-8; // absolute accuracy
        int minimalIterationCount = 2; // Minimum number of iterations
        int maximalIterationCount = 10000; //Maximum number of iterations, adjust as needed
        return new IterativeLegendreGaussIntegrator(numberOfPoints, relativeAccuracy, absoluteAccuracy, minimalIterationCount, maximalIterationCount);
    }


    public ExponentialPopulation(double GrowthRate, double N0) {
        this.GrowthRate = GrowthRate;
        this.N0 = N0;
    }

    /**
     * Calculate the growth value at a given time t.。
     */
    @Override
    public double getTheta(double t) {

        double r = GrowthRate;
        if (r == 0) {
            return N0;
        } else {
            return N0 * Math.exp(-t * r);
        }
    }

    /**
     * Calculate the cumulative intensity over time period from 0 to t.
     */

    @Override
    public double getIntensity(double t) {

        double r = GrowthRate;
        if (r == 0.0) {
            return t / N0;
        } else {
            return (Math.exp(t * r) - 1.0) / N0 / r;
        }
    }
//
//        if (t == 0) return 0;
//
//
//        UnivariateFunction function = time -> 1 / getTheta(time);
//        IterativeLegendreGaussIntegrator integrator = createIntegrator();
//        return integrator.integrate(Integer.MAX_VALUE, function, 0, t);
//    }


//    @Override
//    public double getInverseIntensity(double x) {
////        If min is set to 0 in BrentSolver, an error will be reported:
////        org.apache.commons.math3.exception.NumberIsTooLargeException: endpoints do not specify an interval: [0, 0]
////        so I change min equals to 0.1
//        UnivariateFunction function = time -> getIntensity(time) - x;
//        UnivariateSolver solver = new BrentSolver();
//        // The range [0, 100] might need to be adjusted depending on the growth model and expected time range.
////        return solver.solve(100, function, 0, 100);
//        return solver.solve(100, function, 0.1, 50);
//    }

    @Override
    public double getInverseIntensity(double x) {
        double r = GrowthRate;
        if (r == 0.0) {
            return N0 * x;
        } else {
            return Math.log(1.0 + N0 * x * r) / r;
        }
    }
//        UnivariateFunction function = time -> getIntensity(time) - x;
//        UnivariateSolver solver = new BrentSolver();
//        double tMin = 0;
//        double tMax = 10;
//
//        double lastIntensity = getIntensity(tMin);
//        System.out.println("Starting search: tMin=" + tMin + ", Intensity=" + lastIntensity);
//
//
//
//        while (lastIntensity < x) {
//            tMax *= 2;
//            lastIntensity = getIntensity(tMax);
//            System.out.println("Expanding search: tMax=" + tMax + ", Intensity=" + lastIntensity);
//
//            if (tMax >= Double.MAX_VALUE / 2) {
//                System.out.println("Reached maximum double value for tMax.");
//                tMax = Double.MAX_VALUE / 2;
//                break;
//            }
//        }
//
//        try {
//            double result = solver.solve(100, function, tMin, tMax);
//            System.out.println("Found time for given intensity: " + result);
//            System.out.println("Found time for given intensity: " + result + ", x=" + x);
//            return result;
//        } catch (Exception e) {
//            System.out.println("Failed at tMax=" + tMax + " with lastIntensity=" + lastIntensity);
//            throw new RuntimeException("Failed to find a valid time for given intensity: " + x, e);
//        }
//    }


    @Override
    public boolean isAnalytical() {
        return false; //use numerical method here
    }


    public static void main(String[] args) {
        double GrowthRate = 0.1;
        double N0 = 3;
        double tStart = 0;
        double tEnd = 100;
        int nPoints = 100;

        ExponentialPopulation exponentialPopulation = new ExponentialPopulation(GrowthRate, N0);

        try (PrintWriter writer = new PrintWriter(new FileWriter("exponential_data.csv"))) {
            writer.println("time,theta");
            for (int i = 0; i < nPoints; i++) {
                double t = tStart + (i / (double) (nPoints - 1)) * (tEnd - tStart);
                double theta = exponentialPopulation.getTheta(t);

                writer.printf(Locale.US, "%.4f,%.4f%n", t, theta);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}



