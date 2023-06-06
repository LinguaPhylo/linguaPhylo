package lphystudio.app.graphicalmodelpanel.viewer;

import lphy.core.model.Value;

import javax.swing.*;

//TODO not used
public class BooleanArrayLabel extends JLabel {

    public BooleanArrayLabel(Value<Boolean[]> values) {

        StringBuilder builder = new StringBuilder();
        builder.append("<html>[");
        builder.append(values.value()[0]);
        for (int i = 1; i < values.value().length; i++) {
            if (i % 8 == 0) {
                builder.append(",<br>");
            } else {
                builder.append(", ");
            }
            builder.append(values.value()[i]);
        }
        builder.append("]</html>");
        String str = builder.toString();
        setText(str);
    }
}
