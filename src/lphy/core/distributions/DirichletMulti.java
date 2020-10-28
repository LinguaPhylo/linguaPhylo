package lphy.core.distributions;

import lphy.core.functions.IntegerArray;
import lphy.graphicalModel.*;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import static lphy.core.distributions.DistributionConstants.*;
import static lphy.core.distributions.DistributionConstants.sdParamName;

/**
 * Created by adru001 on 18/12/19.
 */
public class DirichletMulti implements GenerativeDistribution<Double[][]> {

    private Value<Number[]> concentration;
    private Value<Integer> n;

    public DirichletMulti(@ParameterInfo(name=concParamName, description="the concentration parameters of a Dirichlet distribution.", type=Number[].class) Value<Number[]> concentration,
                          @ParameterInfo(name=nParamName, description = "the number of iid samples from this Dirichlet.", type= Integer.class) Value<Integer> n) {
        this.concentration = concentration;
        this.n = n;
    }

    @GeneratorInfo(name="Dirichlet", description="The dirichlet probability distribution.")
    public RandomVariable<Double[][]> sample() {

        Double[][] dirichlet = new Double[n.value()][concentration.value().length];

        for (int rep = 0; rep < dirichlet.length; rep++) {
            double sum = 0.0;

            for (int i = 0; i < dirichlet[rep].length; i++) {
                double val = Utils.randomGamma(concentration.value()[i].doubleValue(), 1.0);
                dirichlet[rep][i] = val;
                sum += val;
            }
            for (int i = 0; i < dirichlet[rep].length; i++) {
                dirichlet[rep][i] /= sum;
            }
        }

        return new RandomVariable<>(null, dirichlet, this);
    }

    public double density(Double[][] d) {
        // TODO
        return 0;
    }

    @Override
    public Map<String,Value> getParams() {
        return new TreeMap<>() {{
            put(concParamName, concentration);
            put(nParamName, n);
        }};

    }

    public Value<Number[]> getConcentration() {
        return concentration;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(concParamName)) {
            concentration = value;
        } else if (paramName.equals(nParamName)) {
            n = value;
        } else {
            throw new RuntimeException("Only valid parameter name is " + concParamName);
        }
    }

    public String toString() {
        return getName();
    }
}