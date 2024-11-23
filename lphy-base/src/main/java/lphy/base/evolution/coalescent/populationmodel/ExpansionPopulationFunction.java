package lphy.base.evolution.coalescent.populationmodel;

import lphy.base.evolution.coalescent.PopulationFunction;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

public class ExpansionPopulationFunction extends DeterministicFunction<PopulationFunction> {

    public static final String NA_PARAM_NAME = "NA";
    public static final String R_PARAM_NAME = "r";
    public static final String NC_PARAM_NAME = "NC";
    public static final String X_PARAM_NAME = "x"; // New independent parameter for time x

    public ExpansionPopulationFunction(
            @ParameterInfo(name = NA_PARAM_NAME, description = "Ancestral population size after decay.")
            Value<Double> NA,
            @ParameterInfo(name = R_PARAM_NAME, description = "The exponential growth rate.")
            Value<Double> r,
            @ParameterInfo(name = NC_PARAM_NAME, description = "Current population size after time x.")
            Value<Double> NC,
            @ParameterInfo(name = X_PARAM_NAME, description = "Time at which the population reaches NC.")
            Value<Double> x) {

        setParam(NA_PARAM_NAME, NA);
        setParam(R_PARAM_NAME, r);
        setParam(NC_PARAM_NAME, NC);
        setParam(X_PARAM_NAME, x);
    }

    @GeneratorInfo(
            name = "ExpansionPopFunc",
            narrativeName = "Expansion Population Function",
            category = GeneratorCategory.COAL_TREE,
            examples = {"expansionCoal.lphy"},
            description = "Models population using a constant-exponential model with ancestral population size NA."
    )
    @Override
    public Value<PopulationFunction> apply() {
        Value<Double> NAValue = (Value<Double>) getParams().get(NA_PARAM_NAME);
        Value<Double> rValue = (Value<Double>) getParams().get(R_PARAM_NAME);
        Value<Double> NCValue = (Value<Double>) getParams().get(NC_PARAM_NAME);
        Value<Double> xValue = (Value<Double>) getParams().get(X_PARAM_NAME); // Use x parameter

        double NA = NAValue.value();
        double r = rValue.value();
        double NC = NCValue.value();
        double x = xValue.value();

        PopulationFunction expansionPopulation = new ExpansionPopulation(NC, NA, r, x);


        return new Value<>(expansionPopulation, this);
    }

    public Value<Double> getNA() {
        return (Value<Double>) getParams().get(NA_PARAM_NAME);
    }

    public Value<Double> getR() {
        return (Value<Double>) getParams().get(R_PARAM_NAME);
    }

    public Value<Double> getNC() {
        return (Value<Double>) getParams().get(NC_PARAM_NAME);
    }

    public Value<Double> getX() {
        return (Value<Double>) getParams().get(X_PARAM_NAME); // Getter for x parameter
    }
}
