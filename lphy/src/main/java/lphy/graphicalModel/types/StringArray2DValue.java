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
        return Array2DUtils.toString(this);
    }
}
