package lphy.base.function;

import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

public class Copy<T> extends DeterministicFunction<T> {

    private final String xParamName = "val";
    private Value<T> x;

    public Copy(@ParameterInfo(name = xParamName,
            description = "the value to replicate.") Value<T> x) {

        this.x = x;
        if (x == null) throw new IllegalArgumentException("The value can't be null!");
        setParam(xParamName, x);
    }

    @GeneratorInfo(name = "copy", description = "Replicate a value. " +
            "When combining with 'replicates=n', it is equivalent to replicate the value n times.")
    public Value<T> apply() {
        Value<T> value = getParams().get(xParamName);
        return new Value<>( null, value.value(), this);
    }

}