package lphy.core.model.datatype;

import lphy.core.model.component.DeterministicFunction;
import lphy.core.model.component.Value;

public class DoubleArray2DValue extends Value<Double[][]> {

    public DoubleArray2DValue(String id, Double[][] value) {
        super(id, value);
    }

    public DoubleArray2DValue(String id, Double[][] value, DeterministicFunction function) {
        super(id, value, function);
    }

    /**
     * Constructs an anonymous value of type {@code Value<Double[][]>}
     * @param value
     * @param function
     */
    public DoubleArray2DValue(Double[][] value, DeterministicFunction function) {
        super(null, value, function);
    }

//    public JComponent getViewer() {
//        return new DoubleArray2DEditor(value(), false);
//    }

    public String toString() {
        return Array2DUtils.toString(this);
    }

}
