package lphy.base.evolution.coalescent.populationmodel;

import lphy.base.evolution.coalescent.PopulationFunction;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

public class ConstantPopulationFunction extends DeterministicFunction<PopulationFunction> {

    public static final String N0_PARAM_NAME = "N0";

    public ConstantPopulationFunction(@ParameterInfo(name = N0_PARAM_NAME, description = "The constant population size.") Value<Double> N0) {
        setParam(N0_PARAM_NAME, N0);
    }

    @GeneratorInfo(name="constantPopFunc", narrativeName = "Constant population function",
            category = GeneratorCategory.COAL_TREE, examples = {" constantCoalescent.lphy" },
            description = "Models population using a constant population size.")
    @Override
    public Value<PopulationFunction> apply() {
        double N0 = ((Number) getParams().get(N0_PARAM_NAME).value()).doubleValue();

        PopulationFunction constantPopulation = new ConstantPopulation(N0);

        return new Value<>(constantPopulation, this);
    }


    public Value<Double> getN0() {
        return (Value<Double>) getParams().get(N0_PARAM_NAME);
    }
}
