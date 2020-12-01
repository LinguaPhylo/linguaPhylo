package lphy.app;

import lphy.graphicalModel.Utils;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.ValueUtils;

import javax.swing.*;

import static lphy.graphicalModel.ValueUtils.quotedString;

public class StringArrayLabel extends ArrayLabel<String> {

    public StringArrayLabel(Value<String[]> values) {
        super(values);
    }

    public StringArrayLabel(String[] values) {
        super(values);
    }

    @Override
    public String valueToString(String rawValue) {
        return quotedString(rawValue);
    }
}
