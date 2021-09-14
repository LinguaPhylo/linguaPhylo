package lphy.core.functions;

import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.types.DoubleValue;

public class Sum extends DeterministicFunction<Double> {

    public static final String arrayParamName = "array";

    public Sum(@ParameterInfo(name = arrayParamName, description = "the array to sum the elements of.") Value<Number[]> x) {
        setParam(arrayParamName, x);
    }

    @GeneratorInfo(name = "sum", description = "The sum of the elements of the given array")
    public Value<Double> apply() {
        Number[] x = (Number[])getParams().get(arrayParamName).value();

        double sum = 0.0;
        for (int i = 0; i < x.length; i++ ) {
            sum += x[i].doubleValue();
        }

        return new DoubleValue(sum, this);
    }
}
