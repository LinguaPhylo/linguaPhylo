package lphy.base.functions;

import lphy.base.ParameterNames;
import lphy.core.graphicalmodel.components.DeterministicFunction;
import lphy.core.graphicalmodel.components.GeneratorInfo;
import lphy.core.graphicalmodel.components.ParameterInfo;
import lphy.core.graphicalmodel.components.Value;
import lphy.core.graphicalmodel.types.NumberArrayValue;

public class SumCols extends DeterministicFunction<Number[]> {

    public SumCols(@ParameterInfo(name = ParameterNames.ArrayParamName, description = "the 2d array to sum the elements of.")
                   Value<Number[][]> x) {
        setParam(ParameterNames.ArrayParamName, x);
    }

    @GeneratorInfo(name = "sumCols", description = "Sums over each column of the given array")
    public Value<Number[]> apply() {
        Number[][] v = (Number[][]) getParams().get(ParameterNames.ArrayParamName).value();
        SumUtils utils = new SumUtils();
        Number[] sum = utils.sumCols(v);
        return new NumberArrayValue(null, sum, this);
    }
}
