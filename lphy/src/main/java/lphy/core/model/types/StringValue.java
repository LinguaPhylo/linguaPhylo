package lphy.core.model.types;

import lphy.core.model.components.DeterministicFunction;
import lphy.core.model.components.Value;

public class StringValue extends Value<String> {
	
    public StringValue(String id, String value) {
        super(id, value);
    }

    public StringValue(String id, String value, DeterministicFunction<String> function) {
        super(id, value, function);
    }
}
