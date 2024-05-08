
package lphy.base.evolution.coalescent.populationmodel;

import lphy.base.evolution.coalescent.PopulationFunction;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

import static lphy.base.evolution.coalescent.populationmodel.GompertzPopulation.*;

public class GompertzPopulationFunction extends DeterministicFunction<PopulationFunction> {
    public GompertzPopulationFunction(//@ParameterInfo(name = T50ParamName, description = "Time when population is half of carrying capacity.") Value<Double> t50,
                                      @ParameterInfo(name = F0ParamName, description = "Time when population is half of carrying capacity.") Value<Double> f0,
                                      @ParameterInfo(name = BParamName, description = "Initial growth rate of tumor growth.") Value<Number> b,
                                      @ParameterInfo(name = N0ParamName, description = "Limiting population size (carrying capacity).") Value<Number> N0) {
        //setParam(T50ParamName, t50);
        setParam(F0ParamName, f0);
        setParam(BParamName, b);
        //setParam(NINFINITYParamName, NInfinity);
        setParam(N0ParamName, N0);
    }

    @GeneratorInfo(name = "gompertz", narrativeName = "Gompertz growth function",
            category = GeneratorCategory.COAL_TREE, examples = {" .lphy"},
            description = "Models population growth using the Gompertz growth function.")

    @Override
    public Value<PopulationFunction> apply() {

        //double t50 = ((Number) getParams().get(T50ParamName).value()).doubleValue();
        double f0 = ((Number) getParams().get(F0ParamName).value()).doubleValue();
        double b = ((Number) getParams().get(BParamName).value()).doubleValue();
        // double NInfinity = ((Number) getParams().get(NINFINITYParamName).value()).doubleValue();
        double N0 = ((Number) getParams().get(N0ParamName).value()).doubleValue();

        //PopulationFunction gompertzPopulation = new GompertzPopulation(t50, b, NInfinity);

        PopulationFunction gompertzPopulation = new GompertzPopulation(N0, f0, b);

        return new Value<>(gompertzPopulation, this);
    }

    public Value<Double> getF0() {
        return getParams().get(F0ParamName);
    }

    public Value<Number> getB() {
        return getParams().get(BParamName);
    }

    public Value<Number> getN0() {
        return getParams().get(N0ParamName);
    }


}
