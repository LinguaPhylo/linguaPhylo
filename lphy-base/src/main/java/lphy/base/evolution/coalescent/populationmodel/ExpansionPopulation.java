package lphy.base.evolution.coalescent.populationmodel;

import lphy.base.evolution.coalescent.PopulationFunction;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.integration.IterativeLegendreGaussIntegrator;
import org.apache.commons.math3.analysis.solvers.BrentSolver;
import org.apache.commons.math3.analysis.solvers.UnivariateSolver;
import org.apache.commons.math3.exception.NoBracketingException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;

/**
 * ExpansionPopulation model with an optional ancestral size (NA), controlled by an indicator I_na.
 *
 * If I_na=0 or NA<=0, we ignore NA. Then:
 *   For 0 <= t <= x: N(t)=NC
 *   For t > x: N(t)= NC*exp(-r*(t - x))
 *
 * If I_na=1 and NA>0, then for t > x:
 *   N(t)= (NC - NA)*exp(-r*(t-x)) + NA
 *
 * We use numerical integration for getIntensity(t) when using NA,
 * and also a purely numerical approach in getInverseIntensity(...) whenever t > x.
 * This avoids closed-form solutions in the second segment.
 */
public class ExpansionPopulation implements PopulationFunction {

    // NC: population size at [0, x]
    private final double NC;
    // NA: ancestral size, used only if I_na=1 and NA>0
    private final double NA;
    // r : exponential rate
    private final double r;
    // x : boundary time for expansion start
    private final double x;
    // I_na: indicator 0 or 1
    private final int I_na;

    /**
     * Full constructor: If I_na=0 or NA<=0 => ignore NA, else use NA in second segment.
     *
     * @param NC initial population size for t in [0, x]
     * @param NA ancestral population size if I_na=1 and NA>0
     * @param r  exponential decay rate (>0)
     * @param x  time at which exponential decay starts (>=0)
     * @param I_na indicator: 0 or 1
     */
    public ExpansionPopulation(double NC, double NA, double r, double x, int I_na) {
        if (NC <= 0) {
            throw new IllegalArgumentException("NC must be > 0.");
        }
        if (r <= 0) {
            throw new IllegalArgumentException("r must be > 0.");
        }
        if (x < 0) {
            throw new IllegalArgumentException("x must be >= 0.");
        }
        if (I_na != 0 && I_na != 1) {
            throw new IllegalArgumentException("I_na must be 0 or 1.");
        }

        // If we plan to use NA, ensure NA>0 && NA<NC
        if (I_na == 1) {
            if (NA <= 0.0) {
                throw new IllegalArgumentException("NA must be > 0 when I_na=1.");
            }

        }

        this.NC  = NC;
        this.NA  = NA;
        this.r   = r;
        this.x   = x;
        this.I_na= I_na;
    }

    /**
     * A simpler constructor if you do not want to specify I_na explicitly (defaults to 1).
     */
    public ExpansionPopulation(double NC, double NA, double r, double x) {
        this(NC, NA, r, x, 1);
    }

    /**
     * Population size N(t):
     *   if t <= x => N(t)=NC
     *   if t > x:
     *     - if (I_na=0 or NA<=0) => N(t)= NC*exp(-r*(t-x))
     *     - if (I_na=1 and NA>0) => N(t)= (NC-NA)*exp(-r*(t-x))+NA
     */
    @Override
    public double getTheta(double t) {
        if (t < 0.0) {
            throw new IllegalArgumentException("Time t cannot be negative.");
        }

        if (t <= x) {
            return NC;
        } else {
            if (I_na==1 && NA>0.0) {
                // use ancestral size
                return (NC - NA)*Math.exp(-r*(t - x)) + NA;
            } else {
                // ignore NA => standard exponential
                return NC*Math.exp(-r*(t - x));
            }
        }
    }

    /**
     * Cumulative intensity from 0 to t: ∫(0->t) 1/N(u) du
     *   if t <= x => t/NC
     *   if t > x => x/NC + numeric integration if using NA, else closed form
     */
    @Override
    public double getIntensity(double t) {
        if (t < 0.0) {
            return 0.0;
        }
        if (t <= x) {
            return t/NC;
        } else {
            double firstPart = x/NC; // segment [0, x]

            if (I_na==1 && NA>0.0) {
                // no closed form => numeric integration
                UnivariateFunction integrand = (u) -> {
                    double popU = getTheta(u); // (NC-NA)*exp(-r(u-x)) + NA
                    return 1.0 / Math.max(popU, 1e-20);
                };
                IterativeLegendreGaussIntegrator integrator = new IterativeLegendreGaussIntegrator(
                        5, 1.0e-12, 1.0e-8, 2, 10000
                );
                double secondPart;
                try {
                    secondPart = integrator.integrate(Integer.MAX_VALUE, integrand, x, t);
                } catch (Exception e) {
                    throw new RuntimeException("Numeric integration failed from x="+x+" to t="+t, e);
                }
                return firstPart + secondPart;
            } else {
                // I_na=0 => NA ignored => we do have closed form
                // ∫(x->t) 1/[NC e^-r(u-x)] = (exp(r*(t-x)) -1)/(r*NC)
                double secondPart = (Math.exp(r*(t - x)) - 1.0)/(r*NC);
                return firstPart + secondPart;
            }
        }
    }

    /**
     * Inverse intensity: given intensityVal, solve for t.
     * We handle segment [0, x] analytically, and use a numeric solver for t>x.
     */
    @Override
    public double getInverseIntensity(double intensityVal) {
        if (intensityVal < 0.0) {
            throw new IllegalArgumentException("Intensity must be >=0.");
        }
        // boundary intensity at x
        double Ix = x / NC;

        // 1) if intensityVal <= Ix => in [0, x]
        if (intensityVal <= Ix) {
            // intensityVal= t/NC => t= intensityVal*NC
            return intensityVal * NC;
        } else {
            // 2) second segment => t> x
            //   use numeric approach in [x, someLargeNumber]
            //   because we might or might not have closed form
            //   but user specifically wants "calculator" approach => always numeric
            UnivariateFunction eqn = (timePoint) -> getIntensity(timePoint) - intensityVal;
            UnivariateSolver solver = new BrentSolver(1e-9, 1e-9);
            double tMin = x;
            double tMax = 1e9; // large bound, can expand if needed

            try {
                return solver.solve(1000, eqn, tMin, tMax);
            } catch (NoBracketingException | TooManyEvaluationsException e) {
                throw new IllegalArgumentException("No valid t in [x, ∞) for intensity="+intensityVal, e);
            }
        }
    }

    @Override
    public boolean isAnalytical() {
        // We'll treat it as 'not fully analytical' because second segment does numeric for I_na=1
        // or we might say "true if I_na=0" but let's keep it simple
        return false;
    }

    @Override
    public String toString() {
        return "ExpansionPopulation[I_na="+I_na+", NC="+NC+", NA="+NA
                +", r="+r+", x="+x+"]";
    }

    // ---------- Getters if needed ----------
    public double getNC() {
        return NC;
    }
    public double getNA() {
        return NA;
    }
    public double getR()  {
        return r;
    }
    public double getX()  {
        return x;
    }
    public int getI_na()  {
        return I_na;
    }
}
