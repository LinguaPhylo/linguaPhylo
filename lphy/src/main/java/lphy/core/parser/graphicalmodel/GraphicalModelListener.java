package lphy.core.parser.graphicalmodel;

import lphy.core.model.DeterministicFunction;
import lphy.core.model.GenerativeDistribution;
import lphy.core.model.Value;

public interface GraphicalModelListener {

    void valueSelected(Value value);

    void generativeDistributionSelected(GenerativeDistribution g);

    void functionSelected(DeterministicFunction f);

    /**
     * This is called each time the model is layed out for painting.
     */
    void layout();
}
