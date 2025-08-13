package lphy.phylospec.types;

import org.phylospec.types.Real;

public interface BoundedReal extends Real, BoundedNumber<Double> {

    /**
     * {@inheritDoc}
     *
     * @return "BoundedReal"
     */
    @Override
    default java.lang.String getTypeName() {
        return "BoundedReal";
    }

    /**
     * {@inheritDoc}
     *
     * A BoundedReal is valid if it is finite and within the bound.
     *
     * @return true if the value is finite and within the bound, false otherwise
     */
    @Override
    default boolean isValid() {
        return Real.super.isValid() && BoundedNumber.super.isValid();
    }

    @Override
    default int compareTo(Double o) {
        return getPrimitive().compareTo(o);
    }
}
