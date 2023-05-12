package lphy.core.functions;

import lphy.graphicalModel.*;
import lphy.graphicalModel.types.NumberArrayValue;
import lphy.util.LoggerUtils;

import static lphy.core.ParameterNames.ArrayParamName;
import static lphy.core.ParameterNames.AxisParamName;

public class Sum2dArray extends DeterministicFunction<Number[]> {

    public Sum2dArray(@ParameterInfo(name = ArrayParamName, description = "the 2d array to sum the elements of.")
                      Value<Number[][]> x,
                      @ParameterInfo(name = AxisParamName, description = "the axis over which the array is summed: axis=0 for sum by column, axis=1 for sum by row. Defaults to axis=1 (row summation).")
                      Value<Integer> axis) {
        setParam(ArrayParamName, x);
        setParam(AxisParamName, axis);
    }

    @GeneratorInfo(name = "sum", description = "Sums over an axis of the 2d array")
    public Value<Number[]> apply() {
        Value<Number[][]> x = getParams().get(ArrayParamName);
        Integer axis = (Integer) getParams().get(AxisParamName).value();

        if (axis == 0) {
            // sum by cols
            Number[][] v = (Number[][]) getParams().get(ArrayParamName).value();
            SumUtils utils = new SumUtils();
            Number[] sumCols = utils.sumCols(v);
            return new NumberArrayValue(null, sumCols, this);
        } else {
            // sum by rows
            if (axis != 1) {
                LoggerUtils.log.warning("sum(array, axis) axis should be 0 or 1, input axis is " + axis + ", using default axis=1.");
            }
            Number[][] v = (Number[][]) getParams().get(ArrayParamName).value();
            SumUtils utils = new SumUtils();
            Number[] sumRows = utils.sumRows(v);
            return new NumberArrayValue(null, sumRows, this);
        }
    }
}
