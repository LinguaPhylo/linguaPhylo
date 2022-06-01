package lphy.graphicalModel;

/**
 * Created by Alexei Drummond on 17/12/19.
 */
public interface GenerativeDistribution1D<T> extends GenerativeDistribution<T> {

    /**
     * @return a two-dimensional array containing the lower and the upper bounds of the domain
     */
    T[] getDomainBounds();

    /**
     * Create the instance of distribution class given the parameter(s),
     * and cache it in order to reuse in sample() and density().
     * It should be only called in constructor and setParam(),
     * or any setters to change parameter value.
     */
    void constructDistribution(); //TODO mv to GenerativeDistribution
}
