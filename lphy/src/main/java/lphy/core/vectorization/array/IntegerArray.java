package lphy.core.vectorization.array;

import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.datatype.IntegerArrayValue;
import org.phylospec.types.Int;

public class IntegerArray extends ArrayFunction<Int[]> {

    Value<Int>[] x;

    public IntegerArray(Value<Int>... x) {
        this.x = x;
        super.setInput(x);
    }

    @GeneratorInfo(name = "integerArray", description = "The constructor function for an array of integers.")
    public Value<Int[]> apply() {

        Int[] values = new Int[x.length];

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
    public Value<Int>[] getValues() {
        return x;
    }
}
