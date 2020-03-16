package lphy.core.functions;

import lphy.TimeTree;
import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.types.DoubleArrayValue;
import lphy.graphicalModel.types.DoubleValue;
import scala.util.parsing.combinator.testing.Str;

public class DoubleArray extends DeterministicFunction<Double[]> {

    Value<Double>[] x;

    public DoubleArray(Value<Double>... x) {

        int length = x.length;
        this.x = x;

        for (int i = 0; i < length; i++) {
            setParam(i + "", x[i]);
        }
    }

    @GeneratorInfo(name = "doubleArray", description = "The constructor function for an array of doubles.")
    public Value<Double[]> apply() {

        Double[] values = new Double[x.length];

        for (int i = 0; i < x.length; i++) {
            values[i] = x[i].value();
        }

        return new DoubleArrayValue(null, values, this);
    }

    public void setParam(String param, Value value) {
        super.setParam(param, value);
        int i = Integer.parseInt(param);
        x[i] = value;
    }

    public String codeString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        builder.append(ref(x[0]));
        for (int i = 1; i < x.length; i++) {
            builder.append(", ");
            builder.append(ref(x[i]));
        }
        builder.append("]");
        return builder.toString();
    }

    private String ref(Value<Double> val) {
        if (val.isAnonymous()) return val.codeString();
        return val.getId();
    }
}
