package lphy.core.vectorization.arrays;

import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.components.DeterministicFunction;
import lphy.core.model.components.Value;
import lphy.core.model.types.BooleanArrayValue;

public class BooleanArray extends DeterministicFunction<Boolean[]> {

    Value<Boolean>[] x;

    public BooleanArray(Value<Boolean>... x) {

        int length = x.length;
        this.x = x;

        for (int i = 0; i < length; i++) {
            setInput(i + "", x[i]);
        }
    }

    @GeneratorInfo(name = "booleanArray", description = "The constructor function for an array of booleans.")
    public Value<Boolean[]> apply() {

        Boolean[] values = new Boolean[x.length];

        for (int i = 0; i < x.length; i++) {
            values[i] = x[i].value();
        }

        return new BooleanArrayValue(null, values, this);
    }

    public void setParam(String param, Value value) {
        super.setParam(param, value);
        int i = Integer.parseInt(param);
        x[i] = value;
    }

    public String codeString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        builder.append(ref(x[0]));
        for (int i = 1; i < x.length; i++) {
            builder.append(", ");
            builder.append(ref(x[i]));
        }
        builder.append("]");
        return builder.toString();
    }

    private String ref(Value<?> val) {
        if (val.isAnonymous()) return val.codeString();
        return val.getId();
    }
}
