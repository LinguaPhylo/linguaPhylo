package lphy.base.evolution.coalescent.populationmodel;

import lphy.base.evolution.coalescent.PopulationFunction;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

/**
 * A deterministic function that generates a {@link LogisticPopulation} based on user-specified parameters:
 * <ul>
 *     <li><strong>t50</strong> (required): The midpoint (inflection point) of the logistic function.</li>
 *     <li><strong>nCarryingCapacity</strong> (required): The carrying capacity (K).</li>
 *     <li><strong>b</strong> (required): The logistic growth rate.</li>
 *     <li><strong>NA</strong> (optional): The ancestral population size. Only used if I_na=1 and NA>0.</li>
 *     <li><strong>I_na</strong> (optional): Indicator (0 or 1) for whether NA is used.</li>
 * </ul>
 *
 * <p>If I_na=0 (or NA <= 0), the model ignores NA. If I_na=1 and NA>0, the logistic model uses NA
 * as a lower bound population size. This exactly mirrors the approach in ExponentialPopulationFunction.</p>
 */
public class LogisticPopulationFunction extends DeterministicFunction<PopulationFunction> {

    // -------------------------------
    // Parameter names
    // -------------------------------
    public static final String T50_PARAM_NAME = "t50";
    public static final String N_CARRYING_CAPACITY_PARAM_NAME = "nCarryingCapacity";
    public static final String B_PARAM_NAME = "b";
    public static final String NA_PARAM_NAME = "NA";
    public static final String I_NA_PARAM_NAME = "I_na";

    // -------------------------------
    // Default values (mirroring ExponentialPopulationFunction style)
    // -------------------------------
    private static final double DEFAULT_NA = 0.0;
    private static final int DEFAULT_I_NA = 1;

    /**
     * A single constructor. t50, nCarryingCapacity, and b are required;
     * NA and I_na are optional and handled in {@link #apply()}.
     */
    public LogisticPopulationFunction(
            @ParameterInfo(name = T50_PARAM_NAME, description = "The logistic midpoint (inflection point).")
            Value<Double> t50,
            @ParameterInfo(name = N_CARRYING_CAPACITY_PARAM_NAME, description = "Carrying capacity (K).")
            Value<Double> nCarryingCapacity,
            @ParameterInfo(name = B_PARAM_NAME, description = "Logistic growth rate.")
            Value<Double> b,
            @ParameterInfo(name = NA_PARAM_NAME, description = "Ancestral population size (optional).")
            Value<Double> NA,
            @ParameterInfo(name = I_NA_PARAM_NAME, description = "Indicator for using NA (0 or 1).")
            Value<Integer> I_na
    ) {
        setParam(T50_PARAM_NAME, t50);
        setParam(N_CARRYING_CAPACITY_PARAM_NAME, nCarryingCapacity);
        setParam(B_PARAM_NAME, b);
        setParam(NA_PARAM_NAME, NA);
        setParam(I_NA_PARAM_NAME, I_na);
    }

    /**
     * Creates a {@link LogisticPopulation} instance from the given parameters,
     * mirroring the logic in {@code ExponentialPopulationFunction.apply()}.
     *
     * <p>If I_na=0, NA is effectively set to 0. Otherwise, if NA is provided and > 0,
     * the model uses NA as a lower bound. If not, NA remains 0.</p>
     */
    @GeneratorInfo(
            name = "logisticPopFunc",
            narrativeName = "Logistic growth function",
            category = GeneratorCategory.COAL_TREE,
            examples = {"logisticCoalescent.lphy", "logisticCoalJC.lphy"},
            description = "Models population growth using a logistic growth function, " +
                    "with optional ancestral population size (NA) and indicator (I_na)."
    )
    @Override
    public Value<PopulationFunction> apply() {
        // 1) Read required doubles: t50, nCarryingCapacity, b
        double t50Val = readDoubleParam(T50_PARAM_NAME, 0.0);
        double KVal = readDoubleParam(N_CARRYING_CAPACITY_PARAM_NAME, 0.0);
        double bVal = readDoubleParam(B_PARAM_NAME, 0.0);

        // 2) Read optional NA and I_na, with defaults
        double NAVal = readDoubleParam(NA_PARAM_NAME, DEFAULT_NA);
        int iNaVal = readIntParam(I_NA_PARAM_NAME, DEFAULT_I_NA);

        // 3) Validate iNaVal must be 0 or 1
        if (iNaVal != 0 && iNaVal != 1) {
            throw new IllegalArgumentException("I_na must be 0 or 1.");
        }

        // 4) If I_na=0, ignore NA => set NA=0
        NAVal *= iNaVal;

        // 5) Construct the logistic population
        //    You need a corresponding constructor:
        //       LogisticPopulation(double t50, double K, double b, double NA, int iNa)
        PopulationFunction logisticPop = new LogisticPopulation(t50Val, KVal, bVal, NAVal, iNaVal);

        // 6) Wrap the result
        return new Value<>(logisticPop, this);
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



    public Value<Double> getT50() {
        return (Value<Double>) getParams().get(T50_PARAM_NAME);
    }

    public Value<Double> getNCarryingCapacity() {
        return (Value<Double>) getParams().get(N_CARRYING_CAPACITY_PARAM_NAME);
    }

    public Value<Double> getB() {
        return (Value<Double>) getParams().get(B_PARAM_NAME);
    }

    public Value<Double> getNA() {
        return (Value<Double>) getParams().get(NA_PARAM_NAME);
    }

    public Value<Integer> getI_na() {
        return (Value<Integer>) getParams().get(I_NA_PARAM_NAME);
    }
}
