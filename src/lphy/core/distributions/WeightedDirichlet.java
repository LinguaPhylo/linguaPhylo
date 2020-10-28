package lphy.core.distributions;

import lphy.graphicalModel.*;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import static lphy.core.distributions.DistributionConstants.concParamName;

/**
 * Created by adru001 on 18/12/19.
 */
public class WeightedDirichlet implements GenerativeDistribution<Double[]> {

    public static final String weightsParamName = "weights";

    private Value<Number[]> concentration;
    private Value<Integer[]> weights;

    public WeightedDirichlet(@ParameterInfo(name = concParamName, description = "the concentration parameters of the scaled Dirichlet distribution.", type=Number[].class) Value<Number[]> concentration,
                             @ParameterInfo(name = weightsParamName, description = "the weight parameters of the scaled Dirichlet distribution.", type=Integer[].class) Value<Integer[]> weights) {
        this.concentration = concentration;
        this.weights = weights;
    }

    @GeneratorInfo(name = "WeightedDirichlet", description = "The scaled dirichlet probability distribution.")
    public RandomVariable<Double[]> sample() {

        Number[] weight = weights.value();
        Number[] conc = concentration.value();

        double weightsum = 0.0;
        for (int i = 0; i < weight.length; i++) {
            weightsum += weight[i].doubleValue();
        }

        Double[] z = new Double[concentration.value().length];
        double sum = 0.0;
        for (int i = 0; i < z.length; i++) {
            double val = Utils.randomGamma(conc[i].doubleValue(), 1.0);
            z[i] = val;
            sum += val * (weight[i].doubleValue() / weightsum);
        }

        for (int i = 0; i < z.length; i++) {
            z[i] /= sum;
        }

        return new RandomVariable<>(null, z, this);
    }

    public double density(Double d) {
        // TODO
        return 0;
    }

    @Override
    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(concParamName, concentration);
            put(weightsParamName, weights);
        }};
    }

    public Value<Number[]> getConcentration() {
        return concentration;
    }

    public Value<Integer[]> getWeights() {
        return weights;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(concParamName)) {
            concentration = value;
        } else if (paramName.equals(weightsParamName)) {
            weights = value;
        } else {
            throw new RuntimeException("Only valid parameter name is " + concParamName);
        }
    }

    public String toString() {
        return getName();
    }
}