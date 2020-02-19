package james.graphicalModel;

public abstract class DeterministicFunction<T> extends Func {

    public abstract Value<T> apply();

    @Override
    public Value currentValue() {
    	return apply();
    }
}
