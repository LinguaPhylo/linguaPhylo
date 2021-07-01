package lphystudio.app;

import lphy.graphicalModel.Utils;
import lphy.graphicalModel.Value;

public class NumberArrayLabel extends ArrayLabel<Number> {

    public NumberArrayLabel(Value<Number[]> values) {
        super(values);
    }
    
    public NumberArrayLabel(Number[] values) {
        super(values);
    }


    @Override
    public String valueToString(Number rawValue) {
        return Utils.FORMAT.format(rawValue.doubleValue());
    }
}
