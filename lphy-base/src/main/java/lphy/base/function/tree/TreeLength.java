package lphy.base.function.tree;

import lphy.base.evolution.tree.TimeTree;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.model.component.DeterministicFunction;
import lphy.core.model.component.GeneratorCategory;
import lphy.core.model.component.Value;
import lphy.core.model.datatype.DoubleValue;

/**
 * use {@link TimeTree#treeLength()}
 */
@Deprecated
public class TreeLength extends DeterministicFunction<Double> {

    final String paramName;

    public TreeLength(@ParameterInfo(name = "tree", description = "the tree.") Value<TimeTree> x) {
        paramName = getParamName(0);
        setParam(paramName, x);
    }

    @GeneratorInfo(name="treeLength",
            category = GeneratorCategory.TREE, examples = {"simpleCalibratedYule.lphy","simpleExtantBirthDeath.lphy"},
            description = "The sum of all the branch lengths in the tree.")
    public Value<Double> apply() {
        Value<TimeTree> v = (Value<TimeTree>)getParams().get(paramName);
        return new DoubleValue(v.value().treeLength(), this);
    }
}
