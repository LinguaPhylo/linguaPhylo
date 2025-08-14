package lphy.core.model.datatype;

import lphy.core.model.DeterministicFunction;
import org.phylospec.types.Real;

public class RealArrayValue extends VectorValue<Real> {

    public RealArrayValue(String id, Real[] value) {
        super(id, value);
    }

    public RealArrayValue(String id, Real[] value, DeterministicFunction function) {
        super(id, value, function);
    }
}
