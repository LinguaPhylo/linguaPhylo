package james.graphicalModel;

public class DoubleValue extends Value<Double> {

    public DoubleValue(String id, Double value) {
        super(id, value);
    }

    public DoubleValue(String id, Double value, Function function) {
        super(id, value, function);
    }
}
