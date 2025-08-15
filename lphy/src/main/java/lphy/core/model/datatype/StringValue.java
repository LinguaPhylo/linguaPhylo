package lphy.core.model.datatype;

import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.phylospec.types.PrimitiveType;
import org.phylospec.types.Str;

public class StringValue extends Value<String> implements Str, PrimitiveType<String> {
	
    public StringValue(String id, String value) {
        super(id, value);
    }

    public StringValue(String id, String value, DeterministicFunction<String> function) {
        super(id, value, function);
    }

    @Override
    public String getPrimitive() {
        return value();
    }
}
