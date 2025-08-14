package lphy.core.model.datatype;

import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import org.phylospec.types.Int;

public class IntegerValue extends Value<Int> implements RangeElement {

    public IntegerValue(String id, Int value) {
        super(id, value);
    }

    public IntegerValue(String id, Int value, DeterministicFunction function) {
        super(id, value, function);
    }

    /**
     * Constructs an anonymous integer value produced by the given function.
     * @param value
     * @param function
     */
    public IntegerValue(Int value, DeterministicFunction function) {
        super(null, value, function);
    }

    @Override
    public Integer[] range() {
        return new Integer[value().getPrimitive()];
    }
}
