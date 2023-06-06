package lphy.core.model.datatype;

import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;

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
