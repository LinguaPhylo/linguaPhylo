package lphy.app;

import lphy.graphicalModel.Value;

import javax.swing.*;

public class StringArrayLabel extends JLabel {

    public StringArrayLabel(Value<String[]> values) {

        StringBuilder builder = new StringBuilder();
        builder.append("<html>[");
        builder.append(Value.quotedString(values.value()[0]));
        for (int i = 1; i < values.value().length; i++) {
            if (i % 8 == 0) {
                builder.append(",<br>");
            } else {
                builder.append(", ");
            }
            builder.append(Value.quotedString(values.value()[i]));
        }
        builder.append("]</html>");
        String str = builder.toString();
        setText(str);
    }
}
