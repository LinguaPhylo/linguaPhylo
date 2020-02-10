package james.graphicalModel.types;

import james.graphicalModel.DeterministicFunction;

public class DoubleValue extends NumberValue<Double> {

    public DoubleValue(String id, Double value) {

        super(id, value);
    }

    public DoubleValue(String id, Double value, DeterministicFunction<Double> function) {

        super(id, value, function);
    }
}
