package lphy.core.model.datatype;

import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import org.phylospec.types.Int;

public class IntegerArray2DValue extends Value<Int[][]> {

    public IntegerArray2DValue(String id, Int[][] value) {
        super(id, value);
    }

    public IntegerArray2DValue(String id, Int[][] value, DeterministicFunction function) {
        super(id, value, function);
    }

    public String toString() {
        return Array2DUtils.toString(this);
    }
}
