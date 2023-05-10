package lphy.core.functions;

import lphy.graphicalModel.*;
import lphy.util.LoggerUtils;

import static lphy.core.ParameterNames.ArrayParamName;
import static lphy.core.ParameterNames.AxisParamName;

public class Sum2dArray extends DeterministicFunction<Number[]> {

    public Sum2dArray(@ParameterInfo(name = ArrayParamName, description = "the 2d array to sum the elements of.")
                      Value<Number[][]> x,
                      @ParameterInfo(name = AxisParamName, description = "the axis to sum the array over.")
                      Value<Integer> axis) {
        setParam(ArrayParamName, x);
        setParam(AxisParamName, axis);
    }

    @GeneratorInfo(name = "sum", description = "The sums over each row of the given array")
    public Value<Number[]> apply() {
        Number[][] v = (Number[][]) getParams().get(ArrayParamName).value();
        Integer axis = (Integer) getParams().get(AxisParamName).value();

        Value<Number[][]> x = getParams().get(ArrayParamName);

        if (axis == 0) {
            SumCols sumCols = new SumCols(x);
            return sumCols.apply();
            // sum columns
            // original implementation below moved to sumCols
//            Number[] sum = new Number[v[0].length];
//            for (int j = 0; j < v[0].length; j++) {
//                double colSum = 0.0;
//                for (int i = 0; i < v.length; i++) {
//                    colSum = colSum + v[i][j].doubleValue();
//                }
//                sum[j] = colSum;
//            }
//            return ValueUtils.createValue(sum, this);

        } else {
            // sum rows
            if (axis != 1) {
                LoggerUtils.log.warning("sum(array, axis) axis should be 0 or 1, input axis is " + axis + ", using default axis=1.");
            }

            SumRows sumRows = new SumRows(x);
            return sumRows.apply();
            // original implementation below moved to sumRows
//            Number[] sum = new Number[v.length];
//            for (int i = 0; i < v.length; i++) {
//                double rowSum = 0.0;
//                for (int j = 0; j < v[i].length; j++) {
//                    rowSum = rowSum + v[i][j].doubleValue();
//                }
//                sum[i] = rowSum;
//            }
//            return ValueUtils.createValue(sum, this);

        }
    }
}
