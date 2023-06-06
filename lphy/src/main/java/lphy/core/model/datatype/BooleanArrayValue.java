package lphy.core.model.datatype;

import lphy.core.model.DeterministicFunction;

public class BooleanArrayValue extends VectorValue<Boolean> {

    public BooleanArrayValue(String id, Boolean[] value) {
        super(id, value);
    }

    public BooleanArrayValue(String id, Boolean[] value, DeterministicFunction function) {
        super(id, value, function);
    }
}
