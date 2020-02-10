package james.graphicalModel.types;

import james.graphicalModel.DeterministicFunction;
import james.graphicalModel.types.NumberValue;

public class IntegerValue extends NumberValue<Integer> {

    public IntegerValue(String id, Integer value) {
        super(id, value);
    }

    public IntegerValue(String id, Integer value, DeterministicFunction function) {
        super(id, value, function);
    }
}
