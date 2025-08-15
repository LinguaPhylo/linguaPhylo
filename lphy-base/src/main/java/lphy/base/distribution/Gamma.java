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
import org.phylospec.types.NonNegativeReal;
import org.phylospec.types.PositiveReal;
import org.phylospec.types.impl.NonNegativeRealImpl;

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
public class Gamma extends ParametricDistribution<NonNegativeReal> implements GenerativeDistribution1D<NonNegativeReal, Double> {

    private Value<PositiveReal> shape;
    private Value<PositiveReal> scale;

    GammaDistribution gammaDistribution;

    public Gamma(@ParameterInfo(name = shapeParamName, description = "the shape of the distribution.")
                 Value<PositiveReal> shape,
                 @ParameterInfo(name = scaleParamName, description = "the scale of the distribution.")
                 Value<PositiveReal> scale) {
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
        double sh = ValueUtils.doublePrimitiveValue(shape);
        // in case the scale is type integer
        double sc = ValueUtils.doublePrimitiveValue(scale);
        // use code available since apache math 3.1
        gammaDistribution = new GammaDistribution(random, sh, sc, GammaDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
    }

    @GeneratorInfo(name = "Gamma", verbClause = "has", narrativeName = "gamma distribution prior",
            category = GeneratorCategory.PRIOR, examples = {"covidDPG.lphy"},
            description = "The gamma probability distribution.")
    public RandomVariable<NonNegativeReal> sample() {
        // constructDistribution() only required in constructor and setParam
        double x = gammaDistribution.sample();
        NonNegativeReal nonNegativeReal = new NonNegativeRealImpl(x);
        return new RandomVariable<>("x", nonNegativeReal, this);
    }

    @Override
    public double density(NonNegativeReal x) {
        return gammaDistribution.density(x.getPrimitive());
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

    public Value<PositiveReal> getScale() {
        return scale;
    }

    public Value<PositiveReal> getShape() {
        return shape;
    }

    private static final Double[] domainBounds = {0.0, Double.POSITIVE_INFINITY};

    public Double[] getDomainBounds() {
        return domainBounds;
    }
}