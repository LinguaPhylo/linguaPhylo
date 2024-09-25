package lphy.base.evolution.coalescent.populationmodel;

import lphy.base.evolution.coalescent.PopulationFunction;

public class SVSPopulation implements PopulationFunction {

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


}