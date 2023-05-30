package lphy.base.mcmc;

import lphy.core.graphicalmodel.components.RandomVariable;

import java.util.List;

public interface Operator<T> {

    /**
     *
     * @return Green-Hastings ratio
     */
    double operate();

    List<RandomVariable<T>> getVariables();
}
