package lphy.core.functions;

import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.types.IntegerValue;

/**
 * replaced by built-in {@link lphy.parser.functions.ExpressionNode1Arg}
 */
@Deprecated
public class Round extends DeterministicFunction<Integer> {

    public static final String paramName = "0";

    public Round(@ParameterInfo(name = paramName, description = "the argument.") Value<Double> x) {
        setParam(paramName, x);
    }

    // TODO why no LongValue
    @GeneratorInfo(name="round",description = "The round function to return the closest integer.")
    public Value<Integer> apply() {
        Value<Double> v = (Value<Double>)getParams().get(paramName);
        return new IntegerValue((int)Math.round(v.value()), this);
    }
}
