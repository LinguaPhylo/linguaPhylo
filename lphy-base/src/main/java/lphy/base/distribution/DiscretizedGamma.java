package lphy.base.distribution;

import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.phylospec.types.PositiveInt;
import org.phylospec.types.PositiveReal;
import org.phylospec.types.Probability;
import org.phylospec.types.impl.ProbabilityImpl;

import java.util.Map;
import java.util.TreeMap;

import static lphy.base.distribution.DistributionConstants.shapeParamName;

/**
 * Discretized Gamma distribution
 */
public class DiscretizedGamma extends ParametricDistribution<Probability> {

    private static final String ncatParamName = "ncat";
    private Value<PositiveReal> shape;
    private Value<PositiveInt> ncat;

    GammaDistribution gammaDistribution;
    double[] rates;

    public DiscretizedGamma(@ParameterInfo(name = shapeParamName,
                                    description = "the shape of the discretized gamma distribution.")
                            Value<PositiveReal> shape,
                            @ParameterInfo(name = ncatParamName,
                                    description = "the number of bins in the discretization.")
                            Value<PositiveInt> ncat) {
        super();
        this.shape = shape;
        this.ncat = ncat;

        constructDistribution(random);
    }

    @Override
    protected void constructDistribution(RandomGenerator random) {
        if (shape == null) throw new IllegalArgumentException("The shape value can't be null!");
        double sh = shape.value().getPrimitive();
        // use code available since apache math 3.1
        gammaDistribution = new GammaDistribution(random, sh, 1.0 / sh, GammaDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);

        rates = new double[ncat.value().getPrimitive()];
    }


    @GeneratorInfo(name = "DiscretizeGamma",
            category = GeneratorCategory.PRIOR, examples = {"gtrGammaCoalescent.lphy","simpleBModelTest.lphy"},
            description = "The discretized gamma probability distribution with mean = 1.")
    public RandomVariable<Probability> sample() {

        double meanRate = 0;
		for (int i = 0; i < rates.length; i++) {
			double q = (2.0 * i + 1.0) / (2.0 * rates.length);
			rates[i] = gammaDistribution.inverseCumulativeProbability(q);
			meanRate += rates[i];
		}
		// renormalise cat rates
		meanRate /= rates.length;
		for (int i = 0; i < rates.length; i++) {
			rates[i] /= meanRate;
		}
        Probability prob = new ProbabilityImpl(rates[random.nextInt(rates.length)]);
		return new RandomVariable<>(null, prob, this);
    }

    public double logDensity(Probability x) {
        //TODO
        throw new UnsupportedOperationException("TODO");
    }

    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(shapeParamName, shape);
            put(ncatParamName, ncat);
        }};
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(shapeParamName)) shape = value;
        else if (paramName.equals(ncatParamName)) ncat = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);

        super.setParam(paramName, value); // constructDistribution
    }

    public Value<PositiveReal> getShape() {
        return shape;
    }

    public Value<PositiveInt> getNcat() {
        return ncat;
    }

}