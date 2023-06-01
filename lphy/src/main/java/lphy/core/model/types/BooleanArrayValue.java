package lphy.core.model.types;

import lphy.core.model.components.DeterministicFunction;
import lphy.core.vectorization.VectorValue;

public class BooleanArrayValue extends VectorValue<Boolean> {

    public BooleanArrayValue(String id, Boolean[] value) {
        super(id, value);
    }

    public BooleanArrayValue(String id, Boolean[] value, DeterministicFunction function) {
        super(id, value, function);
    }
}
