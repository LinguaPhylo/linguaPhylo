package lphy.base.distribution;

import lphy.base.math.MathUtils;
import lphy.core.model.GeneratorCategory;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.TreeMap;

/**
 * The scaled dirichlet probability distribution.
 * @see Dirichlet
 * @author Alexei Drummond
 * @author Walter Xie
 */
public class WeightedDirichlet extends ParametricDistribution<Double[]> {

    public static final String weightsParamName = "weights";

    private Value<Number[]> concentration;
    private Value<Integer[]> weights;

    public WeightedDirichlet(@ParameterInfo(name = DistributionConstants.concParamName, narrativeName = "concentration", description = "the concentration parameters of the scaled Dirichlet distribution.") Value<Number[]> concentration,
                             @ParameterInfo(name = weightsParamName, description = "the weight parameters of the scaled Dirichlet distribution.") Value<Integer[]> weights) {
        super();
        this.concentration = concentration;
        this.weights = weights;
    }

    @Override
    protected void constructDistribution(RandomGenerator random) {  }

    @GeneratorInfo(name = "WeightedDirichlet",
            category = GeneratorCategory.PRIOR, examples = {"totalEvidence.lphy","weightedDirichlet.lphy"},
            description = "The scaled dirichlet probability distribution.")
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
            double val = MathUtils.randomGamma(conc[i].doubleValue(), 1.0, random);
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
            put(DistributionConstants.concParamName, concentration);
            put(weightsParamName, weights);
        }};
    }

    public Value<Number[]> getConcentration() {
        return concentration;
    }

    public Value<Integer[]> getWeights() {
        return weights;
    }
}