package james.core.functions;

import james.graphicalModel.DoubleValue;
import james.graphicalModel.Function;
import james.graphicalModel.Value;

public class Exp extends Function<Double, Double> {

    public String getName() {
        return "exp";
    }

    public Value<Double> apply(Value<Double> v) {
        setParam("x", v);
        return new DoubleValue("exp(" + v.getId() + ")", Math.exp(v.value()), this);
    }
}
