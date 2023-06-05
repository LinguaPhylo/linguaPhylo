package lphy.core.model.datatype;

import lphy.core.model.component.DeterministicFunction;
import lphy.core.vectorization.array.RangeElement;

public class IntegerValue extends NumberValue<Integer> implements RangeElement {

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
}
