package lphy.core.functions;

import lphy.evolution.tree.TimeTree;
import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.types.IntegerValue;

public class NodeCount extends DeterministicFunction<Integer> {

    final String paramName;

    public NodeCount(@ParameterInfo(name = "tree", description = "the tree.") Value<TimeTree> x) {
        paramName = getParamName(0);
        setParam(paramName, x);
    }

    @GeneratorInfo(name="nodecount",description = "The number of nodes in the tree")
    public Value<Integer> apply() {
        Value<TimeTree> v = (Value<TimeTree>)getParams().get(paramName);
        return new IntegerValue(v.value().getNodeCount(), this);
    }
}
