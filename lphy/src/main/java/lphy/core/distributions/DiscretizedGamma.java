package lphy.core.distributions;

import lphy.graphicalModel.*;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.TreeMap;

import static lphy.core.distributions.DistributionConstants.shapeParamName;
import static lphy.graphicalModel.ValueUtils.doubleValue;
import static org.apache.commons.math3.distribution.GammaDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY;

/**
 * Discretized Gamma distribution
 */
public class DiscretizedGamma extends ParametricDistribution<Double> {

    private static final String ncatParamName = "ncat";
    private Value<Number> shape;
    private Value<Integer> ncat;

    GammaDistribution gammaDistribution;
    double[] rates;

    public DiscretizedGamma(@ParameterInfo(name = shapeParamName, description = "the shape of the discretized gamma distribution.") Value<Number> shape,
                            @ParameterInfo(name = ncatParamName, description = "the number of bins in the discretization.") Value<Integer> ncat) {
        super();
        this.shape = shape;
        this.ncat = ncat;

        constructDistribution(random);
    }

    @Override
    protected void constructDistribution(RandomGenerator random) {
        if (shape == null) throw new IllegalArgumentException("The shape value can't be null!");
        double sh = doubleValue(shape);
        // use code available since apache math 3.1
        gammaDistribution = new GammaDistribution(random, sh, 1.0 / sh, DEFAULT_INVERSE_ABSOLUTE_ACCURACY);

        rates = new double[ncat.value()];
    }


    @GeneratorInfo(name = "DiscretizeGamma",
            category = GeneratorCategory.PRIOR, examples = {"gtrGammaCoalescent.lphy","simpleBModelTest.lphy"},
            description = "The discretized gamma probability distribution with mean = 1.")
    public RandomVariable<Double> sample() {

        for (int i = 0; i < rates.length; i++) {
            double q = (2.0 * i + 1.0) / (2.0 * rates.length);
            rates[i] = gammaDistribution.inverseCumulativeProbability(q);
        }

        return new RandomVariable<>(null, rates[random.nextInt(rates.length)], this);
    }

    public double logDensity(Double[] x) {
        //TODO
        throw new UnsupportedOperationException("TODO");
    }

    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(shapeParamName, shape);
            put(ncatParamName, ncat);
        }};
    }

    public Value<Number> getShape() {
        return shape;
    }

    public Value<Integer> getNcat() {
        return ncat;
    }

}