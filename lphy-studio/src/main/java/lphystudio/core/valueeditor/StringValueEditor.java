package lphystudio.core.valueeditor;

import lphy.core.model.Value;

public class StringValueEditor extends AbstractValueEditor<String> {

    public StringValueEditor(Value<String> value)  {
        super(value);
    }

    @Override
    protected String parseValue(String text) {
        return text;
    }

    @Override
    protected Class<String> getType() {
        return String.class;
    }
}
