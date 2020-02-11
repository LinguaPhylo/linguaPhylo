package james.graphicalModel.types;

import james.graphicalModel.DeterministicFunction;
import james.graphicalModel.swing.DoubleValueEditor;

import javax.swing.*;

public class DoubleValue extends NumberValue<Double> {

    public DoubleValue(String id, Double value) {

        super(id, value);
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
