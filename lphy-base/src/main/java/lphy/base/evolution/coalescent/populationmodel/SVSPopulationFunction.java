package lphy.base.evolution.coalescent.populationmodel;

import lphy.base.evolution.coalescent.PopulationFunction;

public class SVSPopulationFunction implements PopulationFunction {

    private PopulationFunction model;

    public SVSPopulationFunction(PopulationFunction f) {
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

}
