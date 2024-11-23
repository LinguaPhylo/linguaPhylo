package lphy.base.evolution.coalescent.populationmodel;

import lphy.base.evolution.coalescent.PopulationFunction;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.integration.IterativeLegendreGaussIntegrator;
import org.apache.commons.math3.analysis.solvers.BrentSolver;
import org.apache.commons.math3.analysis.solvers.UnivariateSolver;
import org.apache.commons.math3.exception.NoBracketingException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;

/**
 * ExpansionPopulation models population size changes over time following a constant-exponential model.
 * It specifically handles scenarios with an ancestral population size (NA).
 */
public class ExpansionPopulation implements PopulationFunction {

    private final double NC;   // Initial population size at t=0
    private final double NA;   // Ancestral population size after decay
    private final double r;    // Exponential decay rate
    private final double x;    // Time at which exponential decay starts

    /**
     * Constructor for the ExpansionPopulation model with ancestral population size (NA).
     *
     * @param NC Initial population size at time t=0.
     * @param NA Ancestral population size after decay.
     * @param r  Exponential decay rate.
     * @param x  Time at which exponential decay starts.
     */
    public ExpansionPopulation(double NC, double NA, double r, double x) {
        if (NC <= 0) {
            throw new IllegalArgumentException("Initial population size NC must be positive.");
        }
        if (NA <= 0) {
            throw new IllegalArgumentException("Ancestral population size NA must be positive.");
        }
        if (NA >= NC) {
            throw new IllegalArgumentException("Ancestral population size NA must be less than initial population size NC.");
        }
        if (r <= 0) {
            throw new IllegalArgumentException("Decay rate r must be positive.");
        }
        if (x < 0) {
            throw new IllegalArgumentException("Time x must be non-negative.");
        }
        this.NC = NC;
        this.NA = NA;
        this.r = r;
        this.x = x;
    }

    /**
     * Calculate the population size N(t) at a given time t.
     *
     * @param t Time.
     * @return Population size at time t.
     */
    @Override
    public double getTheta(double t) {
        if (t < 0) {
            throw new IllegalArgumentException("Time t cannot be negative.");
        }

        if (t <= x) {
            return NC;
        } else {
            return (NC - NA) * Math.exp(-r * (t - x)) + NA;
        }
    }

    /**
     * Calculate the cumulative intensity over the time period from 0 to t.
     *
     * @param t Time.
     * @return Intensity value.
     */
    @Override
    public double getIntensity(double t) {
        if (t < 0) return 0.0;

        if (t <= x) {
            return t / NC;
        } else {
            // Integral from 0 to x: ∫(1 / NC) dt = t / NC
            double firstIntegral = x / NC;

            // Integral from x to t: ∫(1 / [(NC - NA)e^{-r(t' - x)} + NA] ) dt
            // No closed-form solution; use numerical integration

            UnivariateFunction integrand = timePoint -> 1.0 / ((NC - NA) * Math.exp(-r * (timePoint - x)) + NA);
            IterativeLegendreGaussIntegrator integrator = new IterativeLegendreGaussIntegrator(
                    5, 1.0e-12, 1.0e-8, 2, 10000);
            double secondIntegral = integrator.integrate(Integer.MAX_VALUE, integrand, x, t);

            return firstIntegral + secondIntegral;
        }
    }

    /**
     * Calculate the inverse intensity, i.e., find time t such that Intensity(t) = x.
     *
     * @param x Intensity value.
     * @return Time t corresponding to the given intensity.
     */
    @Override
    public double getInverseIntensity(double x) {
        if (x < 0) {
            throw new IllegalArgumentException("Intensity x must be non-negative.");
        }

        UnivariateFunction equation = tPoint -> getIntensity(tPoint) - x;
        UnivariateSolver solver = new BrentSolver(1e-9, 1e-9);
        double tMin = 0.0;
        double tMax = 1e6; // Arbitrary large number to ensure convergence

        try {
            return solver.solve(1000, equation, tMin, tMax);
        } catch (NoBracketingException | TooManyEvaluationsException e) {
            throw new IllegalArgumentException("Cannot find corresponding time t for the given intensity x.", e);
        }
    }

    @Override
    public boolean isAnalytical() {
        return false;
    }

    /**
     * Provides a string representation of the ExpansionPopulation model.
     *
     * @return String describing the model parameters.
     */
    @Override
    public String toString() {
        return "ExpansionPopulation [NC=" + NC + ", NA=" + NA +
                ", r=" + r + ", x=" + x + "]";
    }

    /**
     * Get the initial population size NC.
     *
     * @return Initial population size NC.
     */
    public double getNC() {
        return NC;
    }

    /**
     * Get the ancestral population size NA.
     *
     * @return Ancestral population size NA.
     */
    public double getNA() {
        return NA;
    }

    /**
     * Get the exponential decay rate r.
     *
     * @return Exponential decay rate r.
     */
    public double getR() {
        return r;
    }

    /**
     * Get the time x at which exponential decay starts.
     *
     * @return Time x.
     */
    public double getX() {
        return x;
    }
}
