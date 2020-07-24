package lphy.core.distributions;

import beast.core.BEASTInterface;
import beast.core.parameter.RealParameter;
import lphy.beast.BEASTContext;
import lphy.graphicalModel.*;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Gamma distribution
 */
public class Gamma implements GenerativeDistribution<Double> {

    private final String shapeParamName;
    private final String scaleParamName;
    private Value<Double> shape;
    private Value<Double> scale;

    private RandomGenerator random;

    GammaDistribution gammaDistribution;

    public Gamma(@ParameterInfo(name = "shape", description = "the shape of the distribution.") Value<Double> shape,
                 @ParameterInfo(name = "scale", description = "the scale of the distribution.") Value<Double> scale) {

        this.shape = shape;
        if (shape == null) throw new IllegalArgumentException("The shape value can't be null!");
        this.scale = scale;
        if (scale == null) throw new IllegalArgumentException("The scale value can't be null!");
        random = Utils.getRandom();

        shapeParamName = getParamName(0);
        scaleParamName = getParamName(1);

        constructGammaDistribution();
    }

    @GeneratorInfo(name="Gamma", description = "The gamma probability distribution.")
    public RandomVariable<Double> sample() {
        constructGammaDistribution();
        double x = gammaDistribution.sample();
        return new RandomVariable<>("x", x, this);
    }

    @Override
    public double density(Double x) {
        return gammaDistribution.density(x);
    }

    public Map<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        map.put(shapeParamName, shape);
        map.put(scaleParamName, scale);
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(shapeParamName)) shape = value;
        else if (paramName.equals(scaleParamName)) scale = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);

        constructGammaDistribution();
    }

    private void constructGammaDistribution() {
        // in case the shape is type integer
        double sh = ((Number) shape.value()).doubleValue();

        // in case the scale is type integer
        double sc = ((Number) scale.value()).doubleValue();

        gammaDistribution = new GammaDistribution(sh, sc);
    }

    public String toString() {
        return getName();
    }

    public BEASTInterface toBEAST(BEASTInterface value, Map beastObjects) {
        beast.math.distributions.Gamma gammaDistribution = new beast.math.distributions.Gamma();
        gammaDistribution.setInputValue("shape", beastObjects.get(getParams().get(shapeParamName)));
        gammaDistribution.setInputValue("scale", beastObjects.get(getParams().get(scaleParamName)));
        gammaDistribution.initAndValidate();
        return BEASTContext.createPrior(gammaDistribution, (RealParameter)value);
    }

}