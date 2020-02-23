package james.graphicalModel.types;

import james.graphicalModel.DeterministicFunction;
import james.graphicalModel.Value;

import java.util.Arrays;
import java.util.List;

public class IntegerArrayValue extends Value<Integer[]> {

    public IntegerArrayValue(String id, Integer[] value) {
        super(id, value);
    }

    public IntegerArrayValue(String id, Integer[] value, DeterministicFunction function) {
        super(id, value, function);
    }

    public String toString() {
        return (isAnonymous() ? "" : (getId() + " = ")) + Arrays.toString(value());
    }
}
