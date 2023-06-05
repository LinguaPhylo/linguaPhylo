package lphy.core.model.datatype;

import lphy.core.model.component.DeterministicFunction;
import lphy.core.model.component.Value;

public class NumberValue<U extends Number> extends Value<U> {

    public NumberValue(String id, U value) {

        super(id, value);
    }

    public NumberValue(String id, U value, DeterministicFunction<U> function) {

        super(id, value, function);
    }
}
