package james.graphicalModel.types;

import james.graphicalModel.DeterministicFunction;
import james.graphicalModel.Value;

import java.util.List;

public class IntegerListValue extends Value<List<Integer>> {

    public IntegerListValue(String id, List<Integer> value) {
        super(id, value);
    }

    public IntegerListValue(String id, List<Integer> value, DeterministicFunction function) {
        super(id, value, function);
    }
}
