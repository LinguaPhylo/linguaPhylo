package lphy.core.model;

/**
 * Define the 1-d distribution.
 * For example, lphy.base.distributions.ExpMarkovChain requires
 * 1-d prior at first value.
 */
public interface GenerativeDistribution1D<T,S> extends GenerativeDistribution<T> {

    /**
     * @return a two-dimensional array containing the lower and the upper bounds of the domain
     */
    S[] getDomainBounds();

}
