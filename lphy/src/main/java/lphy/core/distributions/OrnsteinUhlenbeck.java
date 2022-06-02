package lphy.core.distributions;

import lphy.graphicalModel.GenerativeDistribution;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.RandomVariable;
import lphy.graphicalModel.Value;
import lphy.util.RandomUtils;
import org.apache.commons.math3.distribution.NormalDistribution;

import java.util.Map;
import java.util.TreeMap;

import static lphy.core.distributions.DistributionConstants.alphaParamName;

/**
 * Created by Alexei Drummond on 2/02/20.
 */
public class OrnsteinUhlenbeck implements GenerativeDistribution<Double> {

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

        this.y0 = y0;
        this.time = time;
        this.diffRate = diffRate;
        this.theta = theta;
        this.alpha = alpha;
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

    @Override
    public void setParam(String paramName, Value value) {
        switch (paramName) {
            case y0ParamName:
                y0 = value;
                break;
            case timeParamName:
                time = value;
                break;
            case diffRateParamName:
                diffRate = value;
                break;
            case thetaParamName:
                theta = value;
                break;
            case alphaParamName:
                alpha = value;
                break;
            default:
                throw new RuntimeException("Unrecognised parameter name: " + paramName);
        }
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

        NormalDistribution distribution = new NormalDistribution(RandomUtils.getRandom(), mean, Math.sqrt(variance));
        return handleBoundaries(distribution.sample());
    }

    protected double handleBoundaries(double rawValue) {
        return rawValue;
    }
}
