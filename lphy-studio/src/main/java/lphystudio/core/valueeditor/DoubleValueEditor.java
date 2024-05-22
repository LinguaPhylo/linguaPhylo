package lphystudio.core.valueeditor;

import lphy.core.model.Value;

public class DoubleValueEditor extends AbstractValueEditor<Double> {

    public DoubleValueEditor(Value<Double> value)  {
        super(value);
    }

    @Override
    protected Double parseValue(String text) {
        return Double.parseDouble(text);
    }

    @Override
    protected Class getType() {
        return Double.class;
    }


}
