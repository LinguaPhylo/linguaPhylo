package lphy.core.functions;

import lphy.TimeTree;
import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.FunctionInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.types.IntegerValue;

public class NTaxa extends DeterministicFunction<Integer> {

    final String paramName;

    public NTaxa(@ParameterInfo(name = "0", description = "the tree.") Value<TimeTree> x) {
        paramName = getParamName(0);
        setParam(paramName, x);
    }

    @FunctionInfo(name="ntaxa",description = "The number of taxa in the tree")
    public Value<Integer> apply() {
        Value<TimeTree> v = (Value<TimeTree>)getParams().get(paramName);
        return new IntegerValue( v.value().n(), this);
    }
}
