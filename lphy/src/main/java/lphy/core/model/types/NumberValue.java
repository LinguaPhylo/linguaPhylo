package lphy.core.model.types;

import lphy.core.model.components.DeterministicFunction;
import lphy.core.model.components.Value;

public class NumberValue<U extends Number> extends Value<U> {

    public NumberValue(String id, U value) {

        super(id, value);
    }

    public NumberValue(String id, U value, DeterministicFunction<U> function) {

        super(id, value, function);
    }
}
