package lphy.core.functions;

import lphy.graphicalModel.*;
import lphy.graphicalModel.types.NumberArrayValue;

import static lphy.core.ParameterNames.ArrayParamName;

public class SumCols extends DeterministicFunction<Number[]> {

    public SumCols(@ParameterInfo(name = ArrayParamName, description = "the 2d array to sum the elements of.")
                   Value<Number[][]> x) {
        setParam(ArrayParamName, x);
    }

    @GeneratorInfo(name = "sumCols", description = "Sums over each column of the given array")
    public Value<Number[]> apply() {
        Number[][] v = (Number[][]) getParams().get(ArrayParamName).value();
        SumUtils utils = new SumUtils();
        Number[] sum = utils.sumCols(v);
        return new NumberArrayValue(null, sum, this);
    }
}
