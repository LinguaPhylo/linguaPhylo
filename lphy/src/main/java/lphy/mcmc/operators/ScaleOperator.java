package lphy.mcmc.operators;

import lphy.graphicalModel.RandomVariable;
import lphy.mcmc.Operator;
import lphy.util.RandomUtils;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Collections;
import java.util.List;

public class ScaleOperator implements Operator<Double> {

    RandomVariable<Double> variable;

    RandomGenerator random = RandomUtils.getRandom();
    double scaleFactor = 0.75;

    public ScaleOperator(RandomVariable<Double> variable, double scaleFactor) {
        this.variable = variable;
        this.scaleFactor = scaleFactor;
    }

    @Override
    public double operate() {
        Double oldValue = variable.value();

        double scale = getScaler();

        final double newValue = scale * oldValue;

        variable.setValue(newValue);

        return -Math.log(scale);
    }

    protected double getScaler() {
        return (scaleFactor + (random.nextDouble() * ((1.0 / scaleFactor) - scaleFactor)));
    }

    @Override
    public List<RandomVariable<Double>> getVariables() {
        return Collections.singletonList(variable);
    }
}
