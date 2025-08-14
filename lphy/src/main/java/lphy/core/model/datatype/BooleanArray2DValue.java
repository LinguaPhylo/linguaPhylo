package lphy.core.model.datatype;

import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import org.phylospec.types.Bool;

public class BooleanArray2DValue extends Value<Bool[][]> {

    public BooleanArray2DValue(String id, Bool[][] value) {
        super(id, value);
    }

    public BooleanArray2DValue(String id, Bool[][] value, DeterministicFunction function) {
        super(id, value, function);
    }

    public String toString() {
        return Array2DUtils.toString(this);
    }
}
