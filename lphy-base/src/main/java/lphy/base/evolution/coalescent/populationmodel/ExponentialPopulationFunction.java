package lphy.base.evolution.coalescent.populationmodel;

import lphy.base.evolution.coalescent.PopulationFunction;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

public class ExponentialPopulationFunction extends DeterministicFunction<PopulationFunction> {

    public static final String GROWTH_RATE_PARAM_NAME = "GrowthRate";
    public static final String N0_PARAM_NAME = "N0";
    public static final String NA_PARAM_NAME = "NA";
    public static final String I_NA_PARAM_NAME = "I_na";

    private static final double DEFAULT_NA = 0.0;
    private static final int DEFAULT_I_NA = 1;

    /**
     * A single constructor. All parameters are optional except GrowthRate & N0.
     * The rest (NA, I_na) can be null, and are handled in apply().
     */
    public ExponentialPopulationFunction(
            @ParameterInfo(name = GROWTH_RATE_PARAM_NAME, description = "Exponential growth rate") Value<Double> GrowthRate,
            @ParameterInfo(name = N0_PARAM_NAME, description = "Initial population size") Value<Double> N0,
            @ParameterInfo(name = NA_PARAM_NAME, description = "Ancestral population size") Value<Double> NA,
            @ParameterInfo(name = I_NA_PARAM_NAME, description = "Indicator for using NA (0 or 1)") Value<Integer> I_na
    ) {
        setParam(GROWTH_RATE_PARAM_NAME, GrowthRate);
        setParam(N0_PARAM_NAME, N0);
        setParam(NA_PARAM_NAME, NA);
        setParam(I_NA_PARAM_NAME, I_na);
    }

    @GeneratorInfo(
            name="exponentialPopFunc",
            narrativeName = "Exponential growth function",
            category = GeneratorCategory.COAL_TREE,
            examples = {"expCoal.lphy", "expCoalJC.lphy"},
            description = "Models population growth using an exponential growth function with optional NA and I_na."
    )
    @Override
    public Value<PopulationFunction> apply() {
        // Retrieve core parameters
        double r = readDoubleParam(GROWTH_RATE_PARAM_NAME, 0.0);
        double N0 = readDoubleParam(N0_PARAM_NAME, 0.0);

        // If NA or I_na are absent, default them
        double NA = readDoubleParam(NA_PARAM_NAME, DEFAULT_NA);
        int I_na = readIntParam(I_NA_PARAM_NAME, DEFAULT_I_NA);

        if (I_na != 0 && I_na != 1) {
            throw new IllegalArgumentException("I_na must be 0 or 1.");
        }

        // If I_na=0, then NA is effectively 0
        NA *= I_na;

        PopulationFunction exponentialPopulation = new ExponentialPopulation(r, N0, NA, I_na);
        return new Value<>(exponentialPopulation, this);
    }

    /**
     * Helper to read a double parameter from getParams(), fallback to defaultVal if not present or null.
     */
    private double readDoubleParam(String paramName, double defaultVal) {
        Value<?> val = getParams().get(paramName);
        if(val == null || val.value() == null) {
            return defaultVal;
        }
        return ((Number) val.value()).doubleValue();
    }

    /**
     * Helper to read an int parameter from getParams(), fallback to defaultVal if not present or null.
     */
    private int readIntParam(String paramName, int defaultVal) {
        Value<?> val = getParams().get(paramName);
        if(val == null || val.value() == null) {
            return defaultVal;
        }
        return ((Number) val.value()).intValue();
    }

    // Optionally keep these getters if the framework expects them:
    public Value<Double> getGrowthRate() {
        return (Value<Double>) getParams().get(GROWTH_RATE_PARAM_NAME);
    }

    public Value<Double> getN0() {
        return (Value<Double>) getParams().get(N0_PARAM_NAME);
    }

    public Value<Double> getNA() {
        return (Value<Double>) getParams().get(NA_PARAM_NAME);
    }

    public Value<Integer> getI_na() {
        return (Value<Integer>) getParams().get(I_NA_PARAM_NAME);
    }
}
