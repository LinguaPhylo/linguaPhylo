package lphy.base.distribution;

import lphy.core.model.GenerativeDistribution1D;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.ValueUtils;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.TreeMap;

import static lphy.base.distribution.DistributionConstants.scaleParamName;
import static lphy.base.distribution.DistributionConstants.shapeParamName;

/**
 * Gamma distribution prior.
 * @see GammaDistribution
 * @author Alexei Drummond
 * @author Walter Xie
 */
public class Gamma extends ParametricDistribution<Double> implements GenerativeDistribution1D<Double> {

    private Value<Number> shape;
    private Value<Number> scale;

    GammaDistribution gammaDistribution;

    public Gamma(@ParameterInfo(name = shapeParamName, description = "the shape of the distribution.") Value<Number> shape,
                 @ParameterInfo(name = scaleParamName, description = "the scale of the distribution.") Value<Number> scale) {
        super();
        this.shape = shape;
        this.scale = scale;

        constructDistribution(random);
    }

    @Override
    protected void constructDistribution(RandomGenerator random) {
        if (shape == null) throw new IllegalArgumentException("The shape value can't be null!");
        if (scale == null) throw new IllegalArgumentException("The scale value can't be null!");
        // in case the shape is type integer
        double sh = ValueUtils.doubleValue(shape);
        // in case the scale is type integer
        double sc = ValueUtils.doubleValue(scale);
        // use code available since apache math 3.1
        gammaDistribution = new GammaDistribution(random, sh, sc, GammaDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
    }

    @GeneratorInfo(name = "Gamma", verbClause = "has", narrativeName = "gamma distribution prior",
            category = GeneratorCategory.PRIOR, examples = {"covidDPG.lphy"},
            description = "The gamma probability distribution.")
    public RandomVariable<Double> sample() {
        // constructDistribution() only required in constructor and setParam
        double x = gammaDistribution.sample();
        return new RandomVariable<>("x", x, this);
    }

    @Override
    public double density(Double x) {
        return gammaDistribution.density(x);
    }

    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(shapeParamName, shape);
            put(scaleParamName, scale);
        }};
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(shapeParamName)) shape = value;
        else if (paramName.equals(scaleParamName)) scale = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);

        super.setParam(paramName, value); // constructDistribution
    }

    public Value<Number> getScale() {
        return scale;
    }

    public Value<Number> getShape() {
        return shape;
    }

    private static final Double[] domainBounds = {0.0, Double.POSITIVE_INFINITY};

    public Double[] getDomainBounds() {
        return domainBounds;
    }
}