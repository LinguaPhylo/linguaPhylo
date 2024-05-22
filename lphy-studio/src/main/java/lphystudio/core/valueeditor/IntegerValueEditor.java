package lphystudio.core.valueeditor;

import lphy.core.model.Value;

public class IntegerValueEditor extends AbstractValueEditor<Integer> {

    public IntegerValueEditor(Value<Integer> value)  {
        super(value);
    }

    @Override
    protected Integer parseValue(String text) {
        return Integer.parseInt(text);
    }

    @Override
    protected Class getType() {
        return Integer.class;
    }
}
