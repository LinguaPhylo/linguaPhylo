package lphy.base.functions;

import lphy.base.ParameterNames;
import lphy.core.model.components.*;
import lphy.core.model.types.IntegerValue;

public class SumBoolean extends DeterministicFunction<Integer> {

    public SumBoolean(@ParameterInfo(name = ParameterNames.ArrayParamName, description = "the boolean array to sum the elements of.") Value<Boolean[]> x) {
        setParam(ParameterNames.ArrayParamName, x);
    }

    @GeneratorInfo(name = "hammingWeight",
            category = GeneratorCategory.PRIOR, examples = {"simpleRandomLocalClock2.lphy","covidDPG.lphy"},
            description = "The sum of the true elements of the given boolean array")
    public Value<Integer> apply() {
        Boolean[] x = (Boolean[])getParams().get(ParameterNames.ArrayParamName).value();

        int sum = 0;
        for (Boolean i : x) {
            sum += i ? 1 : 0;
        }

        return new IntegerValue(sum, this);
    }
}
