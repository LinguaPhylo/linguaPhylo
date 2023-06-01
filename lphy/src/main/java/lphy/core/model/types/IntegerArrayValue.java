package lphy.core.model.types;

import lphy.core.model.components.DeterministicFunction;
import lphy.core.vectorization.RangeElement;
import lphy.core.vectorization.VectorValue;

import java.util.Arrays;
public class IntegerArrayValue extends VectorValue<Integer> implements RangeElement {

    public IntegerArrayValue(String id, Integer[] value) {
        super(id, value);
    }

    public IntegerArrayValue(String id, Integer[] value, DeterministicFunction function) {
        super(id, value, function);
    }

    public String toString() {
        return (isAnonymous() ? "" : (getId() + " = ")) + Arrays.toString(value());
    }

    @Override
    public Integer[] range() {
        return value();
    }
}
