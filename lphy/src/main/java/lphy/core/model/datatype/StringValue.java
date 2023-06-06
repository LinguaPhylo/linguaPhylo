package lphy.core.model.datatype;

import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;

public class StringValue extends Value<String> {
	
    public StringValue(String id, String value) {
        super(id, value);
    }

    public StringValue(String id, String value, DeterministicFunction<String> function) {
        super(id, value, function);
    }
}
