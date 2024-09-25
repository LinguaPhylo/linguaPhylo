package lphy.base.evolution.coalescent.populationmodel;

import lphy.base.evolution.coalescent.PopulationFunction;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

public class LogisticPopulationFunction extends DeterministicFunction<PopulationFunction> {

    public static final String T50_PARAM_NAME = "t50";
    public static final String N_CARRYING_CAPACITY_PARAM_NAME = "nCarryingCapacity";
    public static final String B_PARAM_NAME = "b";

    public LogisticPopulationFunction(@ParameterInfo(name = T50_PARAM_NAME, description = "The midpoint of the logistic function.") Value<Double> t50,
                               @ParameterInfo(name = N_CARRYING_CAPACITY_PARAM_NAME, description = "The carrying capacity or the maximum population size.") Value<Number> nCarryingCapacity,
                               @ParameterInfo(name = B_PARAM_NAME, description = "The logistic growth rate.") Value<Double> b) {
        setParam(T50_PARAM_NAME, t50);
        setParam(N_CARRYING_CAPACITY_PARAM_NAME, nCarryingCapacity);
        setParam(B_PARAM_NAME, b);
    }

    @GeneratorInfo(name="logisticPopFunc", narrativeName = "Logistic growth function",
            category = GeneratorCategory.COAL_TREE, examples = {" logisticCoalescent.lphy, logisticCoalJC.lphy" },
            description = "Models population growth using the logistic growth function.")
    @Override
    public Value<PopulationFunction> apply() {
        double t50 = ((Number) getParams().get(T50_PARAM_NAME).value()).doubleValue();
        double nCarryingCapacity = ((Number) getParams().get(N_CARRYING_CAPACITY_PARAM_NAME).value()).doubleValue();
        double b = ((Number) getParams().get(B_PARAM_NAME).value()).doubleValue();

        PopulationFunction logisticPopulation = new LogisticPopulation(t50, nCarryingCapacity, b);

        return new Value<>(logisticPopulation, this);
    }
    public Value<Double> getT50() {
        return (Value<Double>) getParams().get(T50_PARAM_NAME);
    }


    public Value<Double> getNCarryingCapacity() {
        return (Value<Double>) getParams().get(N_CARRYING_CAPACITY_PARAM_NAME);
    }


    public Value<Double> getB() {
        return (Value<Double>) getParams().get(B_PARAM_NAME);
    }
}

