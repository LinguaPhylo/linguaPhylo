package lphy.base.evolution.coalescent.populationmodel;

import lphy.base.evolution.coalescent.PopulationFunction;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

public class ExpansionPopulationFunction extends DeterministicFunction<PopulationFunction> {

    public static final String N0_PARAM_NAME = "N0";
    public static final String TAU_PARAM_NAME = "tau";
    public static final String R_PARAM_NAME = "r";
    public static final String NC_PARAM_NAME = "NC";

    public ExpansionPopulationFunction(
            @ParameterInfo(name = N0_PARAM_NAME, description = "Initial population size before tau.")
            Value<Double> N0,
            @ParameterInfo(name = TAU_PARAM_NAME, description = "Time before which population size is constant at N0.")
            Value<Double> tau,
            @ParameterInfo(name = R_PARAM_NAME, description = "The exponential growth rate.")
            Value<Double> r,
            @ParameterInfo(name = NC_PARAM_NAME, description = "Current population size after time x.")
            Value<Double> NC) {

        setParam(N0_PARAM_NAME, N0);
        setParam(TAU_PARAM_NAME, tau);
        setParam(R_PARAM_NAME, r);
        setParam(NC_PARAM_NAME, NC);
    }

    @GeneratorInfo(
            name = "expansionPopFunc",
            narrativeName = "Piecewise Exponential Growth Function",
            category = GeneratorCategory.COAL_TREE,
            examples = {"expansionCoal.lphy"},
            description = "Models population growth using a piecewise exponential growth function."
    )
    @Override
    public Value<PopulationFunction> apply() {
        Value<Double> N0Value = (Value<Double>) getParams().get(N0_PARAM_NAME);
        Value<Double> tauValue = (Value<Double>) getParams().get(TAU_PARAM_NAME);
        Value<Double> rValue = (Value<Double>) getParams().get(R_PARAM_NAME);
        Value<Double> NCValue = (Value<Double>) getParams().get(NC_PARAM_NAME);

        double N0 = N0Value.value();
        double tau = tauValue.value();
        double r = rValue.value();
        double NC = NCValue.value();

        PopulationFunction expansionPopulation = new ExpansionPopulation(N0, tau, r, NC);

        return new Value<>(expansionPopulation, this);
    }

    public Value<Double> getN0() {
        return (Value<Double>) getParams().get(N0_PARAM_NAME);
    }

    public Value<Double> getTau() {
        return (Value<Double>) getParams().get(TAU_PARAM_NAME);
    }

    public Value<Double> getR() {
        return (Value<Double>) getParams().get(R_PARAM_NAME);
    }

    public Value<Double> getNC() {
        return (Value<Double>) getParams().get(NC_PARAM_NAME);
    }
}
