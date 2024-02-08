package lphy.core.vectorization.array;

import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;

/**
 * Common code to share
 * @param <T> it should be array, e.g. Double[].
 */
public abstract class ArrayFunction<T> extends DeterministicFunction<T> {

    // get x, for example, which could be Value<Double>[] x
    abstract public Value<?>[] getValues();

    // x[i] = value;
    abstract public void setElement(Value<?> value, int i);


    // used in constructor
    protected void setInput(Value<?>... x) {
        int length = x.length;

        for (int i = 0; i < length; i++) {
            setInput(i + "", x[i]);
        }
    }

    public void setParam(String param, Value value) {
        super.setParam(param, value);
        int i = Integer.parseInt(param);
        setElement(value, i);
    }

    public String codeString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        builder.append( ref( getValues()[0] ) );
        for (int i = 1; i < getValues().length; i++) {
            builder.append(", ");
            builder.append( ref( getValues()[i] ) );
        }
        builder.append("]");
        return builder.toString();
    }

    protected String ref(Value<?> val) {
        if (val.isAnonymous()) return val.codeString();
        return val.getId();
    }
}
