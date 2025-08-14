package lphy.core.model.datatype;

import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import org.phylospec.types.Real;

public class RealArray2DValue extends Value<Real[][]> {

    public RealArray2DValue(String id, Real[][] value) {
        super(id, value);
    }

    public RealArray2DValue(String id, Real[][] value, DeterministicFunction function) {
        super(id, value, function);
    }

    /**
     * Constructs an anonymous value of type {@code Value<Double[][]>}
     * @param value
     * @param function
     */
    public RealArray2DValue(Real[][] value, DeterministicFunction function) {
        super(null, value, function);
    }

//    public JComponent getViewer() {
//        return new DoubleArray2DEditor(value(), false);
//    }

    public String toString() {
        return Array2DUtils.toString(this);
    }

}
