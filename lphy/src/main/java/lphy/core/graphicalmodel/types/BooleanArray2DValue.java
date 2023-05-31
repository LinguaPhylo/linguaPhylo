package lphy.core.graphicalmodel.types;

import lphy.core.graphicalmodel.components.DeterministicFunction;
import lphy.core.graphicalmodel.components.Value;

public class BooleanArray2DValue extends Value<Boolean[][]> {

    public BooleanArray2DValue(String id, Boolean[][] value) {
        super(id, value);
    }

    public BooleanArray2DValue(String id, Boolean[][] value, DeterministicFunction function) {
        super(id, value, function);
    }

    public String toString() {
        return Array2DUtils.toString(this);
    }
}
