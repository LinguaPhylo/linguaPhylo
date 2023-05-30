package lphy.core.graphicalmodel.types;

import lphy.core.graphicalmodel.components.DeterministicFunction;
import lphy.core.graphicalmodel.components.Value;

public class NumberValue<U extends Number> extends Value<U> {

    public NumberValue(String id, U value) {

        super(id, value);
    }

    public NumberValue(String id, U value, DeterministicFunction<U> function) {

        super(id, value, function);
    }
}
