package lphy.graphicalModel.types;

import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.Value;

public class StringArrayValue extends Value<String[]> {

    public StringArrayValue(String id, String[] value) {
        super(id, value);
    }

    public StringArrayValue(String id, String[] value, DeterministicFunction function) {
        super(id, value, function);
    }

    public String valueToString() {

        String[] val = value();

        StringBuilder builder = new StringBuilder();
        builder.append("[");
        if (val.length > 0) {
            builder.append(quotedString(val[0]));
        }
        for (int i = 1; i < val.length; i++) {
            builder.append(", ");
            builder.append(quotedString(val[i]));
        }
        builder.append("]");
        return builder.toString();
    }
}
