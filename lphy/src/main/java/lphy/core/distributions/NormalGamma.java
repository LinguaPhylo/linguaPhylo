package lphy.core.distributions;

import lphy.graphicalModel.*;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.TreeMap;

import static lphy.core.distributions.DistributionConstants.*;
import static lphy.graphicalModel.ValueUtils.doubleValue;
import static org.apache.commons.math3.distribution.GammaDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY;

/**
 * Normal-gamma distribution
 */
public class NormalGamma extends PriorDistributionGenerator<Double[]> {

    private Value<Number> shape;
    private Value<Number> scale;
    private Value<Number> mean;
    private Value<Number> precision;

    GammaDistribution gammaDistribution;
    NormalDistribution normalDistribution;

    static final String precisionParamName = "precision";

    public NormalGamma(@ParameterInfo(name = shapeParamName, description = "the shape of the distribution.") Value<Number> shape,
                       @ParameterInfo(name = scaleParamName, description = "the scale of the distribution.") Value<Number> scale,
                       @ParameterInfo(name = meanParamName, description = "the mean of the distribution.") Value<Number> mean,
                       @ParameterInfo(name = precisionParamName, narrativeName = "precision", description = "the standard deviation of the distribution.") Value<Number> precision) {
        super();
        this.shape = shape;
        this.scale = scale;

        this.mean = mean;
        this.precision = precision;

        constructDistribution(random);
    }

    @GeneratorInfo(name = "NormalGamma", verbClause = "has", narrativeName = "normal-gamma prior",
            category = GeneratorCategory.PRIOR, examples = {"simplePhyloBrownian.lphy","simplePhyloOU.lphy"},
            description = "The normal-gamma probability distribution.")
    public RandomVariable<Double[]> sample() {
        double T = gammaDistribution.sample();
        double x = normalDistribution.sample();
        return new RandomVariable<>(null, new Double[] {x, T}, this);
    }

    @Override
    protected void constructDistribution(RandomGenerator random) {
        if (mean == null) throw new IllegalArgumentException("The mean value can't be null!");
        if (precision == null) throw new IllegalArgumentException("The precision value can't be null!");

        double m = doubleValue(mean);
        double sh = doubleValue(shape);
        double sc = doubleValue(scale);
        double lambda = doubleValue(precision);

        gammaDistribution = new GammaDistribution(random, sh, sc, DEFAULT_INVERSE_ABSOLUTE_ACCURACY);

        double T = gammaDistribution.sample();
        normalDistribution = new NormalDistribution(random, m, lambda * T,
                org.apache.commons.math3.distribution.NormalDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
    }

    @Override
    public double density(Double[] x) {
        return gammaDistribution.density(x[0]) * normalDistribution.density(x[1]);
    }

    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(shapeParamName, shape);
            put(scaleParamName, scale);
            put(meanParamName, mean);
            put(precisionParamName, precision);
        }};
    }

    public Value<Number> getMean() {
        return mean;
    }

    public Value<Number> getPrecision() {
        return precision;
    }
}