//package lphy.base.evolution.coalescent.populationmodel;
//
//import lphy.base.evolution.coalescent.PopulationFunction;
//import lphy.core.model.annotation.GeneratorCategory;
//import lphy.core.model.annotation.MethodInfo;
//
//public class SVS implements PopulationFunction {
//
//    private PopulationFunction[] models;
//    private int indicator;
//    private SVSPopulationFunction selectedModel;
//
//    public SVS(int indicator, SVSPopulationFunction selectedModel) {
//        this.indicator = indicator;
//        this.selectedModel = selectedModel;
//    }
//
//    @MethodInfo(description = "Get the population size at time t", category = GeneratorCategory.COAL_TREE,
//            examples = {" SVS.lphy"})
//    @Override
//    public double getTheta(double t) {
//        return selectedModel.getTheta(t);
//    }
//
//    @Override
//    public double getIntensity(double t) {
//        return selectedModel.getIntensity(t);
//    }
//
//    @Override
//    public double getInverseIntensity(double x) {
//        return selectedModel.getInverseIntensity(x);
//    }
//
//    @Override
//    public boolean isAnalytical() {
//        return selectedModel.isAnalytical();
//    }
//
//
//    public SVSPopulationFunction getSelectedModel() {
//        return selectedModel;
//    }
//}
