package lphy.core.distributions;

import lphy.graphicalModel.*;
import org.apache.commons.math3.distribution.MultivariateNormalDistribution;

import java.util.Map;
import java.util.TreeMap;

import static lphy.core.distributions.DistributionConstants.*;

/**
 * Multivariate Normal distribution
 */
public class MVN implements GenerativeDistribution<Double[]> {

    private static final String covariancesParamName = "covariances";
    private Value<Double[]> mean;
    private Value<Double[][]> covariances;

    MultivariateNormalDistribution multivariateNormalDistribution;

    public MVN(@ParameterInfo(name = meanParamName, description = "the mean of the distribution.") Value<Double[]> mean,
               @ParameterInfo(name = covariancesParamName, description = "the variance-covariance matrix of the distribution.") Value<Double[][]> covariances) {

        this.mean = mean;
        if (mean == null) throw new IllegalArgumentException("The means can't be null!");
        this.covariances = covariances;
        if (covariances == null) throw new IllegalArgumentException("The covariances can't be null!");

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
        return new TreeMap<>() {{
            put(meanParamName, mean);
            put(covariancesParamName, covariances);
        }};
    }

    @Override
    public void setParam(String paramName, Value value) {
        switch (paramName) {
            case meanParamName:
                mean = value;
                break;
            case covariancesParamName:
                covariances = value;
                break;
            default:
                throw new RuntimeException("Unrecognised parameter name: " + paramName);
        }
    }

    public String toString() {
        return getName();
    }

}