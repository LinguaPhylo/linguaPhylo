package lphy.core.lightweight.distributions;

import lphy.core.distributions.Utils;
import lphy.core.lightweight.LightweightGenerativeDistribution;
import lphy.graphicalModel.*;
import org.apache.commons.math3.distribution.GammaDistribution;

/**
 * Discretized Gamma distribution
 */
public class DiscretizedGamma implements LightweightGenerativeDistribution<Double[]> {

    private Double shape;
    private Integer ncat;
    private Integer reps;

    GammaDistribution gammaDistribution;
    double[] rates;


    public DiscretizedGamma(@ParameterInfo(name = "shape", description = "the shape of the discretized gamma distribution.") Double shape,
                            @ParameterInfo(name = "ncat", description = "the number of bins in the discretization.") Integer ncat,
                            @ParameterInfo(name = "reps", description = "the number of iid samples to produce.", optional = true) Integer reps) {

        this.shape = shape;
        if (shape == null) throw new IllegalArgumentException("The shape value can't be null!");
        this.ncat = ncat;
        this.reps = reps;

        constructGammaDistribution();
    }

    @GeneratorInfo(name="G", description = "The discretized gamma probability distribution with mean = 1.")
    public Double[] sample() {
        constructGammaDistribution();

        int n = 1;
        if (reps != null) n = reps;

        Double[] x = new Double[n];
        for (int i = 0; i < x.length; i++){
            x[i] = rates[Utils.getRandom().nextInt(rates.length)];
        }
        return x;
    }

    public double logDensity(Double[] x) {
        //TODO
        throw new UnsupportedOperationException("TODO");
    }

    public Double getShape() {
        return shape;
    }

    public void setShape(Double shape) {
        this.shape = shape;
    }

    public Integer getNcat() {
        return ncat;
    }

    public void setNcat(Integer ncat) {
        this.ncat = ncat;
    }

    public Integer getReps() {
        return reps;
    }

    public void setReps(Integer reps) {
        this.reps = reps;
    }

    private void constructGammaDistribution() {
        // in case the shape is type integer
        double sh = ((Number) shape).doubleValue();

        gammaDistribution = new GammaDistribution(sh, 1.0/sh);

        rates = new double[ncat];

        for (int i = 0; i < rates.length; i++) {
            double q = (2.0 * i + 1.0) / (2.0 * rates.length);
            rates[i] = gammaDistribution.inverseCumulativeProbability(q);
        }
    }

    public String toString() {
        return getName();
    }

}