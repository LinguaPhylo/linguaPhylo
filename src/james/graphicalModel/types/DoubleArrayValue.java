package james.graphicalModel.types;

import james.graphicalModel.DeterministicFunction;
import james.graphicalModel.Utils;
import james.graphicalModel.Value;
import james.graphicalModel.swing.DoubleValueEditor;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

public class DoubleArrayValue extends Value<Double[]> {

    public DoubleArrayValue(String id, Double[] value) {
        super(id, value);
    }

    public DoubleArrayValue(String id, Double[] value, DeterministicFunction function) {
        super(id, value, function);
    }

    public String toString() {
        return (isAnonymous() ? "" : (getId() + " = ")) + Arrays.toString(value());
    }

}
