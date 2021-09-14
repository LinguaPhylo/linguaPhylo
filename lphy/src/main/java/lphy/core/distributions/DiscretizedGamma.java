package lphy.core.distributions;

import lphy.graphicalModel.*;
import org.apache.commons.math3.distribution.GammaDistribution;

import java.util.Map;
import java.util.TreeMap;

import static lphy.graphicalModel.ValueUtils.doubleValue;
import static lphy.core.distributions.DistributionConstants.*;

/**
 * Discretized Gamma distribution
 */
public class DiscretizedGamma implements GenerativeDistribution<Double> {

    private static final String ncatParamName = "ncat";
    private Value<Number> shape;
    private Value<Integer> ncat;

    GammaDistribution gammaDistribution;
    double[] rates;


    public DiscretizedGamma(@ParameterInfo(name = shapeParamName, description = "the shape of the discretized gamma distribution.") Value<Number> shape,
                            @ParameterInfo(name = ncatParamName, description = "the number of bins in the discretization.") Value<Integer> ncat) {

        this.shape = shape;
        if (shape == null) throw new IllegalArgumentException("The shape value can't be null!");
        this.ncat = ncat;

        constructGammaDistribution();
    }

    @GeneratorInfo(name = "DiscretizeGamma", description = "The discretized gamma probability distribution with mean = 1.")
    public RandomVariable<Double> sample() {
        constructGammaDistribution();

        return new RandomVariable<>(null, rates[Utils.getRandom().nextInt(rates.length)], this);
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

    @Override
    public void setParam(String paramName, Value value) {

        if (shapeParamName.equals(paramName)) {
            shape = value;
        } else if (ncatParamName.equals(paramName)) {
            ncat = value;
        } else {
            throw new RuntimeException("Unrecognised parameter name: " + paramName);
        }

        constructGammaDistribution();
    }

    public Value<Number> getShape() {
        return shape;
    }

    public Value<Integer> getNcat() {
        return ncat;
    }

    public String toString() {
        return getName();
    }


    private void constructGammaDistribution() {
        double sh = doubleValue(shape);

        gammaDistribution = new GammaDistribution(sh, 1.0 / sh);

        rates = new double[ncat.value()];

        for (int i = 0; i < rates.length; i++) {
            double q = (2.0 * i + 1.0) / (2.0 * rates.length);
            rates[i] = gammaDistribution.inverseCumulativeProbability(q);
        }
    }




}