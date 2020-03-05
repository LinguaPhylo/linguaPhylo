package james.core.functions;

import james.graphicalModel.*;
import james.graphicalModel.types.IntegerValue;

public class Floor extends DeterministicFunction<Integer> {

    final String paramName;

    public Floor(@ParameterInfo(name = "0", description = "the argument.") Value<Double> x) {
        paramName = getParamName(0);
        setParam(paramName, x);
    }

    @FunctionInfo(name="floor",description = "The floor function.")
    public Value<Integer> apply() {
        Value<Double> v = (Value<Double>)getParams().get(paramName);
        return new IntegerValue((int)Math.floor(v.value()), this);
    }
}
