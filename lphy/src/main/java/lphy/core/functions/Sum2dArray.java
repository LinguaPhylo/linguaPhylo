package lphy.core.functions;

import lphy.graphicalModel.*;

import static lphy.core.ParameterNames.ArrayParamName;

@Deprecated
public class Sum2dArray extends DeterministicFunction<Number[]> {

    public Sum2dArray(@ParameterInfo(name = ArrayParamName, description = "the 2d array to sum the elements of.")
                   Value<Number[][]> x) {
        setParam(ArrayParamName, x);
    }

    @GeneratorInfo(name = "sum", description = "The sums over each row of the given array")
    public Value<Number[]> apply() {
        Number[][] v = (Number[][]) getParams().get(ArrayParamName).value();

        Number[] sum = new Number[v.length];
        for (int i = 0; i < v.length; i++) {
            double rowSum = 0.0;
            for (int j = 0; j < v[i].length; j++) {
                rowSum = rowSum + v[i][j].doubleValue();
            }
            sum[i] = rowSum;
        }

        return ValueUtils.createValue(sum, this);
    }
}
