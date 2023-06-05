package lphy.core.model;

import lphy.core.model.component.DeterministicFunction;
import lphy.core.model.component.GenerativeDistribution;
import lphy.core.model.component.Value;

public interface GraphicalModelListener {

    void valueSelected(Value value);

    void generativeDistributionSelected(GenerativeDistribution g);

    void functionSelected(DeterministicFunction f);

    /**
     * This is called each time the model is layed out for painting.
     */
    void layout();
}
