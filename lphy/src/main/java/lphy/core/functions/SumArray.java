package lphy.core.functions;

import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.types.DoubleValue;

import static lphy.core.ParameterNames.ArrayParamName;

public class SumArray extends DeterministicFunction<Double> {

    public SumArray(@ParameterInfo(name = ArrayParamName, description = "the array to sum the elements of.")
               Value<Number[]> x) {
        setParam(ArrayParamName, x);
    }

    @GeneratorInfo(name = "sum", description = "The sum of the elements of the given array")
    public Value<Double> apply() {
        Number[] x = (Number[])getParams().get(ArrayParamName).value();

        double sum = 0.0;
        for (int i = 0; i < x.length; i++ ) {
            sum += x[i].doubleValue();
        }

        return new DoubleValue(sum, this);
    }
}