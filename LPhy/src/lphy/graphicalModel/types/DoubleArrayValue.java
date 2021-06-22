package lphy.graphicalModel.types;

import lphy.graphicalModel.DeterministicFunction;

public class DoubleArrayValue extends VectorValue<Double> {

    public DoubleArrayValue(String id, Double[] value) {
        super(id, value);
    }

    public DoubleArrayValue(String id, Double[] value, DeterministicFunction function) {
        super(id, value, function);
    }
}
