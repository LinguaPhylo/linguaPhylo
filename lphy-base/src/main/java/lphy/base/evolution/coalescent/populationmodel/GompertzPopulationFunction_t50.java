package lphy.base.evolution.coalescent.populationmodel;

import lphy.base.evolution.coalescent.PopulationFunction;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

/**
 * A deterministic function that instantiates a {@link GompertzPopulation_t50}
 * parameterized by (t50, b, NInfinity) with an optional ancestral population size (NA)
 * and an indicator I_na (0 or 1). If I_na=1 and NA>0, NA is used. Otherwise, NA is ignored.
 */
public class GompertzPopulationFunction_t50 extends DeterministicFunction<PopulationFunction> {

    public static final String T50_PARAM_NAME       = "t50";
    public static final String B_PARAM_NAME         = "b";
    public static final String NINFINITY_PARAM_NAME = "NInfinity";
    public static final String NA_PARAM_NAME        = "NA";
    public static final String I_NA_PARAM_NAME      = "I_na";

    // Default values if NA or I_na are absent
    private static final double DEFAULT_NA   = 0.0;
    private static final int    DEFAULT_I_NA = 1;

    /**
     * A single constructor accepting t50, b, NInfinity, NA, and I_na.
     * If NA or I_na are not provided or null, they default in {@link #apply()}.
     */
    public GompertzPopulationFunction_t50(
            @ParameterInfo(name = T50_PARAM_NAME, description = "Time when population is half of carrying capacity.")
            Value<Double> t50,
            @ParameterInfo(name = B_PARAM_NAME, description = "Growth rate parameter (>0).")
            Value<Number> b,
            @ParameterInfo(name = NINFINITY_PARAM_NAME, description = "Carrying capacity NInfinity (>0).")
            Value<Number> NInfinity,
            @ParameterInfo(name = NA_PARAM_NAME, description = "Ancestral population size (>=0).", optional = true)
            Value<Number> NA,
            @ParameterInfo(name = I_NA_PARAM_NAME, description = "Indicator (0 or 1). If 1 and NA>0 => use NA.", optional = true)
            Value<Integer> iNa
    ) {
        setParam(T50_PARAM_NAME,       t50);
        setParam(B_PARAM_NAME,         b);
        setParam(NINFINITY_PARAM_NAME, NInfinity);
        setParam(NA_PARAM_NAME,        NA);
        setParam(I_NA_PARAM_NAME,      iNa);
    }

    @GeneratorInfo(
            name          = "gompertzPopFunc_t50",
            narrativeName = "Gompertz (t50) growth function",
            category      = GeneratorCategory.COAL_TREE,
            examples      = {"gomp_t50_jc69.lphy", "gomp_t50_gt16.lphy"},
            description   = "Constructs a GompertzPopulation_t50 model with optional NA and an I_na indicator."
    )
    @Override
    public Value<PopulationFunction> apply() {

        double t50Val       = readDoubleParam(T50_PARAM_NAME,  0.0);
        double bVal         = readDoubleParam(B_PARAM_NAME,    0.0);
        double NInfVal      = readDoubleParam(NINFINITY_PARAM_NAME, 0.0);

        double NAVal   = readDoubleParam(NA_PARAM_NAME,   DEFAULT_NA);
        int iNaVal     = readIntParam(I_NA_PARAM_NAME,    DEFAULT_I_NA);

        if (iNaVal != 0 && iNaVal != 1) {
            throw new IllegalArgumentException("I_na must be 0 or 1.");
        }

        NAVal *= iNaVal;

        PopulationFunction gompertzPop = new GompertzPopulation_t50(
                t50Val, bVal, NInfVal, NAVal, iNaVal
        );
        return new Value<>(gompertzPop, this);
    }


    private double readDoubleParam(String paramName, double defaultVal) {
        Value<?> val = getParams().get(paramName);
        if (val == null || val.value() == null) {
            return defaultVal;
        }
        return ((Number) val.value()).doubleValue();
    }

    private int readIntParam(String paramName, int defaultVal) {
        Value<?> val = getParams().get(paramName);
        if (val == null || val.value() == null) {
            return defaultVal;
        }
        return ((Number) val.value()).intValue();
    }

    public Value<Double>  getT50()       { return getParams().get(T50_PARAM_NAME); }
    public Value<Number>  getB()         { return getParams().get(B_PARAM_NAME); }
    public Value<Number>  getNInfinity() { return getParams().get(NINFINITY_PARAM_NAME); }
    public Value<Number>  getNA()        { return getParams().get(NA_PARAM_NAME); }
    public Value<Integer> getI_na()      { return getParams().get(I_NA_PARAM_NAME); }
}
