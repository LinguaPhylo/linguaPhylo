package lphy.graphicalModel.types;

import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.Value;

import java.util.Arrays;

public class BooleanArrayValue extends VectorValue<Boolean> {

    public BooleanArrayValue(String id, Boolean[] value) {
        super(id, value);
    }

    public BooleanArrayValue(String id, Boolean[] value, DeterministicFunction function) {
        super(id, value, function);
    }
}
