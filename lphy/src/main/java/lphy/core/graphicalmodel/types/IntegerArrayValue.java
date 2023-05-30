package lphy.core.graphicalmodel.types;

import lphy.core.graphicalmodel.components.DeterministicFunction;
import lphy.core.graphicalmodel.vectorization.RangeElement;
import lphy.core.graphicalmodel.vectorization.VectorValue;

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
