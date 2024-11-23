package lphy.base.evolution.coalescent.populationmodel;

import lphy.base.evolution.coalescent.PopulationFunction;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.integration.IterativeLegendreGaussIntegrator;
import org.apache.commons.math3.analysis.solvers.BrentSolver;
import org.apache.commons.math3.analysis.solvers.UnivariateSolver;

public class ExponentialPopulation implements PopulationFunction {

    private double growthRate;
    private double N0;
    private double NA; // Ancestral population size
    private boolean useAncestralPopulation; // Flag to indicate whether to use NA


    /**
     * Constructor without ancestral population.
     *
     * @param growthRate Growth rate of the population.
     * @param N0         Initial population size at time t=0.
     */
    public ExponentialPopulation(double growthRate, double N0) {
        if (N0 <= 0) {
            throw new IllegalArgumentException("Initial population size N0 must be positive.");
        }
        this.growthRate = growthRate;
        this.N0 = N0;
        this.useAncestralPopulation = false;
        this.NA = 0.0; // Default value when not using NA
    }

    /**
     * Constructor with ancestral population.
     *
     * @param growthRate             Growth rate of the population.
     * @param N0                     Initial population size at time t=0.
     * @param ancestralPopulationSize Ancestral population size NA.
     */
    public ExponentialPopulation(double growthRate, double N0, double ancestralPopulationSize) {
        if (N0 <= 0) {
            throw new IllegalArgumentException("Initial population size N0 must be positive.");
        }
        if (ancestralPopulationSize <= 0) {
            throw new IllegalArgumentException("Ancestral population size NA must be positive.");
        }
        if (ancestralPopulationSize > N0) {
            throw new IllegalArgumentException("Ancestral population size NA cannot exceed initial population size N0.");
        }
        this.growthRate = growthRate;
        this.N0 = N0;
        this.useAncestralPopulation = true;
        this.NA = ancestralPopulationSize;
    }

    /**
     * Calculates the population size at a given time t.
     *
     * @param t Time at which to calculate the population size.
     * @return Population size N(t) at time t.
     */
    @Override
    public double getTheta(double t) {
        double r = growthRate;
        if (r == 0) {
            return useAncestralPopulation ? NA : N0;
        } else {
            if (useAncestralPopulation) {
                return (N0 - NA) * Math.exp(-r * t) + NA;
            } else {
                return N0 * Math.exp(-r * t);
            }
        }
    }

    /**
     * Calculates the cumulative intensity from time 0 to t.
     *
     * @param t Time up to which to calculate the cumulative intensity.
     * @return Cumulative intensity up to time t.
     */
    @Override
    public double getIntensity(double t) {
        if (t == 0) return 0.0;

        if (!useAncestralPopulation) {
            // Without NA, use analytical solution
            double r = growthRate;
            if (r == 0.0) {
                return t / N0;
            }
            return (Math.exp(r * t) - 1.0) / (r * N0);
        } else {
            // Numerical integration when using Ancestral Population (NA)
            UnivariateFunction function = time -> 1 / Math.max(getTheta(time), 1e-20);
            IterativeLegendreGaussIntegrator integrator = new IterativeLegendreGaussIntegrator(5, 1.0e-12, 1.0e-8, 2, 10000);


            return integrator.integrate(Integer.MAX_VALUE, function, 0, t);
        }
    }

    /**
     * Calculates the time t corresponding to a given cumulative intensity x.
     *
     * @param x Cumulative intensity.
     * @return Time t such that Intensity(t) = x.
     */
    @Override
    public double getInverseIntensity(double x) {
        if (x == 0.0) return 0.0;

        if (!useAncestralPopulation) {
            // Without NA, use analytical solution
            double r = growthRate;
            if (r == 0.0) {
                return N0 > 0 ? x * N0 : 0.0;
            }
            return Math.log(1.0 + x * r * N0) / r;
        } else {
            // With NA, use numerical solver
            UnivariateFunction function = time -> getIntensity(time) - x;
            UnivariateSolver solver = new BrentSolver();
            double tMin = 0.0;

            // Estimate initial tMax based on the given intensity x
            double tMax = estimateInitialTMax(x);

            // Ensure tMax does not exceed half of Double.MAX_VALUE
            tMax = Math.min(tMax, Double.MAX_VALUE / 2);

            // Attempt to solve within [tMin, tMax]
            try {
                return solver.solve(100, function, tMin, tMax);
            } catch (Exception e) {
                // If not found, double tMax and retry
                tMax *= 2.0;
                while (tMax < Double.MAX_VALUE / 2) {
                    try {
                        return solver.solve(100, function, tMin, tMax);
                    } catch (Exception ex) {
                        tMax *= 2.0;
                    }
                }
                throw new RuntimeException("Failed to find a valid time for given intensity: " + x, e);
            }
        }
    }

    /**
     * Estimates an initial tMax value to improve solver efficiency.
     *
     * @param x Cumulative intensity.
     * @return Estimated tMax.
     */
    private double estimateInitialTMax(double x) {
        // Based on cumulative intensity x and model parameters, estimate tMax
        // Assume when N(t) approaches NA, Intensity(t) ≈ t / (NA * (N0 - NA))
        // Hence, estimate tMax ≈ x * NA * (N0 - NA) * 1.5
        double estimatedTMax = x * NA * (N0 - NA) * 1.5;

        // Prevent tMax from being too small
        estimatedTMax = Math.max(estimatedTMax, 1.0);

        return estimatedTMax;
    }

    /**
     * Checks whether the model is using an ancestral population.
     *
     * @return True if using NA, false otherwise.
     */
    public boolean isUsingAncestralPopulation() {
        return useAncestralPopulation;
    }

    /**
     * Retrieves the ancestral population size NA.
     *
     * @return Ancestral population size NA.
     */
    public double getAncestralPopulation() {
        return NA;
    }

    /**
     * Indicates whether the model uses an analytical solution.
     *
     * @return True if using analytical solution (without NA), false otherwise.
     */
    @Override
    public boolean isAnalytical() {
        return !useAncestralPopulation; // Analytical only when not using NA
    }

    /**
     * Provides a string representation of the ExponentialPopulation model.
     *
     * @return String describing the model parameters.
     */
    @Override
    public String toString() {
        if (useAncestralPopulation) {
            return "Exponential Population: Growth Rate = " + growthRate +
                    ", Initial Size = " + N0 + ", Ancestral Population = " + NA;
        } else {
            return "Exponential Population: Growth Rate = " + growthRate +
                    ", Initial Size = " + N0;
        }
    }
}

