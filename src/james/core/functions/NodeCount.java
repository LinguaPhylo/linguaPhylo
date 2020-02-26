package james.core.functions;

import james.TimeTree;
import james.graphicalModel.DeterministicFunction;
import james.graphicalModel.FunctionInfo;
import james.graphicalModel.ParameterInfo;
import james.graphicalModel.Value;
import james.graphicalModel.types.IntegerValue;

public class NodeCount extends DeterministicFunction<Integer> {

    final String paramName;

    public NodeCount(@ParameterInfo(name = "0", description = "the tree.") Value<TimeTree> x) {
        paramName = getParamName(0);
        setParam(paramName, x);
    }

    @FunctionInfo(name="nodecount",description = "The number of nodes in the tree")
    public Value<Integer> apply() {
        Value<TimeTree> v = (Value<TimeTree>)getParams().get(paramName);
        return new IntegerValue("nodecount(" + v.getLabel() + ")", v.value().getNodeCount(), this);
    }
}
