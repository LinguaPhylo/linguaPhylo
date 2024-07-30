package lphy.base.evolution.coalescent.populationmodel;

import lphy.base.evolution.coalescent.PopulationFunction;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.integration.IterativeLegendreGaussIntegrator;
import org.apache.commons.math3.analysis.solvers.BrentSolver;
import org.apache.commons.math3.analysis.solvers.UnivariateSolver;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

/**
 * Represents a logistic growth model.
 * This model is defined by the logistic function, which is characterized by an S-shaped growth curve.
 * It is similar to the Gompertz growth model but uses the logistic function parameters:
 * - t50 corresponds to the inflection point of the curve, similar to t50 in the Gompertz model, indicating the time at which the population reaches half of its carrying capacity.
 * - L is analogous to NInfinity in the Gompertz model, representing the carrying capacity or the maximum achievable population size.
 * - b is related to the growth rate parameter b in the Gompertz model, determining the steepness of the curve.
 */
public class LogisticPopulation implements PopulationFunction {
    private double t50;
    private double nCarryingCapacity;
    private double b;
    private double resolution_magic_number = 1e3;

    /**
     * Constructs a LogisticPopulation model with specified parameters.
     * @param t50 The midpoint of the logistic function, similar to t50 in the Gompertz model.
     * @param nCarryingCapacity The carrying capacity or the maximum population size, analogous to NInfinity in the Gompertz model.
     * @param b The growth rate, related to the growth rate b in the Gompertz model.
     */

    public LogisticPopulation(double t50, double nCarryingCapacity, double b) {
        this.t50 = t50;
        this.nCarryingCapacity = nCarryingCapacity;
        this.b = b;
    }

    /**
     * Initializes an IterativeLegendreGaussIntegrator with predefined settings for numerical integration.
     * This setup is optimized for accuracy and efficiency in logistic population model computations.
     * Parameter values are from chatGPT
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


    //
    @Override
    public double getTheta(double t) {
        return nCarryingCapacity / (1 + Math.exp(b * (t - t50)));
    }


    /**
     * Updates the IterativeLegendreGaussIntegrator setup to address the issue of exceeding the maximum number of evaluations.
     * Previously, the method encountered TooManyEvaluationsException when the specified maximum iteration count (10,000) was surpassed,
     * indicating the numerical integration task required more evaluations to achieve the desired accuracy. This update aims to mitigate
     * the exception by either adjusting the maximum iteration count or enhancing the integrator's accuracy parameters, ensuring the integration
     * process can complete successfully without compromising computational efficiency or precision.
     *
     * Adjustments include:
     * - Increasing the maximum iteration count, if the computation demands more iterations for convergence.
     * - Tweaking accuracy parameters (relative and absolute accuracy) for a more robust integration evaluation.
     * The exact strategy should be chosen based on the specific integration challenges and performance considerations of the task at hand.
     */
    @Override
    public double getIntensity(double t) {
        if (t == 0) return 0;

        if (getTheta(t) < nCarryingCapacity / resolution_magic_number) {
//            throw new RuntimeException("Theta too small to calculate intensity!");
        }
        UnivariateFunction function = time -> 1 / getTheta(time);

        // Use the separate method to create the integrator
        IterativeLegendreGaussIntegrator integrator = createIntegrator();
        return integrator.integrate(Integer.MAX_VALUE, function, 0, t);
    }



    @Override
    public double getInverseIntensity(double x) {
        UnivariateFunction function = time -> getIntensity(time) - x;
        UnivariateSolver solver = new BrentSolver();
        double tMin, tMax;

        // Calculate the cumulative intensity at t50 to determine which part of the curve x belongs to
        double intensityAtT50 = getIntensity(t50);

        if (x <= intensityAtT50) {
            // If the given x is less than or equal to the cumulative intensity at t50, the search interval is from 0 to t50
            tMin = 0;
            tMax = t50;
        } else {
            // If the given x is greater than the cumulative intensity at t50, start the search interval from t50 and extend further
            tMin = t50;
            tMax = 2 * t50; // Initially assume the maximum time as 2*t50


            // Dynamically adjust tMax until finding a sufficiently large value such that getIntensity(tMax) >= x
            while (getIntensity(tMax) < x) {
                if (tMax < Double.MAX_VALUE / 2) {
                    tMax *= 2; // Gradually increase the upper limit of the interval
                } else {
                    tMax = Double.MAX_VALUE;
                    System.out.println("Reached Double.MAX_VALUE when finding tMax"); // This print statement is just for testing purposes
                    break; // Prevent infinite loop
                }
            }
        }

        // Attempt to solve using the adjusted interval
        try {
            return solver.solve(100, function, tMin, tMax);
        } catch (Exception e) {
            throw new RuntimeException("Failed to find a valid time for given intensity: " + x, e);
        }
    }



    @Override
    public boolean isAnalytical() {
        return false; //use numerical method here
    }

    @Override
    public String toString() {
        return "Logistic Model: t50=" + t50 + ", nCarryingCapacity=" + nCarryingCapacity + ", b=" + b;
    }




    public static void main(String[] args) {
        double nCarryingCapacity = 1;
        double b = 1;
        double t50 = 0;
        double tStart = -10;
        double tEnd = 10;
        int nPoints = 100;

        // Logistic constructor order is (t50, L, b)
        LogisticPopulation logisticPopulation = new LogisticPopulation(t50, nCarryingCapacity, b);

        try (PrintWriter writer = new PrintWriter(new FileWriter("logistic_data.csv"))) {
            writer.println("time,theta");
            for (int i = 0; i < nPoints; i++) {
                double t = tStart + (i / (double) (nPoints - 1)) * (tEnd - tStart);
                double theta = logisticPopulation.getTheta(t);

                writer.printf(Locale.US, "%.4f,%.4f%n", t, theta);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}
