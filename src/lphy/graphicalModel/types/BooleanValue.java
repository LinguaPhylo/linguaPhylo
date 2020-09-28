package lphy.graphicalModel.types;

import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.swing.BooleanValueEditor;
import lphy.graphicalModel.swing.IntegerValueEditor;

import javax.swing.*;

public class BooleanValue extends Value<Boolean> {

    public BooleanValue(String id, Boolean value) {
        super(id, value);
    }

    public BooleanValue(String id, Boolean value, DeterministicFunction function) {
        super(id, value, function);
    }

    /**
     * Constructs an anonymous integer value produced by the given function.
     * @param value
     * @param function
     */
    public BooleanValue(Boolean value, DeterministicFunction function) {
        super(null, value, function);
    }

    @Override
    public JComponent getViewer() {
        if (getGenerator() == null) {
            return new BooleanValueEditor(this);
        }
        return super.getViewer();
    }
}
