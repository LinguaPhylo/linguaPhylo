package lphy.app;

import lphy.graphicalModel.Utils;
import lphy.graphicalModel.Value;

public class DoubleArrayLabel extends ArrayLabel<Double> {

    public DoubleArrayLabel(Value<Double[]> values) {
        super(values);
    }

    public DoubleArrayLabel(Double[] values) {
        super(values);
    }

    @Override
    public String valueToString(Double rawValue) {
        return Utils.FORMAT.format(rawValue);
    }
}
