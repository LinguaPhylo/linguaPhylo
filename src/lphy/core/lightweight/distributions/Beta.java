package lphy.core.lightweight.distributions;

import lphy.core.lightweight.LightweightGenerativeDistribution;
import lphy.graphicalModel.*;
import org.apache.commons.math3.distribution.BetaDistribution;

/**
 * Created by adru001 on 18/12/19.
 */
public class Beta implements LightweightGenerativeDistribution<Double> {

    private Double alpha;
    private Double beta;
    BetaDistribution betaDistribution;

    public Beta(@ParameterInfo(name="alpha", description="the first shape parameter.") Double alpha,
                @ParameterInfo(name="beta", description="the second shape parameter.") Double beta) {
        this.alpha = alpha;
        this.beta = beta;
        setup();
    }

    void setup() { betaDistribution = new BetaDistribution(alpha, beta); }

    public Double getAlpha() {
        return alpha;
    }
    public Double getBeta() {
        return beta;
    }

    public void setAlpha(Double alpha) { this.alpha = alpha; setup(); }
    public void setBeta(Double beta) { this.beta = beta; setup(); }

    @GeneratorInfo(name="Beta", description="The beta probability distribution.")
    public Double sample() { return betaDistribution.sample(); }
}