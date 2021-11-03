package lphy.graphicalModel.types;

import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.Value;

public class BooleanArray2DValue extends Value<Boolean[][]> {

    public BooleanArray2DValue(String id, Boolean[][] value) {
        super(id, value);
    }

    public BooleanArray2DValue(String id, Boolean[][] value, DeterministicFunction function) {
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
