package lphystudio.core.valueeditor;

import lphy.core.model.Value;

public class BooleanValueEditor extends AbstractValueEditor<Boolean> {

    public BooleanValueEditor(Value<Boolean> value)  {
        super(value);
    }

    @Override
    protected Boolean parseValue(String text) {
        return Boolean.parseBoolean(text);
    }

    @Override
    protected Class getType() {
        return Boolean.class;
    }
}
