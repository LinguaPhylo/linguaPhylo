package lphy.core.model;

public abstract class DeterministicFunction<T> extends BasicFunction {

    public abstract Value<T> apply();

    public Value<T> generate() {
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
