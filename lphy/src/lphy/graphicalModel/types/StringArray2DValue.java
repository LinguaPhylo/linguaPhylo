package lphy.graphicalModel.types;

import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.Value;

public class StringArray2DValue extends Value<String[][]> {

    public StringArray2DValue(String id, String[][] value) {
        super(id, value);
    }

    public StringArray2DValue(String id, String[][] value, DeterministicFunction function) {
        super(id, value, function);
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
