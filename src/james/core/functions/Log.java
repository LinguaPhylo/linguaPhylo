package james.core.functions;

import james.graphicalModel.*;

public class Log extends Function<Double, Double> {

    @FunctionInfo(name="log",description = "The natural logarithm function: ln x")
    public Value<Double> apply(Value<Double> v) {
        setParam("x", v);
        return new DoubleValue("log " + v.getId(), Math.log(v.value()), this);
    }
}
