package lphy.graphicalModel.types;

import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.Value;

import java.util.Arrays;

public class BooleanArrayValue extends Value<Boolean[]> {

    public BooleanArrayValue(String id, Boolean[] value) {
        super(id, value);
    }

    public BooleanArrayValue(String id, Boolean[] value, DeterministicFunction function) {
        super(id, value, function);
    }

    public String toString() {
        return (isAnonymous() ? "" : (getId() + " = ")) + Arrays.toString(value());
    }

}
