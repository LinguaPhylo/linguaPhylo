package lphy.base.distribution;

import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.annotation.ParameterInfo;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.TreeMap;

import static lphy.base.distribution.DistributionConstants.alphaParamName;

/**
 * Created by Alexei Drummond on 2/02/20.
 */
public class OrnsteinUhlenbeck extends ParametricDistribution<Double> {

    protected Value<Double> y0;
    protected Value<Double> time;
    protected Value<Double> diffRate;
    protected Value<Double> theta;
    protected Value<Double> alpha;

    public static final String y0ParamName = "y0";
    public static final String timeParamName = "time";
    public static final String diffRateParamName = "diffRate";
    public static final String thetaParamName = "theta";

    public OrnsteinUhlenbeck(@ParameterInfo(name = y0ParamName, description = "the initial value of the continuous trait.") Value<Double> y0,
                             @ParameterInfo(name = timeParamName, description = "the time since the initial value.") Value<Double> time,
                             @ParameterInfo(name = diffRateParamName, description = "the variance of the underlying Brownian process. This is not the equilibrium variance of the OU process.") Value<Double> diffRate,
                             @ParameterInfo(name = thetaParamName, description = "the 'optimal' value that the long-term process is centered around.") Value<Double> theta,
                             @ParameterInfo(name = alphaParamName, description = "the drift term that determines the rate of drift towards the optimal value.") Value<Double> alpha) {
        super();
        this.y0 = y0;
        this.time = time;
        this.diffRate = diffRate;
        this.theta = theta;
        this.alpha = alpha;

//        constructDistribution(random);
    }

    @Override
    protected void constructDistribution(RandomGenerator random) {
    }


    public RandomVariable<Double> sample() {
        return new RandomVariable<>(null, sampleNewState(y0.value(), time.value()), this);
    }

    protected double sampleNewState(double initialState, double time) {

        double th = theta.value();
        double a = alpha.value();

        double v = diffRate.value() / (2 * a);

        double weight = Math.exp(-a * time);

        double mean = (1.0 - weight) * th + weight * initialState;

        double variance = v * (1.0 - Math.exp(-2.0 * a * time));

        NormalDistribution distribution = new NormalDistribution(random, mean, Math.sqrt(variance),
                NormalDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
        return handleBoundaries(distribution.sample());
    }

    protected double handleBoundaries(double rawValue) {
        return rawValue;
    }

    @Override
    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(y0ParamName, y0);
            put(timeParamName, time);
            put(diffRateParamName, diffRate);
            put(thetaParamName, theta);
            put(alphaParamName, alpha);
        }};
    }

}
