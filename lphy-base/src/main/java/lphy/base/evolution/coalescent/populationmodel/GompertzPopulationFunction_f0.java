package lphy.base.evolution.coalescent.populationmodel;

import lphy.base.evolution.coalescent.PopulationFunction;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

/**
 * A deterministic function that generates a {@link GompertzPopulation_f0} model
 * using parameters b, N0, f0, NA (optional), and I_na (optional).
 * <ul>
 *   <li><strong>b</strong>: Growth rate parameter, > 0.</li>
 *   <li><strong>N0</strong>: Initial population size, > 0.</li>
 *   <li><strong>f0</strong>: Initial proportion (N0 / NInfinity), > 0.</li>
 *   <li><strong>NA</strong>: Ancestral population size (>=0). Used if I_na=1 and NA>0.</li>
 *   <li><strong>I_na</strong>: 0 or 1; if 0 => ignore NA, if 1 => use NA if > 0.</li>
 * </ul>
 */
public class GompertzPopulationFunction_f0 extends DeterministicFunction<PopulationFunction> {


    public static final String B_PARAM_NAME    = "b";
    public static final String N0_PARAM_NAME   = "N0";
    public static final String F0_PARAM_NAME   = "f0";
    public static final String NA_PARAM_NAME   = "NA";
    public static final String I_NA_PARAM_NAME = "I_na";

    // Default values for NA and I_na if absent
    private static final double DEFAULT_NA   = 0.0;
    private static final int    DEFAULT_I_NA = 1;

    /**
     * A single constructor. b, N0, and f0 are essential.
     * NA and I_na can be null and will be handled in {@link #apply()}.
     */
    public GompertzPopulationFunction_f0(
            @ParameterInfo(name = B_PARAM_NAME, description = "Gompertz growth rate (b).")
            Value<Double> b,
            @ParameterInfo(name = N0_PARAM_NAME, description = "Initial population size (N0).")
            Value<Double> N0,
            @ParameterInfo(name = F0_PARAM_NAME, description = "Initial proportion, f0 = N0 / NInfinity.")
            Value<Double> f0,
            @ParameterInfo(name = NA_PARAM_NAME, description = "Ancestral population size (NA).")
            Value<Double> NA,
            @ParameterInfo(name = I_NA_PARAM_NAME, description = "Indicator (0 or 1) controlling usage of NA.")
            Value<Integer> I_na
    ) {
        setParam(B_PARAM_NAME,     b);
        setParam(N0_PARAM_NAME,    N0);
        setParam(F0_PARAM_NAME,    f0);
        setParam(NA_PARAM_NAME,    NA);
        setParam(I_NA_PARAM_NAME,  I_na);
    }

    /**
     * Constructs a {@link GompertzPopulation_f0} by reading the specified parameters.
     * If I_na=0, NA is forced to 0; if I_na=1 and NA>0, the ancestral size is incorporated.
     *
     * @return A {@link Value} wrapping the new {@link GompertzPopulation_f0}.
     */
    @GeneratorInfo(
            name          = "gompertzPopFunc_f0",
            narrativeName = "Gompertz (f0) growth function",
            category      = GeneratorCategory.COAL_TREE,
            examples      = {"gompertzF0Coal.lphy"},
            description   = "Constructs a Gompertz population model (f0-parameterized) with optional NA and indicator I_na."
    )
    @Override
    public Value<PopulationFunction> apply() {

        double bVal   = readDoubleParam(B_PARAM_NAME, 0.0);
        double N0Val  = readDoubleParam(N0_PARAM_NAME, 0.0);
        double f0Val  = readDoubleParam(F0_PARAM_NAME, 0.0);

        double NAVal   = readDoubleParam(NA_PARAM_NAME, DEFAULT_NA);
        int    iNaVal  = readIntParam(I_NA_PARAM_NAME, DEFAULT_I_NA);


        if (iNaVal != 0 && iNaVal != 1) {
            throw new IllegalArgumentException("I_na must be 0 or 1.");
        }

        NAVal *= iNaVal;


        PopulationFunction gompertzPop = new GompertzPopulation_f0(N0Val, f0Val, bVal, NAVal, iNaVal);


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


    public Value<Double> getB()    { return (Value<Double>) getParams().get(B_PARAM_NAME); }
    public Value<Double> getN0()   { return (Value<Double>) getParams().get(N0_PARAM_NAME); }
    public Value<Double> getF0()   { return (Value<Double>) getParams().get(F0_PARAM_NAME); }
    public Value<Double> getNA()   { return (Value<Double>) getParams().get(NA_PARAM_NAME); }
    public Value<Integer> getI_na(){ return (Value<Integer>) getParams().get(I_NA_PARAM_NAME); }
}
