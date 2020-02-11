package james.graphicalModel.types;

import james.graphicalModel.DeterministicFunction;
import james.graphicalModel.Value;

import java.util.Arrays;

public class IntegerArray2DValue extends Value<Integer[][]> {

    public IntegerArray2DValue(String id, Integer[][] value) {
        super(id, value);
    }

    public IntegerArray2DValue(String id, Integer[][] value, DeterministicFunction function) {
        super(id, value, function);
    }

    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append(getId());
        builder.append(" = [");
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
