package lphy.core.functions;

import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.types.DoubleArrayValue;

import static lphy.core.ParameterNames.ArrayParamName;

public class Sum2dArray extends DeterministicFunction<Double[]> {

    public Sum2dArray(@ParameterInfo(name = ArrayParamName, description = "the 2d array to sum the elements of.")
                   Value<Double[][]> x) {
        setParam(ArrayParamName, x);
    }

    @GeneratorInfo(name = "sum", description = "The sums over each row of the given array")
    public Value<Double[]> apply() {
        Number[][] v = (Number[][]) getParams().get(ArrayParamName).value();

        Double[] sum = new Double[v.length];
        for (int i = 0; i < v.length; i++) {
            double rowSum = 0.0;
            for (int j = 0; j < v[i].length; j++) {
                rowSum = rowSum + v[i][j].doubleValue();
            }
            sum[i] = rowSum;
        }

        return new DoubleArrayValue(null, sum, this);
    }
}
