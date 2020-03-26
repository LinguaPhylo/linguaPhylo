package lphy.core.lightweight;

public interface LightweightGenerativeDistribution<T> extends LightweightGenerator<T> {

    @Override
    default boolean isRandomGenerator() {
        return true;
    }

    default T sample() { return generateLight(); }

    default double density(T value) {
        return Math.exp(logDensity(value));
    }

    default double logDensity(T value) {
        return Math.log(density(value));
    }

    default T generateLight() { return sample(); }
}

