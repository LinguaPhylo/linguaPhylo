package lphy.base.evolution.coalescent.populationmodel;

import lphy.base.evolution.coalescent.PopulationFunction;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.MethodInfo;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.integration.IterativeLegendreGaussIntegrator;
import org.apache.commons.math3.analysis.solvers.BrentSolver;
import org.apache.commons.math3.analysis.solvers.UnivariateSolver;
import org.apache.commons.math3.exception.NoBracketingException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;



public class GompertzPopulation_t50 implements PopulationFunction {

    public static final String BParamName = "b";
    public static final String NINFINITYParamName = "NInfinity";
    public static final String T50ParamName = "t50";
    public static final String F0ParamName = "f0";
    public static final String NAParamName = "NA";

    private double N0;  // Initial population size
    private double b;   // Initial growth rate of tumor growth
    private double NInfinity; // Carrying capacity
    double t50; // Time when population is half of carrying capacity
    private double f0;
    private double NA;
    private boolean useAncestralPopulation;

    public GompertzPopulation_t50(double t50, double b, double NInfinity) {
        this.b = b;
        this.t50 = t50;
        this.NInfinity = NInfinity;
        this.NA = 0.0;
        this.useAncestralPopulation = false;

        this.t50 = computeT50(NInfinity, t50, b, false, 0.0);
        this.N0 = calculateN0(t50, b, NInfinity);
    }

    public GompertzPopulation_t50(double t50, double b, double NInfinity, double ancestralPopulationSize) {
        if (ancestralPopulationSize < 0) {
            throw new IllegalArgumentException("Ancestral population size NA must be non-negative.");
        }
        this.b = b;
        this.t50 = t50;
        this.NInfinity = NInfinity;
        this.NA = ancestralPopulationSize;
        this.useAncestralPopulation = true;
        this.N0 = calculateN0(t50, b, NInfinity, NA);
        if (N0 <= NA) {
            throw new IllegalArgumentException("Initial population size N0 must be greater than ancestral population size NA.");
        }
        if (NInfinity <= NA) {
            throw new IllegalArgumentException("Carrying capacity NInfinity must be greater than ancestral population size NA.");
        }
    }



    public static double computeT50(double NInfinity, double N0, double b, boolean useAncestralPopulation, double NA) {
        if (b <= 0) {
            throw new IllegalArgumentException("b must be greater than 0.");
        }

        double ratio;
        if (useAncestralPopulation) {
            double N0Effective = N0 - NA;
            double NInfinityEffective = NInfinity - NA;

            if (N0Effective <= 0 || NInfinityEffective <= 0) {
                throw new IllegalArgumentException("Effective N0 and NInfinity must be positive.");
            }

            ratio = NInfinityEffective / N0Effective;
        } else {
            ratio = NInfinity / N0;
        }

        if (ratio <= 1) {
            throw new IllegalArgumentException("N0 must be less than NInfinity.");
        }

        double proportion = 0.5;
        return Math.log(1 - Math.log(proportion) / Math.log(ratio)) / b;
    }


    public static double calculateN0(double t50, double b, double NInfinity) {
        return NInfinity * Math.pow(2, -Math.exp(-b * t50));
    }

    public static double calculateN0(double t50, double b, double NInfinity, double NA) {
        double exponent = -Math.log(2) / Math.exp(b * t50);
        double N0_minus_NA = (NInfinity - NA) * Math.exp(exponent);
        return N0_minus_NA + NA;
    }

    @Override
    public double getTheta(double t) {
        if (useAncestralPopulation) {
            double N0_minus_NA = N0 - NA;
            double Ninf_minus_NA = NInfinity - NA;
            double exponent = Math.log(Ninf_minus_NA / N0_minus_NA) * (1 - Math.exp(b * t));
            return N0_minus_NA * Math.exp(exponent) + NA;
        } else {
            return N0 * Math.exp(Math.log(NInfinity / N0) * (1 - Math.exp(b * t)));
        }
    }

    @Override
    public double getIntensity(double t) {
        if (t == 0) return 0.0;

        UnivariateFunction function = time -> 1 / Math.max(getTheta(time), 1e-20);
        IterativeLegendreGaussIntegrator integrator = new IterativeLegendreGaussIntegrator(5, 1.0e-12, 1.0e-8, 2, 10000);
        return integrator.integrate(Integer.MAX_VALUE, function, 0, t);
    }

    @Override
    public double getInverseIntensity(double x) {
        double targetIntensity = x;

        double proportionForT1 = 0.01;
        double proportionForT50 = 0.5;
        double time = 0;

        double t1 = getTimeForGivenProportion(proportionForT1);
        double t50 = getTimeForGivenProportion(proportionForT50);

        double growthPhaseTime = t1 - t50;
        double deltaTime = growthPhaseTime / 100;

        double intensity = getIntensity(time);

        while (intensity < targetIntensity) {
            time += deltaTime;
            intensity = getIntensity(time);
        }

        double lowerBound = Math.max(0, time - deltaTime);
        double upperBound = time;
        UnivariateFunction function = t -> getIntensity(t) - x;
        UnivariateSolver solver = new BrentSolver(1e-9, 1e-9);
        try {
            return solver.solve(100, function, lowerBound, upperBound);
        } catch (NoBracketingException | TooManyEvaluationsException e) {
            System.err.println("Solver failed: " + e.getMessage());
            return Double.NaN;
        }
    }

    @Override
    public boolean isAnalytical() {
        return false;
    }

    /**
     * Provides a string representation of the GompertzPopulation_t50 model.
     *
     * @return String describing the model parameters.
     */
    @Override
    public String toString() {
        if (useAncestralPopulation) {
            return "Gompertz_t50 Model with NA: t50=" + t50 + ", NInfinity=" + NInfinity +
                    ", b=" + b + ", N0=" + N0 + ", NA=" + NA;
        } else {
            return "Gompertz_t50 Model: t50=" + t50 + ", NInfinity=" + NInfinity +
                    ", b=" + b + ", N0=" + N0;
        }
    }




    public double getTimeForGivenProportion(double k) {
        // Ensure b is not 0 to avoid division by zero
        if (b == 0) {
            throw new IllegalArgumentException("Growth rate b cannot be zero.");
        }

        double ratio;
        double N0_effective;
        double NInfinity_effective;

        if (useAncestralPopulation) {
            // When NA is used
            N0_effective = N0 - NA;
            NInfinity_effective = NInfinity - NA;
        } else {
            // When NA is not used
            N0_effective = N0;
            NInfinity_effective = NInfinity;
        }

        if (N0_effective <= 0 || NInfinity_effective <= 0) {
            throw new IllegalArgumentException("Effective N0 and NInfinity must be positive.");
        }

        ratio = NInfinity_effective / N0_effective;
        double proportion = k * ratio;

        // Ensure proportion is within valid range to avoid taking log of non-positive number
        if (proportion <= 0 || proportion >= ratio) {
            throw new IllegalArgumentException("Proportion must be between 0 and " + ratio);
        }

        // Apply the formula to calculate t*
        double tStar = Math.log(1 - Math.log(proportion) / Math.log(ratio)) / b;
        return tStar;
    }
    public double getT50() {
        return getTimeForGivenProportion(0.5);
    }

    @MethodInfo(description = "Get the initial population size N0", category = GeneratorCategory.COAL_TREE,
            examples = {" gompertzCoalescent_t50.lphy"}
    )
    public double getN0() {
        return this.N0;
    }

}
