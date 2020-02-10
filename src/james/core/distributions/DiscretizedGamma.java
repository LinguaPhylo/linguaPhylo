package james.core.distributions;

import james.graphicalModel.*;
import org.apache.commons.math3.distribution.GammaDistribution;

import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Discretized Gamma distribution
 */
public class DiscretizedGamma implements GenerativeDistribution<Double> {

    private final String shapeParamName;
    private final String ncatParamName;
    private Value<Double> shape;
    private Value<Integer> ncat;

    private Random random;

    GammaDistribution gammaDistribution;
    double[] rates;

    public DiscretizedGamma(@ParameterInfo(name = "shape", description = "the shape of the discretized gamma distribution.") Value<Double> shape,
                            @ParameterInfo(name = "ncat", description = "the number of bins in the discretization.") Value<Integer> ncat) {

        this.shape = shape;
        if (shape == null) throw new IllegalArgumentException("The shape value can't be null!");
        this.ncat = ncat;
        random = Utils.getRandom();

        shapeParamName = getParamName(0);
        ncatParamName = getParamName(1);

        constructGammaDistribution();
    }

    @GenerativeDistributionInfo(description = "The normal probability distribution.")
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
        map.put(ncatParamName, ncat);
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(shapeParamName)) shape = value;
        else if (paramName.equals(ncatParamName)) ncat = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);

        constructGammaDistribution();
    }

    private void constructGammaDistribution() {
        // in case the shape is type integer
        double sh = ((Number) shape.value()).doubleValue();

        gammaDistribution = new GammaDistribution(sh, 1.0/sh);

        rates = new double[ncat.value()];

        for (int i = 0; i < rates.length; i++) {
            double q = (2.0 * i + 1.0) / (2.0 * rates.length);
            rates[i] = gammaDistribution.inverseCumulativeProbability(q);
        }
    }

    public String toString() {
        return getName();
    }

}