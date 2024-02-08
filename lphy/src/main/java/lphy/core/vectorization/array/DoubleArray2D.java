package lphy.core.vectorization.array;

import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.datatype.DoubleArray2DValue;

public class DoubleArray2D extends ArrayFunction<Double[][]> {

    Value<Double[]>[] x;

    public DoubleArray2D(Value<Double[]>... x) {
        this.x = x;
        super.setInput(x);
    }

    @GeneratorInfo(name = "doubleArray", description = "The constructor function for a 2d array of doubles.")
    public Value<Double[][]> apply() {

        Double[][] values = new Double[x.length][];

        for (int i = 0; i < x.length; i++) {
            if (x[i] != null) // handle null
                values[i] = x[i].value();
        }

        return new DoubleArray2DValue(null, values, this);
    }

    @Override
    public Value<Double[]>[] getValues() {
        return x;
    }

    @Override
    public void setElement(Value value, int i) {
        x[i] = value;
    }

}
