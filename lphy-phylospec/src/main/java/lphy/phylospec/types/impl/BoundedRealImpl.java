package lphy.phylospec.types.impl;

import lphy.phylospec.types.BoundedReal;

public class BoundedRealImpl implements BoundedReal {

    private final double value;

    /**
     * Constructs a PositiveReal with the given value.
     *
     * @param value the positive real number value
     * @throws IllegalArgumentException if value is not positive or not finite
     */
    public BoundedRealImpl(double value) {
        this.value = value;
        if (!isValid()) {
            throw new IllegalArgumentException(
                    "BoundedRealImpl value must be " + getBoundsString() + ", but was: " + value);
        }
    }

    @Override
    public Double getPrimitive() {
        return value;
    }

    @Override
    public Double getLower() {
        return Double.NEGATIVE_INFINITY;
    }

    @Override
    public Double getUpper() {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public boolean lowerInclusive() {
        return false;
    }

    @Override
    public boolean upperInclusive() {
        return false;
    }

}
