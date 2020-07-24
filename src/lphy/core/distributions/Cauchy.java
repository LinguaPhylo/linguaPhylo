package lphy.core.distributions;

import beast.core.BEASTInterface;
import lphy.graphicalModel.*;
import org.apache.commons.math3.distribution.CauchyDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Gamma distribution
 */
public class Cauchy implements GenerativeDistribution<Double> {

    private final String meanParamName;
    private final String scaleParamName;
    private Value<Double> mean;
    private Value<Double> scale;

    private RandomGenerator random;

    CauchyDistribution cauchyDistribution;

    public Cauchy(@ParameterInfo(name = "mean", description = "the mean of the Cauchy distribution.", type=Double.class) Value<Double> mean,
                  @ParameterInfo(name = "scale", description = "the scale of the Cauchy distribution.", type=Double.class) Value<Double> scale) {

        this.mean = mean;
        if (mean == null) throw new IllegalArgumentException("The mean value can't be null!");
        this.scale = scale;
        if (scale == null) throw new IllegalArgumentException("The scale value can't be null!");
        random = Utils.getRandom();

        meanParamName = getParamName(0);
        scaleParamName = getParamName(1);

        constructCauchyDistribution();
    }

    @GeneratorInfo(name="Gamma", description = "The gamma probability distribution.")
    public RandomVariable<Double> sample() {
        constructCauchyDistribution();
        double x = cauchyDistribution.sample();
        return new RandomVariable<>("x", x, this);
    }

    @Override
    public double density(Double x) {
        return cauchyDistribution.density(x);
    }

    public Map<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        map.put(meanParamName, mean);
        map.put(scaleParamName, scale);
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(meanParamName)) mean = value;
        else if (paramName.equals(scaleParamName)) scale = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);

        constructCauchyDistribution();
    }

    private void constructCauchyDistribution() {
        // in case the shape is type integer
        double mean = ((Number) this.mean.value()).doubleValue();

        // in case the scale is type integer
        double sc = ((Number) scale.value()).doubleValue();

        cauchyDistribution = new CauchyDistribution(mean, sc);
    }

    public String toString() {
        return getName();
    }


    public BEASTInterface toBEAST(BEASTInterface value, Map beastObjects) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + ".toBEAST not implemented yet!");
    }
}