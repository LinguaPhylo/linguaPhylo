package lphy.base.lightweight.distributions;

import lphy.base.lightweight.LGenerativeDistribution;
import lphy.core.model.component.GeneratorInfo;
import lphy.core.model.component.ParameterInfo;
import org.apache.commons.math3.distribution.BetaDistribution;

/**
 * Created by Alexei Drummond on 18/12/19.
 */
public class Beta implements LGenerativeDistribution<Double> {

    private Number alpha;
    private Number beta;
    BetaDistribution betaDistribution;

    public Beta(@ParameterInfo(name="alpha", description="the first shape parameter.") Number alpha,
                @ParameterInfo(name="beta", description="the second shape parameter.") Number beta) {
        this.alpha = alpha;
        this.beta = beta;
        setup();
    }

    void setup() { betaDistribution = new BetaDistribution(alpha.doubleValue(), beta.doubleValue()); }

    public Number getAlpha() {
        return alpha;
    }
    public Number getBeta() {
        return beta;
    }

    public void setAlpha(Number alpha) { this.alpha = alpha; setup(); }
    public void setBeta(Number beta) { this.beta = beta; setup(); }

    @GeneratorInfo(name="Beta", description="The beta probability distribution.")
    public Double sample() { return betaDistribution.sample(); }
}