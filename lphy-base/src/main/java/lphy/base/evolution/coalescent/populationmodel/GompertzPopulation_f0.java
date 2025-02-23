package lphy.base.evolution.coalescent.populationmodel;

import lphy.base.evolution.coalescent.PopulationFunction;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.integration.IterativeLegendreGaussIntegrator;
import org.apache.commons.math3.analysis.solvers.BrentSolver;
import org.apache.commons.math3.analysis.solvers.UnivariateSolver;
import org.apache.commons.math3.exception.NoBracketingException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;

import java.util.Locale;

/**
 * Gompertz population model (f0-parameterized) with an optional ancestral population size (NA)
 * controlled by an indicator I_na (0 or 1).
 * <p>
 * <strong>Model summary:</strong>
 * <ul>
 *   <li>If <code>I_na=0</code> (or <code>NA=0</code>), the model ignores NA and uses the original Gompertz formula.</li>
 *   <li>If <code>I_na=1</code> and <code>NA>0</code>, we incorporate NA into the extended Gompertz formula as a baseline population size.</li>
 * </ul>
 * N<sub>∞</sub> is computed as <code>NInfinity = N0 / f0</code>.
 * <p>
 * <strong>Intensity &amp; Inverse Intensity:</strong>
 * Both are computed numerically, regardless of I_na, since this implementation does not provide a closed-form solution.
 */
public class GompertzPopulation_f0 implements PopulationFunction {

    private double N0;         // Initial population size
    private double f0;         // Initial proportion of the carrying capacity (N0/NInfinity)
    private double b;          // Growth rate parameter
    private double NInfinity;  // Carrying capacity, computed as N0/f0
    private double NA;         // Ancestral population size (>= 0)
    private int    I_na;       // Indicator: 0 or 1


    /**
     * Constructor without NA, defaults I_na=1 but NA=0 => effectively ignoring NA.
     * @param N0  initial population size (> 0)
     * @param f0  initial proportion (N0/NInfinity) (> 0)
     * @param b   growth rate (> 0)
     */
    public GompertzPopulation_f0(double N0, double f0, double b) {
        validatePositive(N0, "N0");
        validatePositive(f0, "f0");
        validatePositive(b,   "b");

        this.N0   = N0;
        this.f0   = f0;
        this.b    = b;
        this.NInfinity = N0 / f0;

        this.NA   = 0.0;  // effectively no NA
        this.I_na = 1;    // can be changed later if needed
    }

    /**
     * Constructor with NA, defaults I_na=1.
     * If NA=0, effectively ignoring NA (unless I_na changed).
     * @param N0 initial population size (>0)
     * @param f0 initial proportion (>0)
     * @param b  growth rate (>0)
     * @param NA ancestral population size (>=0, <=NInfinity)
     */
    public GompertzPopulation_f0(double N0, double f0, double b, double NA) {
        validatePositive(N0, "N0");
        validatePositive(f0, "f0");
        validatePositive(b,   "b");
        validateNA(NA, N0/f0);

        this.N0   = N0;
        this.f0   = f0;
        this.b    = b;
        this.NInfinity = N0 / f0;

        this.NA   = NA;
        this.I_na = 1;
    }

    /**
     * Full constructor with NA and I_na.
     * If I_na=0 => NA is effectively 0; if I_na=1 => use NA if NA>0.
     * @param N0  initial population size (>0)
     * @param f0  initial proportion (>0)
     * @param b   growth rate (>0)
     * @param NA  ancestral population size (>=0, <=NInfinity)
     * @param I_na 0 or 1
     */
    public GompertzPopulation_f0(double N0, double f0, double b, double NA, int I_na) {
        validatePositive(N0, "N0");
        validatePositive(f0, "f0");
        validatePositive(b,   "b");
        validateNA(NA, N0/f0);
        validateIndicator(I_na);

        this.N0   = N0;
        this.f0   = f0;
        this.b    = b;
        this.NInfinity = N0 / f0;

        this.NA   = NA;
        this.I_na = I_na;
        if (this.I_na == 0) {
            // If iNa=0 => ignore NA => set it to 0
            this.NA = 0.0;
        }
    }

    /**
     * Population size N(t) under the Gompertz model:
     * <ul>
     *   <li>If I_na=0 or NA=0 => original Gompertz formula
     *     <pre>
     *       N(t) = N0 * exp( log(N∞/N0)*(1 - exp(b*t)) )
     *     </pre>
     *   <li>If I_na=1 and NA>0 => extended formula with baseline NA
     *     <pre>
     *       N(t) = (N0 - NA)*exp( log((N∞ - NA)/(N0 - NA))*(1 - exp(b*t)) ) + NA
     *     </pre>
     * </ul>
     * @param t time
     * @return N(t) at time t
     */
    @Override
    public double getTheta(double t) {
        if (getEffectiveNA() > 0.0) {
            // Extended Gompertz
            double numerator   = NInfinity - getEffectiveNA();
            double denominator = N0         - getEffectiveNA();
            double logRatio    = Math.log(numerator / denominator);
            double exponent    = (1 - Math.exp(b * t)) * logRatio;
            return (N0 - getEffectiveNA()) * Math.exp(exponent) + getEffectiveNA();
        } else {
            // Original Gompertz
            double logRatio = Math.log(NInfinity / N0);
            double exponent = (1 - Math.exp(b * t)) * logRatio;
            return N0 * Math.exp(exponent);
        }
    }

    @Override
    public double getIntensity(double t) {
        if (t <= 0.0) {
            return 0.0;
        }
        UnivariateFunction integrand = x -> 1.0 / Math.max(getTheta(x), 1e-20);
        IterativeLegendreGaussIntegrator integrator = new IterativeLegendreGaussIntegrator(5, 1.0e-12, 1.0e-8, 2, 10000);
        return integrator.integrate(100000, integrand, 0.0, t);
    }

    @Override
    public double getInverseIntensity(double x) {
        if (x <= 0.0) {
            return 0.0;
        }
        UnivariateFunction f = time -> getIntensity(time) - x;
        UnivariateSolver solver = new BrentSolver();

        double tMin = 0.0;
        double tMax = 1.0;

        // Expand until we bracket the root or reach a huge upper bound
        while (getIntensity(tMax) < x && tMax < Double.MAX_VALUE / 2) {
            tMax *= 1.001;
        }

        try {
            return solver.solve(100, f, tMin, tMax);
        } catch (NoBracketingException | TooManyEvaluationsException e) {
            throw new RuntimeException("Failed to find a valid time for intensity=" + x, e);
        }
    }

    @Override
    public boolean isAnalytical() {
        // We rely on numerical integration for intensity
        return false;
    }


    /**
     * Effective NA = I_na * NA. If I_na=0 => NA=0, ignoring ancestral population.
     */
    private double getEffectiveNA() {
        return I_na * NA;
    }

    /**
     * Whether we are actually using the ancestral population size.
     */
    public boolean isUsingAncestralPopulation() {
        return getEffectiveNA() > 0.0;
    }

    public double getN0() {
        return N0;
    }

    public double getF0() {
        return f0;
    }

    public double getB() {
        return b;
    }

    public double getNA() {
        return NA;
    }

    public int getI_na() {
        return I_na;
    }

    public double getNInfinity() {
        return NInfinity;
    }


    private void validatePositive(double val, String paramName) {
        if (val <= 0.0) {
            throw new IllegalArgumentException(paramName + " must be > 0.");
        }
    }

    private void validateNA(double NA, double maxNA) {
        if (NA < 0.0) {
            throw new IllegalArgumentException("NA must be >= 0.");
        }

    }

    private void validateIndicator(int iNa) {
        if (iNa != 0 && iNa != 1) {
            throw new IllegalArgumentException("I_na must be 0 or 1.");
        }
    }


    @Override
    public String toString() {
        if (isUsingAncestralPopulation()) {
            return String.format(Locale.US,
                    "GompertzPopulation_f0: N0=%.4f, f0=%.4f, b=%.4f, NInfinity=%.4f, NA=%.4f, I_na=%d",
                    N0, f0, b, NInfinity, NA, I_na
            );
        } else {
            return String.format(Locale.US,
                    "GompertzPopulation_f0: N0=%.4f, f0=%.4f, b=%.4f, NInfinity=%.4f, (NA=%.4f ignored), I_na=%d",
                    N0, f0, b, NInfinity, NA, I_na
            );
        }
    }
}
