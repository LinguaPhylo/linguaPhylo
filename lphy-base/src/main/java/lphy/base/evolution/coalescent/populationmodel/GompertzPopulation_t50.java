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

    // ----------------------------------------------------------------------
    // Parameter names
    // ----------------------------------------------------------------------
    public static final String BParamName         = "b";
    public static final String NINFINITYParamName = "NInfinity";
    public static final String T50ParamName       = "t50";
    public static final String NAParamName        = "NA";

    // ----------------------------------------------------------------------
    // Core fields
    // ----------------------------------------------------------------------
    private double N0;           // Initial population size
    private double b;            // Growth rate
    private double NInfinity;    // Carrying capacity
    double t50;                  // Time when population is half of carrying capacity
    private double NA;           // Ancestral population size
    private boolean useAncestralPopulation; // Derived from I_na + NA
    private int I_na = 1;        // 0 or 1, defaults to 1

    /**
     * Constructor without explicit NA. Defaults I_na=1 but sets NA=0 => effectively ignoring NA.
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
     * This constructor ensures that N0 is computed immediately if I_na=1 and NA>0,
     * so that getTheta(...) will work properly without requiring a separate setI_na(1) call.
     *
     * @param t50 time when population reaches half of carrying capacity
     * @param b growth rate
     * @param NInfinity carrying capacity
     * @param NA ancestral population size
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

        this.b         = b;
        this.NInfinity = NInfinity;
        this.I_na      = I_na;

        // If I_na=0 or NA <= 0 => treat as no NA
        if (I_na == 0 || NA <= 0.0) {
            this.NA = 0.0;
            this.useAncestralPopulation = false;

            // Recompute t50 and N0 ignoring NA
            this.t50 = computeT50(NInfinity, t50, b, false, 0.0);
            this.N0  = calculateN0(this.t50, b, NInfinity);
        } else {
            // I_na=1 && NA>0 => use NA
            this.NA = NA;
            this.useAncestralPopulation = true;

            // Immediately compute N0 here to avoid uninitialized N0
            double tempN0 = calculateN0(t50, b, NInfinity, NA);

//            // Check validity: NInfinity must be > NA, and N0 must be > NA
//            if (NInfinity <= NA) {
//                throw new IllegalArgumentException("NInfinity must be > NA when I_na=1.");
//            }
//            if (tempN0 <= NA) {
//                throw new IllegalArgumentException("Calculated N0 <= NA. Invalid usage with I_na=1.");
//            }

            // Optionally, recompute t50 using the effective scenario
            // to ensure t50 matches the final N0 used, if needed:
            t50 = computeT50(NInfinity, t50, b, true, NA);

            // Store final t50, N0
            this.t50 = t50;
            this.N0  = tempN0;
        }
    }

    /**
     * getTheta(t): if using NA, uses the extended Gompertz formula;
     * otherwise, uses the standard Gompertz formula.
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

    /**
     * getIntensity(t) = ∫ 1 / N(u) du from 0 to t (numerical).
     */
    @Override
    public double getIntensity(double t) {
        if (t <= 0.0) {
            return 0.0;
        }
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
     * Static method to compute t50 if needed, depending on whether NA is used.
     * The second parameter is treated as "N0" in the logic, but you can pass a
     * provisional t50 or N0 depending on your usage.
     */
    public static double computeT50(double NInfinity, double N0, double b, boolean useAncestralPopulation, double NA) {
        if (b <= 0) {
            throw new IllegalArgumentException("Growth rate b must be >0.");
        }
        double ratio;
        if (useAncestralPopulation) {
            double effN0 = N0 - NA;
            double effNInfinity = NInfinity - NA;
            ratio = effNInfinity / effN0;
        } else {
            if (N0 <= 0 || NInfinity <= N0) {
                throw new IllegalArgumentException("NInfinity must be > N0 > 0 for no-NA scenario.");
            }
            ratio = NInfinity / N0;
        }
//        if (ratio <= 1) {
//            throw new IllegalArgumentException("N0 must be < NInfinity.");
//        }
        double proportion = 0.5;
        return Math.log(1 - Math.log(proportion) / Math.log(ratio)) / b;
    }

    /**
     * Static method to compute N0 when ignoring NA.
     */
    public static double calculateN0(double t50, double b, double NInfinity) {
        return NInfinity * Math.pow(2, -Math.exp(-b * t50));
    }

    /**
     * Static method to compute N0 when NA>0.
     */
    public static double calculateN0(double t50, double b, double NInfinity, double NA) {
        double exponent = -Math.log(2) / Math.exp(b * t50);
        double N0_minus_NA = (NInfinity - NA) * Math.exp(exponent);
        return N0_minus_NA + NA;
    }

    /**
     * Returns t50 from the final model perspective, usually getTimeForGivenProportion(0.5).
     */
    public double getT50() {
        return getTimeForGivenProportion(0.5);
    }

    /**
     * Returns the initial population size N0.
     */
    @MethodInfo(description = "Get the initial population size N0", category = GeneratorCategory.COAL_TREE,
            examples = {"gompertzCoalescent_t50.lphy"})
    public double getN0() {
        return this.N0;
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

    /**
     * Sets I_na (0 or 1). If set to 0 => ignore NA => recalc N0 ignoring NA.
     * If set to 1 && NA>0 => we re-derive N0 with NA, ensuring validity.
     *
     * This method can be used to switch between ignoring NA and using NA
     * after construction, but it is not strictly required anymore to
     * initialize N0 properly if the constructor was already given (I_na=1, NA>0).
     *
     * @param i_na 0 or 1
     */
    public void setI_na(int i_na) {
        if (i_na != 0 && i_na != 1) {
            throw new IllegalArgumentException("I_na must be 0 or 1.");
        }
        this.I_na = i_na;
        if (i_na == 0) {
            NA = 0.0;
            useAncestralPopulation = false;
            // Recompute N0 ignoring NA
            t50 = computeT50(NInfinity, t50, b, false, 0.0);
            N0  = calculateN0(t50, b, NInfinity);
        } else {
            if (NA > 0.0) {
                useAncestralPopulation = true;
                // Ensure NInfinity>NA, re-check
                if (NInfinity <= NA) {
                    throw new IllegalArgumentException("NInfinity must be > NA when I_na=1.");
                }
                double tempN0 = calculateN0(t50, b, NInfinity, NA);
                if (tempN0 <= NA) {
                    throw new IllegalArgumentException("Calculated N0 <= NA. Invalid usage with I_na=1.");
                }
                t50 = computeT50(NInfinity, t50, b, true, NA);
                N0  = tempN0;
            } else {
                // NA=0 => same as ignoring
                useAncestralPopulation = false;
                t50 = computeT50(NInfinity, t50, b, false, 0.0);
                N0  = calculateN0(t50, b, NInfinity);
            }
        }
    }

    /**
     * Returns true if NA is actually used.
     */
    public boolean isUsingAncestralPopulation() {
        return useAncestralPopulation;
    }

    @Override
    public String toString() {
        if (useAncestralPopulation) {
            return String.format(
                    "Gompertz_t50 Model with NA: t50=%.4f, NInfinity=%.4f, b=%.4f, N0=%.4f, NA=%.4f, I_na=%d",
                    t50, NInfinity, b, N0, NA, I_na
            );
        } else {
            return String.format(
                    "Gompertz_t50 Model: t50=%.4f, NInfinity=%.4f, b=%.4f, N0=%.4f, (NA=%.4f ignored), I_na=%d",
                    t50, NInfinity, b, N0, NA, I_na
            );
        }
    }
}
