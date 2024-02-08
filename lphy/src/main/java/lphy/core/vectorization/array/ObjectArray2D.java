package lphy.core.vectorization.array;

import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;

public class ObjectArray2D extends ArrayFunction<Object[][]> {

    Value<Object[]>[] x;

    public ObjectArray2D(Value<Object[]>... x) {
        this.x = x;
        super.setInput(x);
    }

    @GeneratorInfo(name = "array", description = "The constructor function for a 2d array of values.")
    public Value<Object[][]> apply() {

        Object[][] values = new Object[x.length][];

        for (int i = 0; i < x.length; i++) {
            values[i] = x[i].value();
        }

        return new Value(null, values, this);
    }

    @Override
    public void setElement(Value value, int i) {
        x[i] = value;
    }

    @Override
    public Value<Object[]>[] getValues() {
        return x;
    }
}
