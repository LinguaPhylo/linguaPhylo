package lphy.graphicalModel.types;

import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.Value;

import java.util.Arrays;

public class StringArrayValue extends Value<String[]> {

    public StringArrayValue(String id, String[] value) {
        super(id, value);
    }

    public StringArrayValue(String id, String[] value, DeterministicFunction function) {
        super(id, value, function);
    }

    public String toString() {
        return (isAnonymous() ? "" : (getId() + " = ")) + Arrays.toString(value());
    }
}
