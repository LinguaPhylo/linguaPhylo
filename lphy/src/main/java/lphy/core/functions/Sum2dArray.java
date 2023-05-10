package lphy.core.functions;

import lphy.graphicalModel.*;
import lphy.util.LoggerUtils;

import static lphy.core.ParameterNames.ArrayParamName;
import static lphy.core.ParameterNames.AxisParamName;

public class Sum2dArray extends DeterministicFunction<Number[]> {

    public Sum2dArray(@ParameterInfo(name = ArrayParamName, description = "the 2d array to sum the elements of.")
                      Value<Number[][]> x,
                      @ParameterInfo(name = AxisParamName, description = "the axis over which the array is summed: axis=0 to sum by column, axis=1 to sum by row. Defaults to axis=1 (row summation).")
                      Value<Integer> axis) {
        setParam(ArrayParamName, x);
        setParam(AxisParamName, axis);
    }

    @GeneratorInfo(name = "sum", description = "The sums over the axis of the given 2d array")
    public Value<Number[]> apply() {
        Value<Number[][]> x = getParams().get(ArrayParamName);
        Integer axis = (Integer) getParams().get(AxisParamName).value();

        if (axis == 0) {
            // sum by rols
            SumCols sumCols = new SumCols(x);
            return sumCols.apply();
        } else {
            // sum by rows
            if (axis != 1) {
                LoggerUtils.log.warning("sum(array, axis) axis should be 0 or 1, input axis is " + axis + ", using default axis=1.");
            }
            SumRows sumRows = new SumRows(x);
            return sumRows.apply();
        }
    }
}
