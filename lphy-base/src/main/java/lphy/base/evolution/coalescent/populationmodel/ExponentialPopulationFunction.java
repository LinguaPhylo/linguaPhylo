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

    /**
     * Constructor for ExponentialPopulationFunction without Ancestral Population (NA).
     *
     * @param GrowthRate Growth rate of the population.
     * @param N0         Initial population size at time t=0.
     */
    public ExponentialPopulationFunction(
            @ParameterInfo(name = GROWTH_RATE_PARAM_NAME, description = "The growth rate of the population.") Value<Double> GrowthRate,
            @ParameterInfo(name = N0_PARAM_NAME, description = "The initial population size.") Value<Double> N0) {
        setParam(GROWTH_RATE_PARAM_NAME, GrowthRate);
        setParam(N0_PARAM_NAME, N0);
    }

    /**
     * Constructor for ExponentialPopulationFunction with Ancestral Population (NA).
     *
     * @param GrowthRate Growth rate of the population.
     * @param N0         Initial population size at time t=0.
     * @param NA         Ancestral population size.
     */
    public ExponentialPopulationFunction(
            @ParameterInfo(name = GROWTH_RATE_PARAM_NAME, description = "The growth rate of the population.") Value<Double> GrowthRate,
            @ParameterInfo(name = N0_PARAM_NAME, description = "The initial population size.") Value<Double> N0,
            @ParameterInfo(name = NA_PARAM_NAME, description = "The ancestral population size.") Value<Double> NA) {
        setParam(GROWTH_RATE_PARAM_NAME, GrowthRate);
        setParam(N0_PARAM_NAME, N0);
        setParam(NA_PARAM_NAME, NA);
    }

    @GeneratorInfo(name="exponentialPopFunc", narrativeName = "Exponential growth function",
            category = GeneratorCategory.COAL_TREE, examples = {" expCoal.lphy, expCoalJC.lphy" },
            description = "Models population growth using an exponential growth function.")
    @Override
    public Value<PopulationFunction> apply() {
        double GrowthRate = ((Number) getParams().get(GROWTH_RATE_PARAM_NAME).value()).doubleValue();
        double N0 = ((Number) getParams().get(N0_PARAM_NAME).value()).doubleValue();

        // Check if NA parameter is provided
        Value<Double> naValue = getParams().get(NA_PARAM_NAME);
        PopulationFunction exponentialPopulation;

        if (naValue != null && naValue.value() != null && naValue.value() > 0.0) {
            double NA = naValue.value().doubleValue();
            // Create ExponentialPopulation with NA
            exponentialPopulation = new ExponentialPopulation(GrowthRate, N0, NA);
        } else {
            // Create ExponentialPopulation without NA
            exponentialPopulation = new ExponentialPopulation(GrowthRate, N0);
        }

        return new Value<>(exponentialPopulation, this);
    }

    public Value<Double> getGrowthRate() {
        return (Value<Double>) getParams().get(GROWTH_RATE_PARAM_NAME);
    }


    public Value<Double> getN0() {
        return (Value<Double>) getParams().get(N0_PARAM_NAME);
    }

    public Value<Double> getNA() {
        return (Value<Double>) getParams().get(NA_PARAM_NAME);
    }


}
