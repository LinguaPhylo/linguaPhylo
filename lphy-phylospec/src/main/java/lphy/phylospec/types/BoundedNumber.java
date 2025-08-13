package lphy.phylospec.types;

import org.phylospec.types.Primitive;

public interface BoundedNumber<T extends java.lang.Number & Comparable<T>> extends Primitive<T>, Comparable<T> {

    /**
     * Get the lower bound.
     *
     * @return the lower bound value
     */
    T getLower();

    /**
     * Get the upper bound.
     *
     * @return the upper bound value
     */
    T getUpper();

    /**
     * Whether to equal lower bound
     *
     * @return true if the value can equal to the lower bound, false otherwise
     */
    boolean lowerInclusive();

    /**
     * Whether to equal upper bound
     *
     * @return true if the value can equal to the upper bound, false otherwise
     */
    boolean upperInclusive();

    /**
     * {@inheritDoc}
     *
     * @return "BoundedReal"
     */
    @Override
    default String getTypeName() {
        return "BoundedNumber";
    }

    /**
     * {@inheritDoc}
     *
     * valid if it is within the bound.
     *
     * @return true if the value is within the bound, false otherwise
     */
    @Override
    default boolean isValid() {
        boolean lowerCheck = lowerInclusive()
                ? getPrimitive().compareTo(getLower()) >= 0
                : getPrimitive().compareTo(getLower()) > 0;

        boolean upperCheck = upperInclusive()
                ? getPrimitive().compareTo(getUpper()) <= 0
                : getPrimitive().compareTo(getUpper()) < 0;

        return lowerCheck && upperCheck;
    }

    /**
     * {@inheritDoc}
     *
     * Get the bounds in string, for example, [1, 2)
     *
     * @return  Bounds in string
     */
    default String getBoundsString() {
        String lowerBracket = lowerInclusive() ? "[" : "(";
        String upperBracket = upperInclusive() ? "]" : ")";
        return lowerBracket + getLower() + ", " + getUpper() + upperBracket;
    }

}
