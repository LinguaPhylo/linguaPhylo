package lphystudio.app.graphicalmodelpanel.viewer;

import lphy.core.model.component.Value;

public class DoubleArrayLabel extends ArrayLabel<Double> {

    public DoubleArrayLabel(Value<Double[]> values) {
        super(values);
    }

    public DoubleArrayLabel(Double[] values) {
        super(values);
    }

    @Override
    public String valueToString(Double rawValue) {
        return DecimalFormat.FORMAT.format(rawValue);
    }
}
