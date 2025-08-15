package lphy.core.model.datatype;

import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.phylospec.types.PrimitiveType;
import org.phylospec.types.Bool;

public class BooleanValue extends Value<Boolean> implements Bool, PrimitiveType<Boolean> {

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

    @Override
    public Boolean getPrimitive() {
        return value();
    }
}
