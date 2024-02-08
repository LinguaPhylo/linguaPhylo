package lphy.core.model.datatype;

import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;

public class NumberArray2DValue extends Value<Number[][]> {

    public NumberArray2DValue(String id, Number[][] value) {
        super(id, value);
    }

    public NumberArray2DValue(String id, Number[][] value, DeterministicFunction function) {
        super(id, value, function);
    }

    public String toString() {
        return Array2DUtils.toString(this);
    }
}
