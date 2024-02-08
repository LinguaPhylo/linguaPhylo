package lphy.core.vectorization.array;

import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.datatype.DoubleArrayValue;

public class DoubleArray extends ArrayFunction<Double[]> {

    Value<Double>[] x;

    public DoubleArray(Value<Double>... x) {
        this.x = x;
        super.setInput(x);
    }

    @GeneratorInfo(name = "doubleArray", description = "The constructor function for an array of doubles.")
    public Value<Double[]> apply() {

        Double[] values = new Double[x.length];

        for (int i = 0; i < x.length; i++) {
            if (x[i] != null) // handle null
                values[i] = x[i].value();
        }

        return new DoubleArrayValue(null, values, this);
    }

    @Override
    public Value<Double>[] getValues() {
        return x;
    }

    @Override
    public void setElement(Value value, int i) {
        x[i] = value;
    }

}
