package lphy.core.model.component;

/**
 * Define the 1-d distribution.
 * For example, lphy.base.distributions.ExpMarkovChain requires
 * 1-d prior at first value.
 */
public interface GenerativeDistribution1D<T> extends GenerativeDistribution<T> {

    /**
     * @return a two-dimensional array containing the lower and the upper bounds of the domain
     */
    T[] getDomainBounds();

}
