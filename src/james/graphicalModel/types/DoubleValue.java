package james.graphicalModel.types;

import james.graphicalModel.DeterministicFunction;
import james.graphicalModel.Value;
import james.graphicalModel.swing.DoubleValueEditor;

import javax.swing.*;

public class DoubleValue extends NumberValue<Double> {

    public DoubleValue(String id, Double value) {

        super(id, value);
    }

    public DoubleValue(String id, Value<Double[]> value) {
        super(id, value.value()[0]);

        if (value.value().length > 1) {
            System.err.println("WARNING: ignoring all but the first element in " + (value.getId()) + " of length " + value.value().length + "!");
        }
    }

    public DoubleValue(String id, Double value, DeterministicFunction<Double> function) {

        super(id, value, function);
    }

    @Override
    public JComponent getViewer() {
        if (getFunction() == null) {
            return new DoubleValueEditor(this);
        }
        return super.getViewer();
    }
}
