package lphy.core.functions;

import lphy.graphicalModel.*;
import lphy.graphicalModel.types.NumberArrayValue;

import static lphy.core.ParameterNames.ArrayParamName;

public class SumCols extends DeterministicFunction<Number[]> {

    public SumCols(@ParameterInfo(name = ArrayParamName, description = "the 2d array to sum the elements of.")
                   Value<Number[][]> x) {
        setParam(ArrayParamName, x);
    }

    @GeneratorInfo(name = "sumCols", description = "The sums over each column of the given array")
    public Value<Number[]> apply() {
        Number[][] v = (Number[][]) getParams().get(ArrayParamName).value();

        Number[] sum = new Number[v[0].length];
        for (int j = 0; j < v[0].length; j++) {
            double colSum = 0.0;
            for (int i = 0; i < v.length; i++) {
                colSum = colSum + v[i][j].doubleValue();
            }
            sum[j] = colSum;
        }

        return new NumberArrayValue(null, sum, this);
    }
}
