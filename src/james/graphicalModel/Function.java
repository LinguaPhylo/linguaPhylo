package james.graphicalModel;

public interface Function<U, V> extends java.util.function.Function<Value<U>, Value<V>> {

    String getName();
}
