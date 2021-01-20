package lphy.core.functions;

import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.types.DoubleValue;
import lphy.graphicalModel.types.IntegerValue;

public class SumBoolean extends DeterministicFunction<Integer> {

    public static final String arrayParamName = "array";

    public SumBoolean(@ParameterInfo(name = arrayParamName, description = "the boolean array to sum the elements of.") Value<Boolean[]> x) {
        setParam(arrayParamName, x);
    }

    @GeneratorInfo(name = "hammingWeight", description = "The sum of the true elements of the given boolean array")
    public Value<Integer> apply() {
        Boolean[] x = (Boolean[])getParams().get(arrayParamName).value();

        int sum = 0;
        for (Boolean i : x) {
            sum += i ? 1 : 0;
        }

        return new IntegerValue(sum, this);
    }
}
