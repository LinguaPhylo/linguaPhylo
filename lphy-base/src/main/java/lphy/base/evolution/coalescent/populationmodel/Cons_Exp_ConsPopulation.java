package lphy.base.evolution.coalescent.populationmodel;

import lphy.base.evolution.coalescent.PopulationFunction;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.BrentSolver;
import org.apache.commons.math3.analysis.solvers.UnivariateSolver;
import org.apache.commons.math3.exception.NoBracketingException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;

public class Cons_Exp_ConsPopulation implements PopulationFunction {

    private double tau; // Time before which population size is constant at N0
    private double r;   // Exponential growth rate
    private double NC;  // Current population size after time x
    private double x;   // Independent parameter for time at which the population reaches NC

    /**
     * Constructor for Cons_Exp_ConsPopulation model
     *
     * @param tau Time before which population size is constant at N0
     * @param r   Exponential growth rate
     * @param NC  Current population size after time x
     * @param x   Independent parameter indicating time at which population size equals NC
     */
    public Cons_Exp_ConsPopulation(double tau, double r, double NC, double x) {
        this.tau = tau;
        this.r = r;
        this.NC = NC;
        this.x = x;
    }

    /**
     * Calculate the initial population size N0 based on tau, r, NC, and x.
     */
    private double calculateN0() {
        return NC * Math.exp(-r * (tau - x));
    }

    /**
     * Calculate the population size N(t) at a given time t.
     *
     * @param t Time
     * @return Population size at time t
     */
    @Override
    public double getTheta(double t) {
        double N0 = calculateN0();

        if (t <= x) {
            return NC;
        } else if (t < tau) {
            return NC * Math.exp(-r * (t - x));
        } else {
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
        double N0 = calculateN0();

        if (t <= x) {
            return t / NC;
        } else if (t <= tau) {
            double firstIntegral = x / NC;
            double secondIntegral = (Math.exp(r * (t - x)) - 1) / (r * NC);
            return firstIntegral + secondIntegral;
        } else {
            double firstIntegral = x / NC;
            double secondIntegral = (Math.exp(r * (tau - x)) - 1) / (r * NC);
            double thirdIntegral = (t - tau) / N0;
            return firstIntegral + secondIntegral + thirdIntegral;
        }
    }

    @Override
    public double getInverseIntensity(double intensity) {
        double N0 = calculateN0();
        double I_x = x / NC;
        double I_tau = I_x + (Math.exp(r * (tau - x)) - 1) / (r * NC);

        if (intensity <= I_x) {
            return intensity * NC;
        } else if (intensity <= I_tau) {
            UnivariateFunction equation = new UnivariateFunction() {
                @Override
                public double value(double t) {
                    return getIntensity(t) - intensity;
                }
            };

            UnivariateSolver solver = new BrentSolver(1e-8, 1e-6);
            double tMin = x;
            double tMax = tau;

            try {
                return solver.solve(1000, equation, tMin, tMax);
            } catch (TooManyEvaluationsException | NoBracketingException e) {
                throw new IllegalArgumentException("Cannot find corresponding time t in the interval (x, tau).", e);
            }
        } else {
            return tau + N0 * (intensity - I_tau);
        }
    }

    @Override
    public boolean isAnalytical() {
        return false;
    }

    @Override
    public String toString() {
        return "Cons_Exp_Cons Population with tau=" + tau + ", r=" + r + ", NC=" + NC + ", x=" + x;
    }


}