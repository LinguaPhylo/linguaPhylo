package lphy.core.vectorization.array;

import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.datatype.IntegerArrayValue;

public class IntegerArray extends ArrayFunction<Integer[]> {

    Value<Integer>[] x;

    public IntegerArray(Value<Integer>... x) {
        this.x = x;
        super.setInput(x);
    }

    @GeneratorInfo(name = "integerArray", description = "The constructor function for an array of integers.")
    public Value<Integer[]> apply() {

        Integer[] values = new Integer[x.length];

        for (int i = 0; i < x.length; i++) {
            if (x[i] != null) // handle null
                values[i] = x[i].value();
        }

        return new IntegerArrayValue(null, values, this);
    }


    @Override
    public void setElement(Value value, int i) {
        x[i] = value;
    }

    @Override
    public Value<Integer>[] getValues() {
        return x;
    }
}
