package lphy.base.function;

import lphy.base.ParameterNames;
import lphy.core.logger.LoggerUtils;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.model.datatype.NumberArrayValue;

public class Sum2dArray extends DeterministicFunction<Number[]> {

    public Sum2dArray(@ParameterInfo(name = ParameterNames.ArrayParamName, description = "the 2d array to sum the elements of.")
                      Value<Number[][]> x,
                      @ParameterInfo(name = ParameterNames.AxisParamName, description = "the axis over which the array is summed: axis=0 for sum by column, axis=1 for sum by row. Defaults to axis=1 (row summation).")
                      Value<Integer> axis) {
        setParam(ParameterNames.ArrayParamName, x);
        setParam(ParameterNames.AxisParamName, axis);
    }

    @GeneratorInfo(name = "sum", description = "Sums over an axis of the 2d array")
    public Value<Number[]> apply() {
        Value<Number[][]> x = getParams().get(ParameterNames.ArrayParamName);
        Integer axis = (Integer) getParams().get(ParameterNames.AxisParamName).value();

        if (axis == 0) {
            // sum by cols
            Number[][] v = (Number[][]) getParams().get(ParameterNames.ArrayParamName).value();
            SumUtils utils = new SumUtils();
            Number[] sumCols = utils.sumCols(v);
            return new NumberArrayValue(null, sumCols, this);
        } else {
            // sum by rows
            if (axis != 1) {
                LoggerUtils.log.warning("sum(array, axis) axis should be 0 or 1, input axis is " + axis + ", using default axis=1.");
            }
            Number[][] v = (Number[][]) getParams().get(ParameterNames.ArrayParamName).value();
            SumUtils utils = new SumUtils();
            Number[] sumRows = utils.sumRows(v);
            return new NumberArrayValue(null, sumRows, this);
        }
    }
}
