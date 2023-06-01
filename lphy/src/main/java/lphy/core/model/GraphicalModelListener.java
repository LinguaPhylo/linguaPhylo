package lphy.core.model;

import lphy.core.model.components.DeterministicFunction;
import lphy.core.model.components.GenerativeDistribution;
import lphy.core.model.components.Value;

public interface GraphicalModelListener {

    void valueSelected(Value value);

    void generativeDistributionSelected(GenerativeDistribution g);

    void functionSelected(DeterministicFunction f);

    /**
     * This is called each time the model is layed out for painting.
     */
    void layout();
}
