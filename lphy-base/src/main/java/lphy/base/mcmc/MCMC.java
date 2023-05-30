package lphy.base.mcmc;

import lphy.core.graphicalmodel.GraphicalModel;
import lphy.core.graphicalmodel.components.RandomVariable;
import lphy.core.graphicalmodel.components.Value;
import lphy.core.graphicalmodel.components.ValueListener;

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
