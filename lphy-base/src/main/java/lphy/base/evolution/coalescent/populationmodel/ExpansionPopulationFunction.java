package lphy.base.evolution.coalescent.populationmodel;

import lphy.base.evolution.coalescent.PopulationFunction;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

/**
 * A piecewise expansion model with optional ancestral population NA, controlled by I_na (0 or 1).
 * If I_na=0 => effectively ignore NA in the second segment.
 * If I_na=1 => use NA in the second segment.
 */
public class ExpansionPopulationFunction extends DeterministicFunction<PopulationFunction> {

    public static final String NA_PARAM_NAME = "NA";
    public static final String R_PARAM_NAME  = "r";
    public static final String NC_PARAM_NAME = "NC";
    public static final String X_PARAM_NAME  = "x";
    public static final String I_NA_PARAM_NAME = "I_na"; // The indicator

    // You can define default values if you like:
    private static final double DEFAULT_NA  = 0.0;
    private static final int    DEFAULT_I_NA= 1;

    public ExpansionPopulationFunction(
            @ParameterInfo(name = NA_PARAM_NAME, description = "Ancestral population size.")
            Value<Double> NA,
            @ParameterInfo(name = R_PARAM_NAME,  description = "Exponential decay rate.")
            Value<Double> r,
            @ParameterInfo(name = NC_PARAM_NAME, description = "Population size for [0, x].")
            Value<Double> NC,
            @ParameterInfo(name = X_PARAM_NAME,  description = "Time boundary x.")
            Value<Double> x,
            @ParameterInfo(name = I_NA_PARAM_NAME, description = "Indicator for NA usage (0 or 1).")
            Value<Integer> I_na
    ) {
        setParam(NA_PARAM_NAME, NA);
        setParam(R_PARAM_NAME,  r);
        setParam(NC_PARAM_NAME, NC);
        setParam(X_PARAM_NAME,  x);
        setParam(I_NA_PARAM_NAME, I_na);
    }

    @GeneratorInfo(
            name = "ExpansionPopFunc",
            narrativeName = "Expansion Population Function with optional NA",
            category = GeneratorCategory.COAL_TREE,
            examples = {"expansionCoal.lphy"},
            description = "Models population using a piecewise constant-exponential function with optional NA and I_na."
    )
    @Override
    public Value<PopulationFunction> apply() {
        // 1) Retrieve double parameters
        double NAVal = readDoubleParam(NA_PARAM_NAME, DEFAULT_NA);
        double rVal  = readDoubleParam(R_PARAM_NAME,  0.0);
        double NCVal = readDoubleParam(NC_PARAM_NAME, 0.0);
        double xVal  = readDoubleParam(X_PARAM_NAME,  0.0);

        // 2) Retrieve I_na, default to 1 if missing
        int iNaVal = readIntParam(I_NA_PARAM_NAME, DEFAULT_I_NA);
        if (iNaVal != 0 && iNaVal != 1) {
            throw new IllegalArgumentException("I_na must be 0 or 1.");
        }

        // 3) If iNaVal=0 => ignore NA => effectively NA=0 in the final usage
        //    (similar to ExponentialPopulationFunction approach)
        if (iNaVal == 0) {
            NAVal = 0.0;
        }

        // 4) Construct the population function
        //    We add a constructor that accepts (NC, NA, r, x, I_na)
        //    In that constructor, if I_na=0 => ignore NA, else use it.
        PopulationFunction expansionPop = new ExpansionPopulation(NCVal, NAVal, rVal, xVal, iNaVal);

        return new Value<>(expansionPop, this);
    }

    // Helper methods similar to "ExponentialPopulationFunction" for reading parameters safely
    private double readDoubleParam(String paramName, double defaultVal) {
        Value<?> paramVal = getParams().get(paramName);
        if (paramVal == null || paramVal.value() == null) {
            return defaultVal;
        }
        return ((Number) paramVal.value()).doubleValue();
    }

    private int readIntParam(String paramName, int defaultVal) {
        Value<?> paramVal = getParams().get(paramName);
        if (paramVal == null || paramVal.value() == null) {
            return defaultVal;
        }
        return ((Number) paramVal.value()).intValue();
    }

    // Optionally keep getters if needed by LPhy or other code:
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
        return (Value<Double>) getParams().get(X_PARAM_NAME);
    }
    public Value<Integer> getI_na() {
        return (Value<Integer>) getParams().get(I_NA_PARAM_NAME);
    }
}
