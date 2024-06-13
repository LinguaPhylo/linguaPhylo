package lphy.base.evolution.coalescent.populationmodel;

import lphy.base.evolution.coalescent.PopulationFunction;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

import static lphy.base.evolution.coalescent.populationmodel.GompertzPopulation_t50.*;



public class GompertzPopulationFunction_t50 extends DeterministicFunction<PopulationFunction> {
    public GompertzPopulationFunction_t50(@ParameterInfo(name = T50ParamName, description = "Time when population is half of carrying capacity.") Value<Double> t50,
                                         @ParameterInfo(name = BParamName, description = "Initial growth rate of tumor growth.") Value<Number> b,
                                         @ParameterInfo(name = NINFINITYParamName, description = "Limiting population size (carrying capacity).") Value<Number> NInfinity) {
        setParam(T50ParamName, t50);
        setParam(BParamName, b);
        setParam(NINFINITYParamName, NInfinity);

    }

    @GeneratorInfo(name = "gompertzPopFunc_t50", narrativeName = "Gompertz growth function",
            category = GeneratorCategory.COAL_TREE, examples = {" gompertzCoalescent_t50.lphy"},
            description = "Models population growth using the Gompertz growth function.")
    @Override
    public Value<PopulationFunction> apply() {

        double t50 = ((Number) getParams().get(T50ParamName).value()).doubleValue();
        double b = ((Number) getParams().get(BParamName).value()).doubleValue();
        double NInfinity = ((Number) getParams().get(NINFINITYParamName).value()).doubleValue();

        PopulationFunction gompertzPopulation = new GompertzPopulation_t50(t50, b, NInfinity);

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

}
