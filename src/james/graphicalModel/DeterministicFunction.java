package james.graphicalModel;

public abstract class DeterministicFunction<T> extends Func {

    public abstract Value<T> apply();

    public Value generate() {
        return apply();
    }

    @Override
    public T value() {
    	return apply().value();
    }

    @Override
    public String getUniqueId() {
        return hashCode() + "";
    }
}
