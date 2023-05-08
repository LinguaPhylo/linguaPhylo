package lphy.core.functions;

import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;

import static lphy.core.ParameterNames.ArrayParamName;

public class Sum<T> extends DeterministicFunction<T> {

    public Sum(@ParameterInfo(name = ArrayParamName, description = "the array to sum the elements of.")
               Value<T> x) {
        setParam(ArrayParamName, x);
    }

    @GeneratorInfo(name = "sum", description = "The sum of the elements of the given array")
    public Value<T> apply() {
        // Number[] x = (Number[])getParams().get(ArrayParamName).value();
        Value<T> valueType = (Value<T>) getParams().get(ArrayParamName);
        if (valueType.value() instanceof Number[]) {
            Number[] x = (Number[]) valueType.value();

            Double sum = 0.0;
            for (int i = 0; i < x.length; i++ ) {
                sum += x[i].doubleValue();
            }

            return new Value(null, sum, this);

        } else if (valueType.value() instanceof Number[][]) {
            Number[][] x = (Number[][]) valueType.value();

            Double[] sum = new Double[x.length];
            for (int i = 0; i < x.length; i++) {
                double rowSum = 0.0;
                for (int j = 0; j < x[i].length; j++) {
                    rowSum = rowSum + x[i][j].doubleValue();
                }
                sum[i] = rowSum;
            }

            return new Value(null, sum, this);

        } else {
            throw new RuntimeException("The input array of sum() must be a 1d or 2d array.");
        }

    }
}
