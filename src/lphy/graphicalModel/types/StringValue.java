package lphy.graphicalModel.types;

import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.Value;

public class StringValue extends Value<String> {
	
    public StringValue(String id, String value) {
        super(id, value);
    }

    public StringValue(String id, String value, DeterministicFunction<String> function) {
        super(id, value, function);
    }
}
