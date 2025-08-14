package lphy.core.vectorization.array;

import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.datatype.IntegerArray2DValue;
import org.phylospec.types.Int;

public class IntegerArray2D extends ArrayFunction<Int[][]> {

    Value<Int[]>[] x;

    public IntegerArray2D(Value<Int[]>... x) {
        this.x = x;
        super.setInput(x);
    }

    @GeneratorInfo(name = "integerArray", description = "The constructor function for a 2d array of integers.")
    public Value<Int[][]> apply() {

        Int[][] values = new Int[x.length][];

        for (int i = 0; i < x.length; i++) {
            if (x[i] != null) // handle null
                values[i] = x[i].value();
        }

        return new IntegerArray2DValue(null,values, this);
    }

    @Override
    public void setElement(Value value, int i) {
        x[i] = value;
    }

    @Override
    public Value<Int[]>[] getValues() {
        return x;
    }
}
