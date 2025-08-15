package lphy.core.vectorization.array;

import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.datatype.RealArrayValue;
import org.phylospec.types.Real;

public class RealArray extends ArrayFunction<Real[]> {

    Value<Real>[] x;

    public RealArray(Value<Real>... x) {
        this.x = x;
        super.setInput(x);
    }

    @GeneratorInfo(name = "doubleArray", description = "The constructor function for an array of doubles.")
    public Value<Real[]> apply() {

        Real[] values = new Real[x.length];

        for (int i = 0; i < x.length; i++) {
            if (x[i] != null) // handle null
                values[i] = x[i].value();
        }

        return new RealArrayValue(null, values, this);
    }

    @Override
    public Value<Real>[] getValues() {
        return x;
    }

    @Override
    public void setElement(Value value, int i) {
        x[i] = value;
    }

}
