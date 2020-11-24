package lphy.core.distributions;

import lphy.graphicalModel.*;
import java.util.*;
import static lphy.core.distributions.DistributionConstants.*;

/**
 * Created by adru001 on 18/12/19.
 */
public class Dirichlet implements GenerativeDistribution<Double[]> {

    private Value<Number[]> concentration;

    public Dirichlet(@ParameterInfo(name=concParamName, description="the concentration parameters of a Dirichlet distribution.") Value<Number[]> concentration) {
        this.concentration = concentration;
    }

    @GeneratorInfo(name="Dirichlet", description="The dirichlet probability distribution.")
    public RandomVariable<Double[]> sample() {

        Double[] dirichlet = new Double[concentration.value().length];
        double sum = 0.0;
        for (int i = 0; i < dirichlet.length; i++) {
            double val = Utils.randomGamma(concentration.value()[i].doubleValue(), 1.0);
            dirichlet[i] = val;
            sum += val;
        }
        for (int i = 0; i < dirichlet.length; i++) {
            dirichlet[i] /= sum;
        }

        return new RandomVariable<>("x", dirichlet, this);
    }

    public double density(Double[] d) {
        // TODO
        return 0;
    }

    @Override
    public Map<String,Value> getParams() {
        return Collections.singletonMap(concParamName, concentration);
    }

    public Value<Number[]> getConcentration() {
        return concentration;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(concParamName)) {
            concentration = value;
        } else {
            throw new RuntimeException("Only valid parameter name is " + concParamName);
        }
    }

    public String toString() {
        return getName();
    }
}