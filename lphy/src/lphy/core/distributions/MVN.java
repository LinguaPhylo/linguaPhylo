package lphy.core.distributions;

import lphy.graphicalModel.*;
import org.apache.commons.math3.distribution.MultivariateNormalDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Multivariate Normal distribution
 */
public class MVN implements GenerativeDistribution<Double[]> {

    private final String meanParamName;
    private final String sdParamName;
    private Value<Double[]> mean;
    private Value<Double[][]> covariances;

    private RandomGenerator random;

    MultivariateNormalDistribution multivariateNormalDistribution;

    public MVN(@ParameterInfo(name = "mean", description = "the mean of the distribution.") Value<Double[]> mean,
               @ParameterInfo(name = "covariances", description = "the variance-covariance matrix of the distribution.") Value<Double[][]> covariances) {

        this.mean = mean;
        if (mean == null) throw new IllegalArgumentException("The means can't be null!");
        this.covariances = covariances;
        if (covariances == null) throw new IllegalArgumentException("The covariances can't be null!");
        random = Utils.getRandom();

        meanParamName = getParamName(0);
        sdParamName = getParamName(1);

        double[] means = new double[mean.value().length];
        double[][] cv = new double[covariances.value().length][covariances.value().length];
        for (int i = 0; i < means.length; i++) {
            means[i] = mean.value()[i];
            for (int j = 0; j < means.length; j++) {
                cv[i][j] = this.covariances.value()[i][j];
            }
        }
        multivariateNormalDistribution = new MultivariateNormalDistribution(means,cv);

    }

    @GeneratorInfo(name="MVN", description="The normal probability distribution.")
    public RandomVariable<Double[]> sample() {

        double[] sample = multivariateNormalDistribution.sample();
        Double[] result= new Double[sample.length];
        for (int i = 0; i < sample.length; i++) {
            result[i] = sample[i];
        }

        return new RandomVariable<>("X", result, this);
    }

    @Override
    public double density(Double[] x) {

        double[] xx = new double[mean.value().length];
        for (int i = 0; i < x.length; i++) {
            xx[i] = x[i];    
        }

        return multivariateNormalDistribution.density(xx);
    }

    public Map<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        map.put(meanParamName, mean);
        map.put(sdParamName, covariances);
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(meanParamName)) mean = value;
        else if (paramName.equals(sdParamName)) covariances = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);
    }

    public String toString() {
        return getName();
    }

}