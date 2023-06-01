package lphy.core.model.types;

import lphy.core.model.components.DeterministicFunction;
import lphy.core.model.components.Value;

public class BooleanValue extends Value<Boolean> {

    public BooleanValue(String id, Boolean value) {
        super(id, value);
    }

    public BooleanValue(String id, Boolean value, DeterministicFunction function) {
        super(id, value, function);
    }

    /**
     * Constructs an anonymous integer value produced by the given function.
     * @param value
     * @param function
     */
    public BooleanValue(Boolean value, DeterministicFunction function) {
        super(null, value, function);
    }

}
