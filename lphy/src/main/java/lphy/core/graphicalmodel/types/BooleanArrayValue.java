package lphy.core.graphicalmodel.types;

import lphy.core.graphicalmodel.components.DeterministicFunction;
import lphy.core.graphicalmodel.vectorization.VectorValue;

public class BooleanArrayValue extends VectorValue<Boolean> {

    public BooleanArrayValue(String id, Boolean[] value) {
        super(id, value);
    }

    public BooleanArrayValue(String id, Boolean[] value, DeterministicFunction function) {
        super(id, value, function);
    }
}
