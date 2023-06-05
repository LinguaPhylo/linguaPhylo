package lphy.core.model.datatype;

import lphy.core.model.component.DeterministicFunction;
import lphy.core.vectorization.VectorValue;

public class DoubleArrayValue extends VectorValue<Double> {

    public DoubleArrayValue(String id, Double[] value) {
        super(id, value);
    }

    public DoubleArrayValue(String id, Double[] value, DeterministicFunction function) {
        super(id, value, function);
    }
}
