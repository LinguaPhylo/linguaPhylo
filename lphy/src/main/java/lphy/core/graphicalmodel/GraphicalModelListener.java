package lphy.core.graphicalmodel;

import lphy.core.graphicalmodel.components.DeterministicFunction;
import lphy.core.graphicalmodel.components.GenerativeDistribution;
import lphy.core.graphicalmodel.components.Value;

public interface GraphicalModelListener {

    void valueSelected(Value value);

    void generativeDistributionSelected(GenerativeDistribution g);

    void functionSelected(DeterministicFunction f);

    /**
     * This is called each time the model is layed out for painting.
     */
    void layout();
}
