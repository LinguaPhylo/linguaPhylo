package james.core.functions;

import james.TimeTree;
import james.graphicalModel.DeterministicFunction;
import james.graphicalModel.FunctionInfo;
import james.graphicalModel.ParameterInfo;
import james.graphicalModel.Value;
import james.graphicalModel.types.IntegerValue;

public class Newick extends DeterministicFunction<TimeTree> {

    final String paramName;

    public Newick(@ParameterInfo(name = "newick", description = "the tree in Newick format.") Value<String> x) {
        paramName = getParamName(0);
        setParam(paramName, x);
    }

    @FunctionInfo(name="treeparser",description = "A function that parses a tree from a newick formatted string.")
    public Value<TimeTree> apply() {
        Value<String> newickValue = (Value<String>)getParams().get(paramName);
        
        TimeTree tree = parseNewick(newickValue.value());
        
        return new Value<>("treeparser " + newickValue.getId(), tree, this);
    }

    private TimeTree parseNewick(String value) {
        return null;
    }
}
