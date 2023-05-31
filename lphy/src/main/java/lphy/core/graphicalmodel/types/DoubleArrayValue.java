package lphy.core.graphicalmodel.types;

import lphy.core.graphicalmodel.components.DeterministicFunction;
import lphy.core.vectorization.VectorValue;

public class DoubleArrayValue extends VectorValue<Double> {

    public DoubleArrayValue(String id, Double[] value) {
        super(id, value);
    }

    public DoubleArrayValue(String id, Double[] value, DeterministicFunction function) {
        super(id, value, function);
    }
}
