package lphy.base.evolution.coalescent.populationmodel;

import lphy.base.evolution.coalescent.PopulationFunction;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.BrentSolver;
import org.apache.commons.math3.analysis.solvers.UnivariateSolver;
import org.apache.commons.math3.exception.NoBracketingException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;

public class ExpansionPopulation implements PopulationFunction {

    private double N0;  // Initial population size before tau
    private double tau; // Time before which population size is constant at N0
    private double r;   // Exponential growth rate
    private double NC;  // Current population size after time x
    private double x;   // Time at which the population reaches NC

    /**
     * Constructor for PiecewiseExponentialPopulation
     *
     * @param N0  Initial population size before tau
     * @param tau Time before which population size is constant at N0
     * @param r   Exponential growth rate
     * @param NC  Current population size after time x
     */
    public ExpansionPopulation(double N0, double tau, double r, double NC) {
        this.N0 = N0;
        this.tau = tau;
        this.r = r;
        this.NC = NC;

        // Validate parameters
//        if (N0 <= 0) {
//            throw new IllegalArgumentException("Initial population size N0 must be greater than 0.");
//        }
//        if (NC <= N0) {
//            throw new IllegalArgumentException("Current population size NC must be greater than N0.");
//        }
//        if (r <= 0) {
//            throw new IllegalArgumentException("Growth rate r must be greater than 0.");
//        }
//        if (tau < 0) {
//            throw new IllegalArgumentException("Time tau must be non-negative.");
//        }
        this.x = tau + (1.0 / r) * Math.log(N0 / NC);
        this.N0 = NC * Math.exp(-r * (tau - x));
    }

    /**
     * Calculate the population size N(t) at a given time t.
     *
     * @param t Time
     * @return Population size at time t
     */
    @Override
    public double getTheta(double t) {
        if (t <= x) {
            // Before tau, population size is constant N0
            return NC;
        } else if (t < tau) {
            // Between tau and x, population grows exponentially
            return NC * Math.exp(-r * (t - x));
        } else {
            // After x, population size is constant NC
            return N0;
        }
    }

    /**
     * Calculate the cumulative intensity over time period from 0 to t.
     *
     * @param t Time
     * @return Intensity value
     */
    @Override
    public double getIntensity(double t) {

        if (t <= x) {
            // Case 1: t <= x
            return t / NC;
        } else if (t <= tau) {
            // Case 2: x < t <= tau
            double firstIntegral = x / NC;
            double secondIntegral = (Math.exp(r * (t - x)) - 1) / (r * NC);
            return firstIntegral + secondIntegral;
        } else {
            // Case 3: t > tau
            double firstIntegral = x / NC;
            double secondIntegral = (Math.exp(r * (tau - x)) - 1) / (r * NC);
            double thirdIntegral = (t - tau) / N0;
            return firstIntegral + secondIntegral + thirdIntegral;
        }
    }

    @Override
    public double getInverseIntensity(double intensity) {
        double I_x = x / NC;
        double I_tau = I_x + (Math.exp(r * (tau - x)) - 1) / (r * NC);

        if (intensity <= I_x) {
            // Case 1: t <= x
            return intensity * NC;
        } else if (intensity <= I_tau) {
            // Case 2: x < t <= tau
            // Use numerical solver to find t such that I(t) = intensity
            UnivariateFunction equation = new UnivariateFunction() {
                @Override
                public double value(double t) {
                    return getIntensity(t) - intensity;
                }
            };

            // Define Brent solver with specified tolerances
            UnivariateSolver solver = new BrentSolver(1e-8, 1e-6);

            double tMin = x;
            double tMax = tau;

            try {
                // Solve for t within the interval (x, tau)
                double t = solver.solve(1000, equation, tMin, tMax);
                return t;
            } catch (TooManyEvaluationsException | NoBracketingException e) {
                // If numerical solving fails, throw an exception
                throw new IllegalArgumentException("Cannot find corresponding time t in the interval (x, tau).", e);
            }
        } else {
            // Case 3: t > tau
            // Analytical inverse function
            return tau + N0 * (intensity - I_tau);
        }
    }

    @Override
    public boolean isAnalytical() {
        return false; // Indicates that analytical methods are not fully used
    }

    @Override
    public String toString() {
        return "Expansion Population with N0=" + N0 + ", tau=" + tau + ", r=" + r + ", NC=" + NC;
    }

    /**
     * Main method to test getIntensity and getInverseIntensity methods.
     *
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        // Test parameters
        double N0 = 0.386;
        double tau = 2.72;
        double r = 0.4;
        double NC = 398.0;

        // Create an instance of ExpansionPopulation
        ExpansionPopulation popModel = new ExpansionPopulation(N0, tau, r, NC);

        // Calculate x = tau + (1 / r) * ln(N0 / NC)
        double x = tau + (1.0 / r) * Math.log(N0 / NC);
        System.out.printf("Calculated x: %.8f%n%n", x);

        // Define comprehensive test time points
        double[] testTimes = {
                0.0,            // t = 0
                0.1,            // t < x
                1.0,            // t < x
                x - 0.01,       // Slightly less than x, t < x
                x,              // t = x
                x + 0.01,       // Slightly more than x, t > x
                2.0,            // t < x
                2.13793,        // t = x (exact x)
                2.138,          // t slightly > x
                2.2,            // x < t < tau
                2.5,            // x < t < tau
                2.51,           // Slightly less than tau, t < tau
                tau,            // t = tau
                tau + 0.01,     // Slightly more than tau, t > tau
                3.0,            // t > tau
                5.0,            // t > tau
                10.0,           // t > tau
                15.0,           // t > tau
                20.0,           // t > tau
                25.0            // t > tau
        };

        // Print header for results
        System.out.println("Time\tTheta(t)\t\tExpected Theta(t)\tPassed");

        for (double t : testTimes) {
            double theta = popModel.getTheta(t);
            double expectedTheta;

            // Calculate expected Theta(t) based on the segment
            if (t <= x) {
                // Segment 1: t <= x, population size is constant NC
                expectedTheta = NC;
            } else if (t < tau) {
                // Segment 2: x < t < tau, population size decays exponentially
                expectedTheta = NC * Math.exp(-r * (t - x));
            } else {
                // Segment 3: t >= tau, population size is constant N0
                expectedTheta = N0;
            }

            // Calculate the difference between actual and expected Theta(t)
            double difference = Math.abs(theta - expectedTheta);

            // Determine if the test passed based on a small tolerance
            boolean passed = difference < 1e-6;

            // Print the results
            System.out.printf("%.2f\t%.8f\t%.8f\t\t%s%n", t, theta, expectedTheta, passed ? "Passed" : "Failed");
        }
    }
}