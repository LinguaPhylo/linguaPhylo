package lphy.graphicalModel.types;

public class Domain<T> {

    Domain(T lower, T upper) {
        setLowerBound(lower);
        setUpperBound(upper);
    }

    private T lowerBound, upperBound;

    public T getLowerBound() {
        return lowerBound;
    }

    public T getUpperBound() {
        return upperBound;
    }

    private void setLowerBound(T lower) {
        lowerBound = lower;
    }

    private void setUpperBound(T upper) {
        upperBound = upper;
    }

    static final Domain<Double> SCALE_PARAMETER_DOMAIN = new Domain<Double>(0.0, Double.POSITIVE_INFINITY);

    static final Domain<Double> PROB_PARAMETER_DOMAIN = new Domain<Double>(0.0, 1.0);
}
