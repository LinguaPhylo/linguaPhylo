package lphy.base.evolution.coalescent.populationmodel;

import lphy.base.evolution.coalescent.PopulationFunction;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.integration.IterativeLegendreGaussIntegrator;
import org.apache.commons.math3.analysis.solvers.BrentSolver;
import org.apache.commons.math3.analysis.solvers.UnivariateSolver;

/**
 * Exponential growth population model with optional ancestral population size NA.
 *
 * This model supports:
 * - If I_na=0, NA is effectively 0, and the population size follows N(t)=N0*exp(-r*t) for r>0.
 *   If r=0, population size is constant at N0.
 *
 * - If I_na=1 and NA>0, the population size transitions from N0 at t=0 to NA as t grows large:
 *   N(t) = (N0 - NA)*exp(-r*t) + NA for r>0.
 *   If r=0, population size is constant at NA.
 *
 * Intensity calculations:
 * - If not using NA (I_na=0 or NA=0), we have a closed-form solution for intensity and inverse intensity.
 * - If using NA (I_na=1 and NA>0), we must use numerical integration and root finding for intensity and inverse intensity.
 */
public class ExponentialPopulation implements PopulationFunction {

    private double growthRate;
    private double N0;
    private double NA;   // original ancestral population size
    private int I_na;    // indicator: 0 or 1

    /**
     * Constructor without NA, defaults I_na=1 but NA=0, so effectively not using NA.
     * @param growthRate growth rate r
     * @param N0 current population size at t=0 (must be >0)
     */
    public ExponentialPopulation(double growthRate, double N0) {
        if (N0 <= 0) {
            throw new IllegalArgumentException("N0 must be positive.");
        }
        this.growthRate = growthRate;
        this.N0 = N0;
        this.NA = 0.0;
        this.I_na = 1; // can be changed by setI_na if needed
    }

    /**
     * Constructor with NA, defaults I_na=1.
     * If NA=0, effectively same as not using NA unless changed.
     * @param growthRate growth rate r
     * @param N0 current population size at t=0 (>0)
     * @param NA ancestral population size (>=0)
     */
    public ExponentialPopulation(double growthRate, double N0, double NA) {
        if (N0 <= 0) {
            throw new IllegalArgumentException("N0 must be positive.");
        }
        if (NA < 0) {
            throw new IllegalArgumentException("NA must be non-negative.");
        }
        this.growthRate = growthRate;
        this.N0 = N0;
        this.NA = NA;
        this.I_na = 1;
    }

    /**
     * Full constructor with NA and I_na.
     * If I_na=0, NA is ignored (treated as 0).
     * @param growthRate growth rate r
     * @param N0 current population size at t=0 (>0)
     * @param NA ancestral population size (>=0)
     * @param I_na indicator, must be 0 or 1
     */
    public ExponentialPopulation(double growthRate, double N0, double NA, int I_na) {
        if (N0 <= 0) {
            throw new IllegalArgumentException("N0 must be positive.");
        }
        if (I_na != 0 && I_na != 1) {
            throw new IllegalArgumentException("I_na must be 0 or 1.");
        }
        if (NA < 0) {
            throw new IllegalArgumentException("NA must be non-negative.");
        }
        this.growthRate = growthRate;
        this.N0 = N0;
        this.NA = NA;
        this.I_na = I_na;
    }

    public void setI_na(int I_na) {
        if (I_na != 0 && I_na != 1) {
            throw new IllegalArgumentException("I_na must be 0 or 1.");
        }
        this.I_na = I_na;
    }

    /**
     * @return Effective NA = I_na * NA. If I_na=0, NA is effectively 0.
     */
    private double getEffectiveNA() {
        return I_na * NA;
    }

    /**
     * Returns population size at time t.
     * If r=0:
     *   - If using NA (I_na=1 and NA>0), population = NA
     *   - Else population = N0
     * If r>0:
     *   - If using NA, N(t) = (N0 - NA)*exp(-r*t)+NA
     *   - Else N(t)=N0*exp(-r*t)
     *
     * @param t time
     * @return population size at time t
     */
    public double getTheta(double t) {
        double r = growthRate;
        double effNA = getEffectiveNA();

        if (r == 0.0) {
            // constant population size
            return effNA > 0.0 ? effNA : N0;
        } else {
            // exponential model with or without NA
            if (effNA > 0.0) {
                return (N0 - effNA) * Math.exp(-r * t) + effNA;
            } else {
                return N0 * Math.exp(-r * t);
            }
        }
    }

    /**
     * Returns the coalescent intensity at time t:
     * Intensity(t) = ∫(0 to t) (1/N(u)) du.
     * If not using NA, we have closed-form solutions.
     * If using NA, use numerical integration.
     *
     * @param t time
     * @return intensity at time t
     */
    @Override
    public double getIntensity(double t) {
        if (t == 0.0) return 0.0;

        double effNA = getEffectiveNA();
        double r = growthRate;

        if (effNA <= 0.0) {
            // no NA usage, closed form
            if (r == 0.0) {
                // integral(1/N0 dt) = t/N0
                return t / N0;
            } else {
                // integral from 0 to t of exp(r*u)/(N0) du = (exp(r*t)-1)/(r*N0)
                return (Math.exp(r * t) - 1.0) / (r * N0);
            }
        } else {
            // using NA, need numerical integration
            UnivariateFunction f = time -> 1.0 / Math.max(getTheta(time), 1e-20);
            IterativeLegendreGaussIntegrator integrator =
                    new IterativeLegendreGaussIntegrator(5, 1.0e-12, 1.0e-8, 2, 10000);
            return integrator.integrate(Integer.MAX_VALUE, f, 0, t);
        }
    }

    /**
     * Inverse intensity function: given x = Intensity(t), solve for t.
     * If not using NA, we have a closed form solution.
     * If using NA, we resort to a numerical root-finding approach.
     *
     * @param x intensity value
     * @return time t such that Intensity(t)=x
     */
    @Override
    public double getInverseIntensity(double x) {
        if (x == 0.0) return 0.0;

        double effNA = getEffectiveNA();
        double r = growthRate;
        if (effNA <= 0.0) {
            // no NA usage, closed form inverse
            if (r == 0.0) {
                return x * N0; // from t/N0 = x => t = x*N0
            }
            // (exp(r*t)-1)/(r*N0)=x => exp(r*t)=1+r*N0*x => t=log(1+r*N0*x)/r
            return Math.log(1.0 + x * r * N0) / r;
        } else {
            // using NA, need root finding
            UnivariateFunction f = time -> getIntensity(time) - x;
            UnivariateSolver solver = new BrentSolver();

            double tMin = 0.0;
            double tMax = estimateInitialTMax(x, effNA);
            tMax = Math.min(tMax, Double.MAX_VALUE / 2);

            try {
                return solver.solve(100, f, tMin, tMax);
            } catch (Exception e) {
                // if initial guess fails, try expanding tMax
                tMax *= 2.0;
                while (tMax < Double.MAX_VALUE / 2) {
                    try {
                        return solver.solve(100, f, tMin, tMax);
                    } catch (Exception ex) {
                        tMax *= 2.0;
                    }
                }
                throw new RuntimeException("Failed to find a valid time for intensity x=" + x, e);
            }
        }
    }

    /**
     * Heuristic for estimating an initial upper bound tMax for the solver.
     */
    private double estimateInitialTMax(double x, double effNA) {
        // Just a heuristic to start searching
        double estimatedTMax = x * effNA * (N0 - effNA) * 1.5;
        return Math.max(estimatedTMax, 1.0);
    }

    /**
     * Whether we are using ancestral population size in the model.
     * @return true if I_na=1 and NA>0
     */
    public boolean isUsingAncestralPopulation() {
        return getEffectiveNA() > 0.0;
    }

    public double getAncestralPopulation() {
        return NA;
    }

    /**
     * Indicates if a closed-form solution is used:
     * When not using NA (I_na=0 or NA=0), we have a closed form for intensity.
     */
    @Override
    public boolean isAnalytical() {
        return !isUsingAncestralPopulation();
    }

    @Override
    public String toString() {
        return "ExponentialPopulation: GrowthRate=" + growthRate +
                ", N0=" + N0 + ", NA=" + NA + ", I_na=" + I_na;
    }
}
