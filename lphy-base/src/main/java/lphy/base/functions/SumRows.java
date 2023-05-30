package lphy.base.functions;

import lphy.base.ParameterNames;
import lphy.core.graphicalmodel.components.DeterministicFunction;
import lphy.core.graphicalmodel.components.GeneratorInfo;
import lphy.core.graphicalmodel.components.ParameterInfo;
import lphy.core.graphicalmodel.components.Value;
import lphy.core.graphicalmodel.types.NumberArrayValue;

public class SumRows extends DeterministicFunction<Number[]> {

    public SumRows(@ParameterInfo(name = ParameterNames.ArrayParamName, description = "the 2d array to sum the elements of.")
               Value<Number[][]> x) {
        setParam(ParameterNames.ArrayParamName, x);
    }

    @GeneratorInfo(name = "sumRows", description = "Sums over each row of the given array")
    public Value<Number[]> apply() {
        Number[][] v = (Number[][]) getParams().get(ParameterNames.ArrayParamName).value();
        SumUtils utils = new SumUtils();
        Number[] sum = utils.sumRows(v);
        return new NumberArrayValue(null, sum, this);
    }
}
