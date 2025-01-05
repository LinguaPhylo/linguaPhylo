package lphy.base.evolution.coalescent.populationmodel;

import lphy.base.evolution.coalescent.PopulationFunction;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.integration.IterativeLegendreGaussIntegrator;
import org.apache.commons.math3.analysis.solvers.BrentSolver;
import org.apache.commons.math3.analysis.solvers.UnivariateSolver;

import java.util.Locale;

/**
 * Logistic population model with a single constructor that accepts (t50, carryingCapacity, growthRate, NA, iNa).
 * <p>
 * If iNa=0 or ancestralPopulation <= 0, the model ignores NA:
 * <pre>
 *   N(t) = carryingCapacity / (1 + exp[growthRate * (t - t50)])
 * </pre>
 * If iNa=1 and ancestralPopulation > 0, NA is incorporated:
 * <pre>
 *   N(t) = ancestralPopulation +
 *          (carryingCapacity - ancestralPopulation)
 *            / (1 + exp[growthRate * (t - t50)])
 * </pre>
 * </p>
 * The coalescent intensity is calculated numerically via ∫1/N(t) dt.
 */
public class LogisticPopulation implements PopulationFunction {

    private final double t50;                // Logistic midpoint
    private final double carryingCapacity;   // Logistic carrying capacity (K)
    private final double growthRate;         // Logistic growth rate (b)
    private final double ancestralPopulation; // NA (may be 0 if ignored)
    private final int iNa;                  // 0 or 1

    // A small "resolution" factor used to check for extremely small N(t) if desired
    private static final double RESOLUTION_MAGIC_NUMBER = 1e3;

    /**
     * A single constructor for logistic growth with an optional ancestral population (NA) and indicator (iNa).
     * <p>
     *  - If iNa=1 and ancestralPopulation>0, we incorporate NA in the logistic formula.
     *  - Otherwise, the model ignores NA (effectively sets NA=0 internally).
     *
     * @param t50                The midpoint (inflection point) of the logistic function.
     * @param carryingCapacity   The carrying capacity K (must be >0).
     * @param growthRate         The logistic growth rate b (must be >=0).
     * @param ancestralPopulation Proposed ancestral population size (must be >=0, <= carryingCapacity).
     * @param iNa                0 or 1. If 1 and ancestralPopulation>0 => use NA, else ignore NA.
     */
    public LogisticPopulation(double t50,
                              double carryingCapacity,
                              double growthRate,
                              double ancestralPopulation,
                              int iNa)
    {
        if (carryingCapacity <= 0) {
            throw new IllegalArgumentException("Carrying capacity must be > 0.");
        }
        if (growthRate < 0) {
            throw new IllegalArgumentException("Growth rate cannot be negative.");
        }
        if (ancestralPopulation < 0) {
            throw new IllegalArgumentException("Ancestral population (NA) cannot be negative.");
        }

        if (iNa != 0 && iNa != 1) {
            throw new IllegalArgumentException("iNa must be 0 or 1.");
        }

        this.t50 = t50;
        this.carryingCapacity = carryingCapacity;
        this.growthRate = growthRate;

        // Determine whether to actually use NA:
        if (iNa == 1 && ancestralPopulation > 0.0) {
            this.ancestralPopulation = ancestralPopulation;
            this.iNa = 1;
        } else {
            // iNa=0 or NA <=0 => ignore NA
            this.ancestralPopulation = 0.0;
            this.iNa = 0;
        }
    }

    /**
     * Returns the logistic population size at time t.
     */
    @Override
    public double getTheta(double t) {
        if (isUsingAncestralPopulation()) {
            return ancestralPopulation
                    + (carryingCapacity - ancestralPopulation)
                    / (1.0 + Math.exp(growthRate * (t - t50)));
        } else {
            return carryingCapacity
                    / (1.0 + Math.exp(growthRate * (t - t50)));
        }
    }

    /**
     * Returns the coalescent intensity: ∫(0..t) [1/N(u)] du, computed by numerical integration.
     */
    @Override
    public double getIntensity(double t) {
        if (t <= 0.0) {
            return 0.0;
        }

        // Optional check for extremely small pop sizes:
        if (getTheta(t) < carryingCapacity / RESOLUTION_MAGIC_NUMBER) {
            // e.g. we might throw a warning or skip, but we'll proceed
        }

        UnivariateFunction integrand = x -> 1.0 / Math.max(getTheta(x), 1e-20);
        IterativeLegendreGaussIntegrator integrator = new IterativeLegendreGaussIntegrator(
                5, 1.0e-12, 1.0e-8, 2, 10000
        );
        return integrator.integrate(Integer.MAX_VALUE, integrand, 0.0, t);
    }

    /**
     * Solves for t given x = Intensity(t), using a Brent solver.
     */
    @Override
    public double getInverseIntensity(double x) {
        if (x <= 0.0) {
            return 0.0;
        }

        UnivariateFunction f = time -> getIntensity(time) - x;
        UnivariateSolver solver = new BrentSolver();

        double tMin, tMax;
        double intensityAtT50 = getIntensity(t50);

        if (x <= intensityAtT50) {
            tMin = 0.0;
            tMax = t50;
        } else {
            tMin = t50;
            tMax = 2.0 * t50;
            while (getIntensity(tMax) < x) {
                if (tMax < Double.MAX_VALUE / 2.0) {
                    tMax *= 2.0;
                } else {
                    tMax = Double.MAX_VALUE;
                    System.err.println("Reached Double.MAX_VALUE for tMax bracket");
                    break;
                }
            }
        }

        try {
            return solver.solve(100, f, tMin, tMax);
        } catch (Exception e) {
            throw new RuntimeException(
                    String.format("Failed to find valid time for intensity=%.4f", x), e
            );
        }
    }

    @Override
    public boolean isAnalytical() {
        // We use numeric integration
        return false;
    }

    /**
     * Checks if we are using an ancestral population in the model (iNa=1 and NA>0).
     */
    public boolean isUsingAncestralPopulation() {
        return (iNa == 1 && ancestralPopulation > 0.0);
    }

    // ------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------

    public double getT50() {
        return t50;
    }

    public double getCarryingCapacity() {
        return carryingCapacity;
    }

    public double getGrowthRate() {
        return growthRate;
    }

    public double getAncestralPopulation() {
        return ancestralPopulation;
    }

    public int getINa() {
        return iNa;
    }

    // ------------------------------------------------------------------
    // toString
    // ------------------------------------------------------------------

    @Override
    public String toString() {
        if (isUsingAncestralPopulation()) {
            return String.format(Locale.US,
                    "LogisticPopulation [t50=%.4f, K=%.4f, b=%.4f, NA=%.4f, iNa=%d]",
                    t50, carryingCapacity, growthRate, ancestralPopulation, iNa);
        } else {
            return String.format(Locale.US,
                    "LogisticPopulation [t50=%.4f, K=%.4f, b=%.4f, NA=%.4f, iNa=%d] (NA ignored)",
                    t50, carryingCapacity, growthRate, ancestralPopulation, iNa);
        }
    }
}
