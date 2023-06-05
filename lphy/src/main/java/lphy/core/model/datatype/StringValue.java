package lphy.core.model.datatype;

import lphy.core.model.component.DeterministicFunction;
import lphy.core.model.component.Value;

public class StringValue extends Value<String> {
	
    public StringValue(String id, String value) {
        super(id, value);
    }

    public StringValue(String id, String value, DeterministicFunction<String> function) {
        super(id, value, function);
    }
}
