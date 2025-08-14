package lphy.core.model.datatype;

import lphy.core.model.DeterministicFunction;
import org.phylospec.types.Bool;

public class BooleanArrayValue extends VectorValue<Bool> {

    public BooleanArrayValue(String id, Bool[] value) {
        super(id, value);
    }

    public BooleanArrayValue(String id, Bool[] value, DeterministicFunction function) {
        super(id, value, function);
    }
}
