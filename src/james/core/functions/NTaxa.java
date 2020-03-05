package james.core.functions;

import james.TimeTree;
import james.graphicalModel.DeterministicFunction;
import james.graphicalModel.FunctionInfo;
import james.graphicalModel.ParameterInfo;
import james.graphicalModel.Value;
import james.graphicalModel.types.IntegerValue;

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
