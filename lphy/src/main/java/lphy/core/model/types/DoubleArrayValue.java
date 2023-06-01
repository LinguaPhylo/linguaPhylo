package lphy.core.model.types;

import lphy.core.model.components.DeterministicFunction;
import lphy.core.vectorization.VectorValue;

public class DoubleArrayValue extends VectorValue<Double> {

    public DoubleArrayValue(String id, Double[] value) {
        super(id, value);
    }

    public DoubleArrayValue(String id, Double[] value, DeterministicFunction function) {
        super(id, value, function);
    }
}
