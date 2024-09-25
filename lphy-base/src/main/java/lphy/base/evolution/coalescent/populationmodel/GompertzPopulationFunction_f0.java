
package lphy.base.evolution.coalescent.populationmodel;

import lphy.base.evolution.coalescent.PopulationFunction;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

import static lphy.base.evolution.coalescent.populationmodel.GompertzPopulation_f0.*;

public class GompertzPopulationFunction_f0 extends DeterministicFunction<PopulationFunction> {
    public GompertzPopulationFunction_f0(@ParameterInfo(name = F0ParamName, description = "Time when population is half of carrying capacity.") Value<Double> f0,
                                         @ParameterInfo(name = BParamName, description = "Initial growth rate of tumor growth.") Value<Number> b,
                                         @ParameterInfo(name = N0ParamName, description = "Limiting population size (carrying capacity).") Value<Number> N0) {

        setParam(F0ParamName, f0);
        setParam(BParamName, b);
        setParam(N0ParamName, N0);
    }

    @GeneratorInfo(name = "gompertzPopFunc_f0", narrativeName = "Gompertz_f0 growth function",
            category = GeneratorCategory.COAL_TREE, examples = {" gomp_f0_jc69.lphy, gomp_f0_gt16.lphy"},
            description = "Models population growth using the Gompertz growth function.")
    @Override
    public Value<PopulationFunction> apply() {


        double f0 = ((Number) getParams().get(F0ParamName).value()).doubleValue();
        double b = ((Number) getParams().get(BParamName).value()).doubleValue();

        double N0 = ((Number) getParams().get(N0ParamName).value()).doubleValue();



        PopulationFunction gompertzPopulation = new GompertzPopulation_f0(N0, f0, b);

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
