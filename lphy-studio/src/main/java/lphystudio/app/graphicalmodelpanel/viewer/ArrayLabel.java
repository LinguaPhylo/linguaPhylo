package lphystudio.app.graphicalmodelpanel.viewer;

import lphy.core.model.components.Value;

import javax.swing.*;

public class ArrayLabel<T> extends JLabel {

    public ArrayLabel(Value<T[]> values) {
        this(values.value());
    }

    public ArrayLabel(T[] values) {

        StringBuilder builder = new StringBuilder();
        builder.append("<html>[");
        if (values.length > 0) // not empty array
            builder.append(valueToString(values[0]));
        for (int i = 1; i < values.length; i++) {
            if (builder.length()-builder.lastIndexOf("<br>") > 80) {
                builder.append(",<br>");
            } else {
                builder.append(", ");
            }
            builder.append(valueToString(values[i]));
        }
        builder.append("]</html>");
        String str = builder.toString();
        setText(str);
    }

    public String valueToString(T rawValue) {
        return rawValue.toString();
    }
}
