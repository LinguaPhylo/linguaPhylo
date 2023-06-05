package lphy.core.model.datatype;

import lphy.core.model.component.DeterministicFunction;
import lphy.core.vectorization.VectorValue;
import lphy.core.vectorization.array.RangeElement;

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
