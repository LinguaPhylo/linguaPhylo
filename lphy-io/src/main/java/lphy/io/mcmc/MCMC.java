package lphy.io.mcmc;

import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.ValueListener;
import lphy.core.parser.graphicalmodel.GraphicalModel;

import java.util.HashMap;
import java.util.Map;

public class MCMC {

    GraphicalModel model;

    Map<RandomVariable, Object> oldValues = new HashMap<>();

    boolean inAcceptReject = false;

    public MCMC(GraphicalModel model) {
        this.model = model;

        for (Value value : model.getModelValues()) {
            if (value instanceof RandomVariable) {
                value.addValueListener(new ValueListener() {
                    @Override
                    public void valueSet(Object oldValue, Object newValue) {
                        if (inAcceptReject) oldValues.put((RandomVariable)value, oldValue);
                    }
                });
            }
        }
    }

    public void run(int chainLength) {

        double logPosterior = model.computeLogPosterior();

        for (int i = 0; i < chainLength; i++) {
            oldValues.clear();
            inAcceptReject = true;
            //Operator operator = pickRandomOperator();
            //double hastingsRatio = operator.operate();

        }

    }
}
