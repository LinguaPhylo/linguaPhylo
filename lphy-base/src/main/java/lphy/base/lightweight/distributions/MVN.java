package lphy.base.lightweight.distributions;

import lphy.base.lightweight.LGenerativeDistribution;
import lphy.core.graphicalmodel.components.GeneratorInfo;
import lphy.core.graphicalmodel.components.ParameterInfo;
import org.apache.commons.math3.distribution.MultivariateNormalDistribution;

/**
 * Multivariate Normal distribution
 */
public class MVN implements LGenerativeDistribution<Double[]> {

    private Double[] mean;
    private Double[][] covariances;

    MultivariateNormalDistribution multivariateNormalDistribution;

    public MVN(@ParameterInfo(name = "mean", description = "the mean of the distribution.") Double[] mean,
               @ParameterInfo(name = "covariances", description = "the variance-covariance matrix of the distribution.") Double[][] covariances) {

        this.mean = mean;
        if (mean == null) throw new IllegalArgumentException("The means can't be null!");
        this.covariances = covariances;
        if (covariances == null) throw new IllegalArgumentException("The covariances can't be null!");

        constructMVNDistribution();
    }

    @GeneratorInfo(name="MVN", description="The multivariate normal probability distribution.")
    public Double[] sample() {

        double[] sample = multivariateNormalDistribution.sample();
        Double[] result= new Double[sample.length];
        for (int i = 0; i < sample.length; i++) {
            result[i] = sample[i];
        }

        return result;
    }

    @Override
    public double density(Double[] x) {

        double[] xx = new double[mean.length];
        for (int i = 0; i < x.length; i++) {
            xx[i] = x[i];    
        }

        return multivariateNormalDistribution.density(xx);
    }

    private void constructMVNDistribution() {
        double[] means = new double[mean.length];
        double[][] cv = new double[covariances.length][covariances.length];
        for (int i = 0; i < means.length; i++) {
            means[i] = mean[i];
            for (int j = 0; j < means.length; j++) {
                cv[i][j] = this.covariances[i][j];
            }
        }
        multivariateNormalDistribution = new MultivariateNormalDistribution(means,cv);
    }

    public void setMean(Double[] mean) {
        this.mean = mean;
    }

    public void setCovariances(Double[][] covariances) {
        this.covariances = covariances;
    }

    public Double[] getMean() {
        return mean;
    }

    public Double[][] getCovariances() {
        return covariances;
    }

    public String toString() {
        return getName();
    }

}