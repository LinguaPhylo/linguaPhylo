package lphy.core.distributions;

import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.RandomVariable;
import lphy.graphicalModel.Value;
import org.apache.commons.math3.distribution.MultivariateNormalDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.TreeMap;

import static lphy.core.distributions.DistributionConstants.meanParamName;

/**
 * Multivariate Normal distribution.
 */
public class MVN extends ParametricDistribution<Double[]> {

    private static final String covariancesParamName = "covariances";
    private Value<Double[]> mean;
    private Value<Double[][]> covariances;

    MultivariateNormalDistribution multivariateNormalDistribution;

    public MVN(@ParameterInfo(name = meanParamName, description = "the mean of the distribution.") Value<Double[]> mean,
               @ParameterInfo(name = covariancesParamName, description = "the variance-covariance matrix of the distribution.") Value<Double[][]> covariances) {
        super();
        this.mean = mean;
        this.covariances = covariances;

        constructDistribution(random);
    }

    @Override
    protected void constructDistribution(RandomGenerator random) {
        if (mean == null) throw new IllegalArgumentException("The means can't be null!");
        if (covariances == null) throw new IllegalArgumentException("The covariances can't be null!");

        double[] means = new double[mean.value().length];
        double[][] cv = new double[covariances.value().length][covariances.value().length];
        for (int i = 0; i < means.length; i++) {
            means[i] = mean.value()[i];
            for (int j = 0; j < means.length; j++) {
                cv[i][j] = this.covariances.value()[i][j];
            }
        }
        multivariateNormalDistribution = new MultivariateNormalDistribution(random, means, cv);
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

}