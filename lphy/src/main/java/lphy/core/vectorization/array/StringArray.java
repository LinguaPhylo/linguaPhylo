package lphy.core.vectorization.array;

import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.datatype.StringArrayValue;

public class StringArray extends ArrayFunction<String[]> {

    Value<String>[] x;

    public StringArray(Value<String>... x) {
        this.x = x;
        super.setInput(x);
    }

    @GeneratorInfo(name = "stringArray", description = "The constructor function for an array of strings.")
    public Value<String[]> apply() {

        String[] values = new String[x.length];

        for (int i = 0; i < x.length; i++) {
            if (x[i] != null) // handle nul
                values[i] = x[i].value();
        }

        return new StringArrayValue(null, values, this);
    }

    @Override
    public void setElement(Value value, int i) {
        x[i] = value;
    }


    @Override
    public Value<String>[] getValues() {
        return x;
    }
}
