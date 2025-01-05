package lphy.base.evolution.coalescent.populationmodel;

import lphy.base.evolution.coalescent.PopulationFunction;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

public class Cons_Exp_ConsPopulationFunction extends DeterministicFunction<PopulationFunction> {

    public static final String TAU_PARAM_NAME = "tau";
    public static final String R_PARAM_NAME = "r";
    public static final String NC_PARAM_NAME = "NC";
    public static final String X_PARAM_NAME = "x"; // New independent parameter for time x

    public Cons_Exp_ConsPopulationFunction(
            @ParameterInfo(name = TAU_PARAM_NAME, description = "Time before which population size is constant.")
            Value<Double> tau,
            @ParameterInfo(name = R_PARAM_NAME, description = "The exponential growth rate.")
            Value<Double> r,
            @ParameterInfo(name = NC_PARAM_NAME, description = "Current population size after time x.")
            Value<Double> NC,
            @ParameterInfo(name = X_PARAM_NAME, description = "Time at which the population reaches NC.")
            Value<Double> x) {

        setParam(TAU_PARAM_NAME, tau);
        setParam(R_PARAM_NAME, r);
        setParam(NC_PARAM_NAME, NC);
        setParam(X_PARAM_NAME, x);
    }

    @GeneratorInfo(
            name = "Cons_Exp_ConsPopFunc",
            narrativeName = "Piecewise Exponential Growth Function",
            category = GeneratorCategory.COAL_TREE,
            examples = {"Cons_Exp_ConsCoal.lphy"},
            description = "Models population growth using a piecewise exponential growth function with x as an independent parameter."
    )
    @Override
    public Value<PopulationFunction> apply() {
        Value<Double> tauValue = (Value<Double>) getParams().get(TAU_PARAM_NAME);
        Value<Double> rValue = (Value<Double>) getParams().get(R_PARAM_NAME);
        Value<Double> NCValue = (Value<Double>) getParams().get(NC_PARAM_NAME);
        Value<Double> xValue = (Value<Double>) getParams().get(X_PARAM_NAME); // Use x parameter

        double tau = tauValue.value();
        double r = rValue.value();
        double NC = NCValue.value();
        double x = xValue.value();

        // Create the population function using tau, r, NC, and x
        PopulationFunction Cons_Exp_ConsPopulation = new Cons_Exp_ConsPopulation(tau, r, NC, x);

        return new Value<>(Cons_Exp_ConsPopulation, this);
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

    public Value<Double> getX() {
        return (Value<Double>) getParams().get(X_PARAM_NAME); // Getter for x parameter
    }
}