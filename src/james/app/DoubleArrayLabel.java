package james.app;

import james.graphicalModel.Utils;
import james.graphicalModel.Value;

import javax.swing.*;

public class DoubleArrayLabel extends JLabel {

    public DoubleArrayLabel(Value<Double[]> values) {

        StringBuilder builder = new StringBuilder();
        builder.append("[");
        builder.append(Utils.FORMAT.format(values.value()[0]));
        for (int i = 1; i < values.value().length; i++) {
            builder.append(", ");
            builder.append(Utils.FORMAT.format(values.value()[i]));

        }
        builder.append("]");
        String str = builder.toString();
        setText(str);
    }
}
