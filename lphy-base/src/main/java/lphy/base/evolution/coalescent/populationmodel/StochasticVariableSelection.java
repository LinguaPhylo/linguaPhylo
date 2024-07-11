package lphy.base.evolution.coalescent.populationmodel;

import lphy.base.evolution.coalescent.PopulationFunction;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

public class StochasticVariableSelection extends DeterministicFunction<PopulationFunction> {

    public static final String INDICATOR_PARAM_NAME = "indicator";
    public static final String MODELS_PARAM_NAME = "models";

    public StochasticVariableSelection(
            @ParameterInfo(name = INDICATOR_PARAM_NAME, description = "The indicator for the population model.") Value<Integer> indicator,
            @ParameterInfo(name = MODELS_PARAM_NAME, description = "The array of population models.") Value<PopulationFunction[]> models) {
        setParam(INDICATOR_PARAM_NAME, indicator);
        setParam(MODELS_PARAM_NAME, models);
    }

    @GeneratorInfo(name="stochasticVariableSelection", narrativeName = "Stochastic Variable Selection",
            category = GeneratorCategory.COAL_TREE, examples = {" stochasticVariableSelection.lphy" },
            description = "Models population using different growth models based on the indicator value.")
    @Override
    public Value<PopulationFunction> apply() {
        int indicator = ((Number) getParams().get(INDICATOR_PARAM_NAME).value()).intValue();
        Object[] models = (Object[]) getParams().get(MODELS_PARAM_NAME).value();

        if (indicator < 0 || indicator >= models.length) {
            throw new IllegalArgumentException("Invalid modelIndex value");
        }

        Object selectedModel = models[indicator];

        if (selectedModel instanceof PopulationFunction populationFunction)
            return new Value<>(populationFunction, this);
        else
            throw new IllegalArgumentException("to do");
    }

    public Value<Integer> getIndicator() {
        return (Value<Integer>) getParams().get(INDICATOR_PARAM_NAME);
    }

    public Value<PopulationFunction[]> getModels() {
        return (Value<PopulationFunction[]>) getParams().get(MODELS_PARAM_NAME);
    }
}