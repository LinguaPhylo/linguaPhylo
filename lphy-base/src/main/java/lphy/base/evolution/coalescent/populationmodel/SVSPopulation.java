package lphy.base.evolution.coalescent.populationmodel;

import lphy.base.evolution.coalescent.PopulationFunction;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;

import static lphy.base.evolution.coalescent.populationmodel.SVSPopulationFunction.INDICATOR_PARAM_NAME;
import static lphy.base.evolution.coalescent.populationmodel.SVSPopulationFunction.MODELS_PARAM_NAME;
public class SVSPopulation extends DeterministicFunction<PopulationFunction> implements PopulationFunction {

    public PopulationFunction model;

    public SVSPopulation(PopulationFunction f) {
        this.model = f;
    }

    @Override
    public double getTheta(double t) {
        return model.getTheta(t);
    }

    @Override
    public double getIntensity(double t) {
        return model.getIntensity(t);
    }

    @Override
    public double getInverseIntensity(double x) {
        return model.getInverseIntensity(x);
    }

    @Override
    public boolean isAnalytical() {
        return model.isAnalytical();
    }

    public Value getIndicator() {
        return (Value<Integer>) getParams().get(INDICATOR_PARAM_NAME);
    }

    public Value<PopulationFunction[]> getModels() {
        return (Value<PopulationFunction[]>) getParams().get(MODELS_PARAM_NAME);
    }

    @Override
    public Value<PopulationFunction> apply() {
        return new Value<>(null, model);
    }



    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SVSPopulationFunction using model: ").append(model.toString());

        Value<Integer> indicator = getIndicator();
        Value<PopulationFunction[]> models = getModels();

        if (indicator != null && models != null) {
            sb.append(" with indicator ").append(indicator.value()).append(" and models: ");
            for (PopulationFunction pf : models.value()) {
                sb.append(pf.toString()).append(" ");
            }
        }

        return sb.toString();
    }


}