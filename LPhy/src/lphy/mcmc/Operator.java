package lphy.mcmc;

import lphy.graphicalModel.RandomVariable;

import java.util.List;

public interface Operator<T> {

    /**
     *
     * @return Green-Hastings ratio
     */
    double operate();

    List<RandomVariable<T>> getVariables();
}
