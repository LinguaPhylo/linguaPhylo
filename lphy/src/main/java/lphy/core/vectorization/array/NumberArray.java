package lphy.core.vectorization.array;

import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.datatype.NumberArrayValue;

public class NumberArray extends ArrayFunction<Number[]> {

    Value<Number>[] x;

    public NumberArray(Value<Number>... x) {
        this.x = x;
        super.setInput(x);
    }

    @GeneratorInfo(name = "numberArray", description = "The constructor function for an array of numbers.")
    public Value<Number[]> apply() {

        Number[] values = new Number[x.length];

        for (int i = 0; i < x.length; i++) {
            if (x[i] != null) // handle null
                values[i] = x[i].value();
        }

        return new NumberArrayValue(null, values, this);
    }

    @Override
    public void setElement(Value value, int i) {
        x[i] = value;
    }

    @Override
    public Value<Number>[] getValues() {
        return x;
    }
}
