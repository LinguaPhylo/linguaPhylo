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
                                  @ParameterInfo(name = BParamName, description = "Initial growth rate of tumor growth.") Value<Double> b,
                                  @ParameterInfo(name = NINFINITYParamName, description = "Limiting population size (carrying capacity).") Value<Double> NInfinity) {
        //setParam(T50ParamName, t50);
        setParam(F0ParamName, f0);
        setParam(BParamName, b);
        setParam(NINFINITYParamName, NInfinity);
    }

    @GeneratorInfo(name="gompertz", narrativeName = "Gompertz growth function",
            category = GeneratorCategory.COAL_TREE, examples = {" .lphy" },
            description = "Models population growth using the Gompertz growth function.")

    @Override
    public Value<PopulationFunction> apply() {

        //double t50 = ((Number) getParams().get(T50ParamName).value()).doubleValue();
        double f0 = ((Number) getParams().get(F0ParamName).value()).doubleValue();
        double b = ((Number) getParams().get(BParamName).value()).doubleValue();
        double NInfinity = ((Number) getParams().get(NINFINITYParamName).value()).doubleValue();

        //PopulationFunction gompertzPopulation = new GompertzPopulation(t50, b, NInfinity);

        PopulationFunction gompertzPopulation = new GompertzPopulation(f0, b, NInfinity);

        return new Value<>( gompertzPopulation, this);
    }


}
