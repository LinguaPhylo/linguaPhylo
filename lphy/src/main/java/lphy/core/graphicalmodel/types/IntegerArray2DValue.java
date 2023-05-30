package lphy.core.graphicalmodel.types;

import lphy.core.graphicalmodel.components.DeterministicFunction;
import lphy.core.graphicalmodel.components.Value;

public class IntegerArray2DValue extends Value<Integer[][]> {

    public IntegerArray2DValue(String id, Integer[][] value) {
        super(id, value);
    }

    public IntegerArray2DValue(String id, Integer[][] value, DeterministicFunction function) {
        super(id, value, function);
    }

    public String toString() {
        return Array2DUtils.toString(this);
    }
}
