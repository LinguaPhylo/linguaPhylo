package lphy.base.evolution.birthdeath;

import lphy.base.evolution.tree.AgeConditionedTreeGenerator;

public class BirthDeathConstants {

    public static final String lambdaParamName = "lambda";
    public static final String muParamName = "mu";
    public static final String rhoParamName = "rho";
    public static final String fracParamName = "frac";
    public static final String psiParamName = "psi";
    public static final String samplingProportionParamName = "samplingProportion";
    public static final String diversificationParamName = "diversification";
    public static final String turnoverParamName = "turnover";
    // single source of truth: the age-conditioning vocabulary is owned by AgeConditionedTreeGenerator
    public static final String rootAgeParamName = AgeConditionedTreeGenerator.rootAgeParamName;
    public static final String originAgeParamName = AgeConditionedTreeGenerator.originAgeParamName;
}
