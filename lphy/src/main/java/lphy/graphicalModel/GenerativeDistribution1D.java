package lphy.graphicalModel;

/**
 * Created by Alexei Drummond on 17/12/19.
 */
public interface GenerativeDistribution1D<T> extends GenerativeDistribution<T> {

    /**
     * @return a two-dimensional array containing the lower and the upper bounds of the domain
     */
    T[] getDomainBounds();

}
