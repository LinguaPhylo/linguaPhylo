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
public class DiscretizedGamma implements GenerativeDistribution<Double[]> {

    private static final String ncatParamName = "ncat";
    private Value<Number> shape;
    private Value<Integer> ncat;
    private Value<Integer> reps;

    GammaDistribution gammaDistribution;
    double[] rates;


    public DiscretizedGamma(@ParameterInfo(name = shapeParamName, description = "the shape of the discretized gamma distribution.") Value<Number> shape,
                            @ParameterInfo(name = ncatParamName, description = "the number of bins in the discretization.") Value<Integer> ncat,
                            @ParameterInfo(name = repsParamName, description = "the number of iid samples to produce.", optional = true) Value<Integer> reps) {

        this.shape = shape;
        if (shape == null) throw new IllegalArgumentException("The shape value can't be null!");
        this.ncat = ncat;
        this.reps = reps;

        constructGammaDistribution();
    }

    @GeneratorInfo(name = "DiscretizeGamma", description = "The discretized gamma probability distribution with mean = 1.")
    public RandomVariable<Double[]> sample() {
        constructGammaDistribution();

        int n = 1;
        if (reps != null) n = reps.value();

        Double[] x = new Double[n];
        for (int i = 0; i < x.length; i++) {
            x[i] = rates[Utils.getRandom().nextInt(rates.length)];
        }
        return new RandomVariable<>("x", x, this);
    }

    public double logDensity(Double[] x) {
        //TODO
        throw new UnsupportedOperationException("TODO");
    }

    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(shapeParamName, shape);
            put(ncatParamName, ncat);
            put(repsParamName, reps);
        }};
    }

    @Override
    public void setParam(String paramName, Value value) {

        switch (paramName) {
            case shapeParamName:
                shape = value;
                break;
            case ncatParamName:
                ncat = value;
                break;
            case repsParamName:
                reps = value;
                break;
            default:
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