package lphy.graphicalModel.types;

import lphy.graphicalModel.DeterministicFunction;

public class NumberArrayValue extends VectorValue<Number> {

    public NumberArrayValue(String id, Number[] value) {
        super(id, value);
    }

    public NumberArrayValue(String id, Number[] value, DeterministicFunction function) {
        super(id, value, function);
    }
}
