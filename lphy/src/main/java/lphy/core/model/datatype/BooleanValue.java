package lphy.core.model.datatype;

import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import org.phylospec.types.Bool;

public class BooleanValue extends Value<Bool> {

    public BooleanValue(String id, Bool value) {
        super(id, value);
    }

    public BooleanValue(String id, Bool value, DeterministicFunction function) {
        super(id, value, function);
    }

    /**
     * Constructs an anonymous integer value produced by the given function.
     * @param value
     * @param function
     */
    public BooleanValue(Bool value, DeterministicFunction function) {
        super(null, value, function);
    }

}
