package lphy.base.evolution.coalescent.populationmodel;

import lphy.base.evolution.coalescent.PopulationFunction;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.integration.IterativeLegendreGaussIntegrator;
import org.apache.commons.math3.analysis.solvers.BrentSolver;
import org.apache.commons.math3.analysis.solvers.UnivariateSolver;

import java.util.Locale;


public class LogisticPopulation implements PopulationFunction {
    private double t50;
    private double nCarryingCapacity;
    private double b;
    private boolean useAncestralPopulation;
    private double NA;
    private double resolution_magic_number = 1e3;

    /**
     * Constructs a LogisticPopulation model without ancestral population size (NA).
     *
     * @param t50               The midpoint of the logistic function, indicating the inflection point.
     * @param nCarryingCapacity The carrying capacity or the maximum population size.
     * @param b                 The growth rate parameter, determining the steepness of the curve.
     */
    public LogisticPopulation(double t50, double nCarryingCapacity, double b) {
        if (nCarryingCapacity <= 0) {
            throw new IllegalArgumentException("Carrying capacity nCarryingCapacity must be positive.");
        }
        this.t50 = t50;
        this.nCarryingCapacity = nCarryingCapacity;
        this.b = b;
        this.useAncestralPopulation = false;
        this.NA = 0.0; // Default value when not using NA.
    }

    /**
     * Constructs a LogisticPopulation model with an optional ancestral population size (NA).
     *
     * @param t50               The midpoint of the logistic function, indicating the inflection point.
     * @param nCarryingCapacity The carrying capacity or the maximum population size.
     * @param b                 The growth rate parameter, determining the steepness of the curve.
     * @param NA                The ancestral population size. If NA > 0, it modifies the logistic function to approach NA as time increases.
     */
    public LogisticPopulation(double t50, double nCarryingCapacity, double b, double NA) {
        if (nCarryingCapacity <= 0) {
            throw new IllegalArgumentException("Carrying capacity nCarryingCapacity must be positive.");
        }
        if (NA <= 0) {
            throw new IllegalArgumentException("Ancestral population size NA must be positive.");
        }
        if (NA > nCarryingCapacity) {
            throw new IllegalArgumentException("Ancestral population size NA cannot exceed carrying capacity nCarryingCapacity.");
        }
        this.t50 = t50;
        this.nCarryingCapacity = nCarryingCapacity;
        this.b = b;
        this.useAncestralPopulation = true;
        this.NA = NA;
    }


    @Override
    public double getTheta(double t) {
        if (useAncestralPopulation) {
            return NA + (nCarryingCapacity - NA) / (1 + Math.exp(b * (t - t50)));
        } else {
            return nCarryingCapacity / (1 + Math.exp(b * (t - t50)));
        }
    }

    @Override
    public double getIntensity(double t) {
        if (t == 0) return 0.0;

        if (getTheta(t) < nCarryingCapacity / resolution_magic_number) {
//            throw new RuntimeException("Theta too small to calculate intensity!");
        }
        UnivariateFunction function = time -> 1 / Math.max(getTheta(time), 1e-20);
        IterativeLegendreGaussIntegrator integrator = new IterativeLegendreGaussIntegrator(5, 1.0e-12, 1.0e-8, 2, 10000);
        return integrator.integrate(Integer.MAX_VALUE, function, 0, t);
    }



    @Override
    public double getInverseIntensity(double x) {
        UnivariateFunction function = time -> getIntensity(time) - x;
        UnivariateSolver solver = new BrentSolver();
        double tMin, tMax;

        double intensityAtT50 = getIntensity(t50);

        if (x <= intensityAtT50) {
            tMin = 0;
            tMax = t50;
        } else {
            // If the given x is greater than the cumulative intensity at t50, start the search interval from t50 and extend further
            tMin = t50;
            tMax = 2 * t50;

            while (getIntensity(tMax) < x) {
                if (tMax < Double.MAX_VALUE / 2) {
                    tMax *= 2;
                } else {
                    tMax = Double.MAX_VALUE;
                    System.out.println("Reached Double.MAX_VALUE when finding tMax");
                    break;
                }
            }
        }
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
        if (useAncestralPopulation) {
            return String.format(Locale.US, "Logistic Model with NA: t50=%.4f, nCarryingCapacity=%.4f, b=%.4f, NA=%.4f",
                    t50, nCarryingCapacity, b, NA);
        } else {
            return String.format(Locale.US, "Logistic Model: t50=%.4f, nCarryingCapacity=%.4f, b=%.4f",
                    t50, nCarryingCapacity, b);
        }
    }
}
