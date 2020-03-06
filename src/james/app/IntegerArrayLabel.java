package james.app;

import james.graphicalModel.Value;

import javax.swing.*;

public class IntegerArrayLabel extends JLabel {

    public IntegerArrayLabel(Value<Integer[]> values) {

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
