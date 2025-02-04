package lphy.base.evolution.coalescent.populationmodel;

import lphy.base.evolution.coalescent.PopulationFunction;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.MethodInfo;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.integration.IterativeLegendreGaussIntegrator;
import org.apache.commons.math3.analysis.solvers.BrentSolver;
import org.apache.commons.math3.analysis.solvers.UnivariateSolver;

/**
 * Gompertz population model parameterized by t50, with optional ancestral size NA
 * controlled by an indicator I_na (0 or 1).
 * If I_na=1 and NA>0, the model uses NA; otherwise, it behaves as if no ancestral population is present.
 *
 * This version ensures that N0 is immediately computed in the constructor if I_na=1 and NA>0,
 * preventing a situation where N0 remains uninitialized until setI_na(1) is explicitly called.
 */
public class GompertzPopulation_t50 implements PopulationFunction {


    private double N0;           // Initial population size
    private double b;            // Growth rate
    private double NInfinity;    // Carrying capacity
    double t50;                  // Time when population is half of carrying capacity
    private double NA;           // Ancestral population size
    private boolean useAncestralPopulation; // Derived from I_na + NA
    private int I_na = 1;        // 0 or 1, defaults to 1
    private double userT50;


    /**
     * Constructor without NA. Defaults I_na=1 but sets NA=0 => effectively ignoring NA.
     */
    public GompertzPopulation_t50(double t50, double b, double NInfinity) {
        this(t50, b, NInfinity, 0.0, 1);
    }

    /**
     * Constructor with NA, defaults I_na=1. If NA=0 => effectively no ancestral population.
     */
    public GompertzPopulation_t50(double t50, double b, double NInfinity, double ancestralPopulationSize) {
        this(t50, b, NInfinity, ancestralPopulationSize, 1);
    }

    /**
     * Full constructor: if I_na=0 => ignore NA; if I_na=1 && NA>0 => use NA.
     *
     * t50 is treated as a "time parameter" for half-carrying capacity.
     * Then N0 is derived from t50.
     *
     * @param t50  time when population reaches half of carrying capacity
     * @param b    growth rate (> 0)
     * @param NInfinity carrying capacity (> 0)
     * @param NA   ancestral population size (>= 0)
     * @param I_na indicator: 0 or 1
     */
    public GompertzPopulation_t50(double t50, double b, double NInfinity, double NA, int I_na) {
        if (b <= 0) {
            throw new IllegalArgumentException("Growth rate b must be > 0.");
        }
        if (NInfinity <= 0) {
            throw new IllegalArgumentException("NInfinity must be > 0.");
        }
        if (NA < 0) {
            throw new IllegalArgumentException("Ancestral population size NA must be >= 0.");
        }
        if (I_na != 0 && I_na != 1) {
            throw new IllegalArgumentException("I_na must be 0 or 1.");
        }

        this.b = b;
        this.NInfinity = NInfinity;
        this.I_na = I_na;
        this.userT50 = t50;  // store user input
        this.t50 = t50;      // the actual t50 used in the model

        if (I_na == 1 && NA > 0.0) {
            this.NA = NA;
            useAncestralPopulation = true;
            // Calculate N0 from the t50-based formula with NA
            this.N0 = calculateN0(t50, b, NInfinity, NA);
        } else {
            this.NA = 0.0;
            useAncestralPopulation = false;
            // Calculate N0 ignoring NA
            this.N0 = calculateN0(t50, b, NInfinity);
        }
    }

    /**
     * getTheta(t): standard Gompertz if ignoring NA;
     * extended formula if useAncestralPopulation==true.
     */
    @Override
    public double getTheta(double t) {
        if (useAncestralPopulation) {
            double N0_minus_NA   = N0 - NA;
            double Ninf_minus_NA = NInfinity - NA;
            double exponent = Math.log(Ninf_minus_NA / N0_minus_NA) * (1 - Math.exp(b * t));
            return N0_minus_NA * Math.exp(exponent) + NA;
        } else {
            double logRatio = Math.log(NInfinity / N0);
            double exponent = (1 - Math.exp(b * t)) * logRatio;
            return N0 * Math.exp(exponent);
        }
    }

    @Override
    public double getIntensity(double t) {
        if (t <= 0.0) return 0.0;
        UnivariateFunction function = x -> 1.0 / Math.max(getTheta(x), 1e-20);
        IterativeLegendreGaussIntegrator integrator = new IterativeLegendreGaussIntegrator(
                5, 1e-12, 1e-8, 2, 10000
        );
        return integrator.integrate(Integer.MAX_VALUE, function, 0.0, t);
    }


    /**
     * getInverseIntensity(x) solves for time T where getIntensity(T) = x (numerical approach).
     */
    @Override
    public double getInverseIntensity(double target) {
        double time = 0.0;
        double proportionForT1  = 0.01;
        double proportionForT50 = 0.5;
        double t1  = getTimeForGivenProportion(proportionForT1);
        double t50 = getTimeForGivenProportion(proportionForT50);

        double growthPhaseTime = t1 - t50;
        double step = growthPhaseTime / 100.0;

        double intensity = getIntensity(time);
        while (intensity < target) {
            time += step;
            intensity = getIntensity(time);
            if (time > 1e8) {
                break;
            }
        }
        double lower = Math.max(0, time - step);
        double upper = time;

        UnivariateFunction f = val -> getIntensity(val) - target;
        UnivariateSolver solver = new BrentSolver(1e-9, 1e-9);
        try {
            return solver.solve(100, f, lower, upper);
        } catch (Exception e) {
            return Double.NaN;
        }
    }
    @Override
    public boolean isAnalytical() {
        return false;
    }

    /**
     * Returns t50 from the final model perspective.
     * In principle, this might differ if we recast b or NInfinity, but here we keep it fixed as userT50
     * or we simply do getTimeForGivenProportion(0.5).
     */
    public double getT50() {
        return t50;
    }

    @MethodInfo(description = "Get the initial population size N0 derived from t50",
            category = GeneratorCategory.COAL_TREE,
            examples = {"gompertzCoalescent_t50.lphy"})
    public double getN0() {
        return N0;
    }


    /**
     * Returns the time at which N(t) is fraction k of the ratio
     * (NInfinity - NA)/(N0 - NA) if using NA; otherwise NInfinity/N0.
     */
    public double getTimeForGivenProportion(double k) {
        if (b == 0.0) {
            throw new IllegalArgumentException("Growth rate b cannot be zero.");
        }
        double effN0, effNInfinity;
        if (useAncestralPopulation) {
            effN0 = N0 - NA;
            effNInfinity = NInfinity - NA;
        } else {
            effN0 = N0;
            effNInfinity = NInfinity;
        }
        if (effN0 <= 0 || effNInfinity <= 0) {
            throw new IllegalArgumentException("Effective N0 and NInfinity must be >0.");
        }
        double ratio = effNInfinity / effN0;
        double proportion = k * ratio;
        if (proportion <= 0.0 || proportion >= ratio) {
            throw new IllegalArgumentException("Proportion must be between 0 and " + ratio);
        }
        return Math.log(1 - Math.log(proportion) / Math.log(ratio)) / b;
    }

    public void setI_na(int i_na) {
        if (i_na != 0 && i_na != 1) {
            throw new IllegalArgumentException("I_na must be 0 or 1.");
        }
        this.I_na = i_na;
        if (i_na == 1 && NA > 0.0) {
            useAncestralPopulation = true;
            N0 = calculateN0(t50, b, NInfinity, NA);
        } else {
            useAncestralPopulation = false;
            NA = 0.0;
            N0 = calculateN0(t50, b, NInfinity);
        }

    }

    /**
     * Returns true if NA is actually used.
     */
    public boolean isUsingAncestralPopulation() {
        return useAncestralPopulation;
    }

    /**
     * If you want an alternative approach for the "time at which population is half of NInfinity",
     * you could add the 'computeT50FromN0(...)' function, but here we interpret user input t50 as final.
     */

    // --- Helper calculations to get N0 from t50 ---
    public static double calculateN0(double t50, double b, double NInfinity) {
        return NInfinity * Math.pow(2, -Math.exp(-b * t50));
    }
    public static double calculateN0(double t50, double b, double NInfinity, double NA) {
        double exponent = -Math.log(2) / Math.exp(b * t50);
        double N0_minus_NA = (NInfinity - NA) * Math.exp(exponent);
        return N0_minus_NA + NA;
    }

    @Override
    public String toString() {
        if (useAncestralPopulation) {
            return String.format(
                    "Gompertz_t50 Model with NA: T50=%.4f, b=%.4f, NInfinity=%.4f, N0=%.4f, NA=%.4f, I_na=%d",
                    userT50, b, NInfinity, N0, NA, I_na, t50
            );
        } else {
            return String.format(
                    "Gompertz_t50 Model: T50=%.4f, b=%.4f, NInfinity=%.4f, N0=%.4f, (NA=%.4f ignored), I_na=%d",
                    userT50, b, NInfinity, N0, NA, I_na, t50
            );
        }
    }

}
