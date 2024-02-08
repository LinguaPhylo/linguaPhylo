package lphy.core.vectorization.array;

import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.datatype.IntegerArray2DValue;

public class IntegerArray2D extends ArrayFunction<Integer[][]> {

    Value<Integer[]>[] x;

    public IntegerArray2D(Value<Integer[]>... x) {
        this.x = x;
        super.setInput(x);
    }

    @GeneratorInfo(name = "integerArray", description = "The constructor function for a 2d array of integers.")
    public Value<Integer[][]> apply() {

        Integer[][] values = new Integer[x.length][];

        for (int i = 0; i < x.length; i++) {
            if (x[i] != null) // handle null
                values[i] = x[i].value();
        }

        return new IntegerArray2DValue(null, values, this);
    }

    @Override
    public void setElement(Value value, int i) {
        x[i] = value;
    }

    @Override
    public Value<Integer[]>[] getValues() {
        return x;
    }
}
