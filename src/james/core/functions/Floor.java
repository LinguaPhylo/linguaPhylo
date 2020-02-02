package james.core.functions;

import james.graphicalModel.DoubleValue;
import james.graphicalModel.Function;
import james.graphicalModel.FunctionInfo;
import james.graphicalModel.Value;

public class Floor extends Function<Double, Double> {

    @FunctionInfo(name="floor",description = "The floor function. Returns the integer component of the argument.")
    public Value<Double> apply(Value<Double> v) {
        setParam("x", v);
        return new DoubleValue("floor " + v.getId(), Math.floor(v.value()), this);
    }
}
