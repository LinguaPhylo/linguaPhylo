package lphy.base.evolution.coalescent.populationmodel;

import lphy.base.evolution.coalescent.PopulationFunction;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

import static lphy.base.evolution.coalescent.populationmodel.GompertzPopulation_t50.*;

public class GompertzPopulationFunction_t50 extends DeterministicFunction<PopulationFunction> {

    public GompertzPopulationFunction_t50(
            @ParameterInfo(name = T50ParamName, description = "Time when population is half of carrying capacity.") Value<Double> t50,
            @ParameterInfo(name = BParamName, description = "Growth rate parameter.") Value<Number> b,
            @ParameterInfo(name = NINFINITYParamName, description = "Carrying capacity NInfinity.") Value<Number> NInfinity,
            @ParameterInfo(name = NAParamName, description = "Ancestral population size NA.", optional = true) Value<Number> NA) {
        setParam(T50ParamName, t50);
        setParam(BParamName, b);
        setParam(NINFINITYParamName, NInfinity);
        setParam(NAParamName, NA);
    }

    @GeneratorInfo(name = "gompertzPopFunc_t50", narrativeName = "Gompertz_t50 growth function",
            category = GeneratorCategory.COAL_TREE, examples = {"gomp_t50_jc69.lphy", "gomp_t50_gt16.lphy"},
            description = "Models population growth using the Gompertz growth function with optional ancestral population size NA.")
    @Override
    public Value<PopulationFunction> apply() {

        double t50 = ((Number) getParams().get(T50ParamName).value()).doubleValue();
        double b = ((Number) getParams().get(BParamName).value()).doubleValue();
        double NInfinity = ((Number) getParams().get(NINFINITYParamName).value()).doubleValue();

        Value<Number> NAValue = getParams().get(NAParamName);
        PopulationFunction gompertzPopulation;

        if (NAValue != null && NAValue.value() != null) {
            double NA = ((Number) NAValue.value()).doubleValue();
            gompertzPopulation = new GompertzPopulation_t50(t50, b, NInfinity, NA);
        } else {
            gompertzPopulation = new GompertzPopulation_t50(t50, b, NInfinity);
        }

        return new Value<>(gompertzPopulation, this);
    }

    public Value<Double> getT50() {
        return getParams().get(T50ParamName);
    }

    public Value<Number> getB() {
        return getParams().get(BParamName);
    }

    public Value<Number> getNInfinity() {
        return getParams().get(NINFINITYParamName);
    }

    public Value<Number> getNA() {
        return getParams().get(NAParamName);
    }

}
