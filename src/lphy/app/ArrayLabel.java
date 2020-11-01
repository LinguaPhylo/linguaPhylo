package lphy.app;

import lphy.graphicalModel.Utils;
import lphy.graphicalModel.Value;

import javax.swing.*;

public class ArrayLabel<T> extends JLabel {

    public ArrayLabel(Value<T[]> values) {

        StringBuilder builder = new StringBuilder();
        builder.append("<html>[");
        builder.append(valueToString(values.value()[0]));
        for (int i = 1; i < values.value().length; i++) {
            if (builder.length()-builder.lastIndexOf("<br>") > 80) {
                builder.append(",<br>");
            } else {
                builder.append(", ");
            }
            builder.append(valueToString(values.value()[i]));
        }
        builder.append("]</html>");
        String str = builder.toString();
        setText(str);
    }

    public String valueToString(T rawValue) {
        return rawValue.toString();
    }
}
