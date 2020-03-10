package lphy.core.distributions;

import lphy.graphicalModel.*;
import org.apache.commons.math3.distribution.GammaDistribution;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Discretized Gamma distribution
 */
public class DiscretizedGamma implements GenerativeDistribution<Double[]> {

    private final String shapeParamName;
    private final String ncatParamName;
    private final String repsParamName;
    private Value<Double> shape;
    private Value<Integer> ncat;
    private Value<Integer> reps;

    GammaDistribution gammaDistribution;
    double[] rates;


    public DiscretizedGamma(@ParameterInfo(name = "shape", description = "the shape of the discretized gamma distribution.") Value<Double> shape,
                            @ParameterInfo(name = "ncat", description = "the number of bins in the discretization.") Value<Integer> ncat,
                            @ParameterInfo(name = "reps", description = "the number of iid samples to produce.", optional = true) Value<Integer> reps) {

        this.shape = shape;
        if (shape == null) throw new IllegalArgumentException("The shape value can't be null!");
        this.ncat = ncat;
        this.reps = reps;

        shapeParamName = getParamName(0);
        ncatParamName = getParamName(1);
        repsParamName = getParamName(2);

        constructGammaDistribution();
    }

    @GeneratorInfo(name="G", description = "The discretized gamma probability distribution with mean = 1.")
    public RandomVariable<Double[]> sample() {
        constructGammaDistribution();

        int n = 1;
        if (reps != null) n = reps.value();

        Double[] x = new Double[n];
        for (int i = 0; i < x.length; i++){
            x[i] = rates[Utils.getRandom().nextInt(rates.length)];
        }
        return new RandomVariable<>("x", x, this);
    }

    public double logDensity(Double[] x) {
        //TODO
        throw new UnsupportedOperationException("TODO");
    }

    public Map<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        map.put(shapeParamName, shape);
        map.put(ncatParamName, ncat);
        map.put(repsParamName, reps);
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(shapeParamName)) shape = value;
        else if (paramName.equals(ncatParamName)) ncat = value;
        else if (paramName.equals(repsParamName)) reps = value;
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