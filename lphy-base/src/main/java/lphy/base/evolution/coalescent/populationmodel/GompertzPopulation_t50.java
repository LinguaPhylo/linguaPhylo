package lphy.base.evolution.coalescent.populationmodel;

import lphy.base.evolution.coalescent.PopulationFunction;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.integration.IterativeLegendreGaussIntegrator;
import org.apache.commons.math3.analysis.integration.RombergIntegrator;
import org.apache.commons.math3.analysis.solvers.BrentSolver;
import org.apache.commons.math3.analysis.solvers.UnivariateSolver;
import org.apache.commons.math3.exception.NoBracketingException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

public class GompertzPopulation_t50 implements PopulationFunction {

    public static final String BParamName = "b";
    public static final String NINFINITYParamName = "NInfinity";
    public static final String T50ParamName = "t50";
    public static final String F0ParamName = "f0";

    private double N0;  // Initial population size
    private double b;   // Initial growth rate of tumor growth
    private double NInfinity; // Carrying capacity
    private double t50; // Time when population is half of carrying capacity
    private double f0;  // Initial proportion of the carrying capacity

    private double resolution_magic_number = 1e4;

    public static double computeT50(double NInfinity, double N0, double b) {
        if (N0 >= NInfinity || b <= 0) {
            throw new IllegalArgumentException("N0 must be less than NInfinity and b must be greater than 0.");
        }
        double ratio = NInfinity / N0;
        double proportion = 0.5;
        double t50 = Math.log(1 - Math.log(proportion) / Math.log(ratio)) / b;
        return t50;
    }

    public double getTimeForGivenProportion(double k) {
        // Ensure b is not 0 to avoid division by zero
        if (b == 0) {
            throw new IllegalArgumentException("Growth rate b cannot be zero.");
        }

        double ratio = NInfinity / N0;
        double proportion = k * ratio;
        // Ensure proportion is within valid range to avoid taking log of non-positive number
        if (proportion <= 0 || proportion >= ratio) {
            throw new IllegalArgumentException("Proportion must be between 0 and " + ratio);
        }

        // Apply the formula to calculate t*
        double tStar = Math.log(1 - Math.log(proportion) / Math.log(ratio)) / b;
        return tStar;
    }

    public double getN0() {
        return this.N0;
    }

    private IterativeLegendreGaussIntegrator createIntegrator() {
        int numberOfPoints = 5; // Legendre-Gauss points
        double relativeAccuracy = 1.0e-10; // Relative precision
        double absoluteAccuracy = 1.0e-9; // Absolute accuracy
        int minimalIterationCount = 2; // Minimum number of iterations
        int maximalIterationCount = 100000; // Maximum number of iterations, adjust as needed
        return new IterativeLegendreGaussIntegrator(numberOfPoints, relativeAccuracy, absoluteAccuracy, minimalIterationCount, maximalIterationCount);
    }

    public GompertzPopulation_t50(double t50, double b, double NInfinity) {   // For t50
        this.b = b;
        this.t50 = t50;
        this.NInfinity = NInfinity;
        // Calculate N0 based on t50, b, and NInfinity
        // N(t50) = NInfinity / 2
        // t50 is a time location given by the user, t50 < 0 means it is in the early exponential phase
        this.N0 = NInfinity * Math.pow(2, -Math.exp(-b * this.t50));
    }

    @Override  // For t50
    public double getTheta(double t) {
        // The sign of b * t is such that t = 0 is present time and t > 0 is time in the past
        return N0 * Math.exp(Math.log(NInfinity / N0) * (1 - Math.exp(b * t)));
    }

    @Override
    public double getIntensity(double t) {
        if (t == 0) return 0;

        UnivariateFunction function = time -> 1 / getTheta(time);

        IterativeLegendreGaussIntegrator integrator = createIntegrator();
        return integrator.integrate(Integer.MAX_VALUE, function, 0, t);
    }

    private double legrandeIntegrator(UnivariateFunction function, double t) {
        IterativeLegendreGaussIntegrator integrator = createIntegrator();
        return integrator.integrate(Integer.MAX_VALUE, function, 0, t);
    }

    private double rombergIntegrator(UnivariateFunction function, double t) {
        int maxBound = Integer.MAX_VALUE;

        RombergIntegrator integrator = new RombergIntegrator();
        double absoluteAccuracy = 1e-6;
        integrator = new RombergIntegrator(RombergIntegrator.DEFAULT_RELATIVE_ACCURACY,
                absoluteAccuracy, RombergIntegrator.DEFAULT_MIN_ITERATIONS_COUNT,
                RombergIntegrator.ROMBERG_MAX_ITERATIONS_COUNT);

        return integrator.integrate(maxBound, function, 0, t);
    }

    @Override
    public double getInverseIntensity(double x) {
        double targetIntensity = x;

        double proportionForT1 = 0.01;
        double proportionForT50 = 0.5;
        double time = 0;

        double t1 = getTimeForGivenProportion(proportionForT1);
        double t50 = getTimeForGivenProportion(proportionForT50);

        double growthPhaseTime = t1 - t50;
        double deltaTime = growthPhaseTime / 100;

        double intensity = getIntensity(time);

        while (intensity < targetIntensity) {
            time += deltaTime;
            intensity = getIntensity(time);
        }

        double lowerBound = Math.max(0, time - deltaTime);
        double upperBound = time;
        UnivariateFunction function = t -> getIntensity(t) - x;
        UnivariateSolver solver = new BrentSolver(1e-9, 1e-9);
        try {
            return solver.solve(100, function, lowerBound, upperBound);
        } catch (NoBracketingException | TooManyEvaluationsException e) {
            System.err.println("Solver failed: " + e.getMessage());
            return Double.NaN;
        }
    }

    @Override
    public boolean isAnalytical() {
        return false; // Use numerical method here
    }

    public static void main(String[] args) {
        double f0 = 0.5;
        double b = 0.1;
        double NInfinity = 1000;
        double tStart = 0;
        double tEnd = 50;
        int nPoints = 100;
        double t50 = 10.0;

        GompertzPopulation_t50 gompertzPopulation = new GompertzPopulation_t50(t50, b, NInfinity);

        try (PrintWriter writer = new PrintWriter(new FileWriter("gompertzpopt50_data.csv"))) {
            writer.println("time,theta");
            for (int i = 0; i < nPoints; i++) {
                double t = tStart + (i / (double) (nPoints - 1)) * (tEnd - tStart);
                double theta = gompertzPopulation.getTheta(t);

                writer.printf(Locale.US, "%.4f,%.4f%n", t, theta);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
