package lphy.core.distributions;

import lphy.graphicalModel.*;

import java.util.*;

/**
 * Created by adru001 on 18/12/19.
 */
public class Dirichlet implements GenerativeDistribution<Double[]> {

    private final String concParamName;
    private Value<Double[]> concentration;

    public Dirichlet(@ParameterInfo(name="conc", description="the concentration parameters of a Dirichlet distribution.", type=Double[].class) Value<Double[]> concentration) {

        this.concentration = concentration;
        concParamName = getParamName(0);
    }

    @GeneratorInfo(name="Dirichlet", description="The dirichlet probability distribution.")
    public RandomVariable<Double[]> sample() {

        Double[] dirichlet = new Double[concentration.value().length];
        double sum = 0.0;
        for (int i = 0; i < dirichlet.length; i++) {
            double val = Utils.randomGamma(concentration.value()[i], 1.0);
            dirichlet[i] = val;
            sum += val;
        }
        for (int i = 0; i < dirichlet.length; i++) {
            dirichlet[i] /= sum;
        }

        return new RandomVariable<>("x", dirichlet, this);
    }

    public double density(Double d) {
        // TODO
        return 0;
    }

    @Override
    public Map<String,Value> getParams() {
        return Collections.singletonMap(getParamName(0), concentration);
    }

    public Value<Double[]> getConcentration() {
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