package lphy.core.model.datatype;

import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.phylospec.types.PrimitiveType;
import org.phylospec.types.Int;

public class IntegerValue extends Value<Integer> implements RangeElement,Int,PrimitiveType<Integer> {

    public IntegerValue(String id, Integer value) {
        super(id, value);
    }

    public IntegerValue(String id, Integer value, DeterministicFunction function) {
        super(id, value, function);
    }

    /**
     * Constructs an anonymous integer value produced by the given function.
     * @param value
     * @param function
     */
    public IntegerValue(Integer value, DeterministicFunction function) {
        super(null, value, function);
    }

    @Override
    public Integer[] range() {
        return new Integer[value()];
    }

    @Override
    public Integer getPrimitive() {
        return value();
    }
}
