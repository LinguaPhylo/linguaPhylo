package james.graphicalModel.types;

import james.app.DoubleArray2DEditor;
import james.graphicalModel.DeterministicFunction;
import james.graphicalModel.Value;

import javax.swing.*;

public class DoubleArray2DValue extends Value<Double[][]> {

    public DoubleArray2DValue(String id, Double[][] value) {
        super(id, value);
    }

    public DoubleArray2DValue(String id, Double[][] value, DeterministicFunction function) {
        super(id, value, function);
    }

    public JComponent getViewer() {
        return new DoubleArray2DEditor(value(), false);
    }

    public String toString() {

        StringBuilder builder = new StringBuilder();
        if (!isAnonymous()) builder.append(getId() + " = ");
        builder.append("[");
        for (int i = 0; i < value().length; i++) {
            builder.append("[");
            builder.append(value()[i][0]);
            for (int j = 1; j < value()[i].length; j++) {
                builder.append(", ");
                builder.append(value()[i][j]);
            }
            builder.append("]");
            if (i < value().length -1) {
                builder.append(", ");
            }
        }
        builder.append("]");

        return builder.toString();
    }

}
