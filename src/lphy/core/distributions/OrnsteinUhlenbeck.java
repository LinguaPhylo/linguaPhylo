package lphy.core.distributions;

import lphy.graphicalModel.GenerativeDistribution;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.RandomVariable;
import lphy.graphicalModel.Value;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by adru001 on 2/02/20.
 */
public class OrnsteinUhlenbeck implements GenerativeDistribution<Double> {

    protected Value<Double> y0;
    protected Value<Double> time;
    protected Value<Double> diffRate;
    protected Value<Double> theta;
    protected Value<Double> alpha;

    String y0ParamName;
    String timeParamName;
    String diffRateParamName;
    String thetaParamName;
    String alphaParamName;

    public OrnsteinUhlenbeck(@ParameterInfo(name = "y0", description = "the initial value of the continuous trait.") Value<Double> y0,
                   @ParameterInfo(name = "time", description = "the time since the initial value.") Value<Double> time,
                   @ParameterInfo(name = "diffRate", description = "the variance of the underlying Brownian process. This is not the equilibrium variance of the OU process.") Value<Double> diffRate,
                   @ParameterInfo(name = "theta", description = "the 'optimal' value that the long-term process is centered around.") Value<Double> theta,
                   @ParameterInfo(name = "alpha", description = "the drift term that determines the rate of drift towards the optimal value.") Value<Double> alpha
    ) {

        this.y0 = y0;
        this.time = time;
        this.diffRate = diffRate;
        this.theta = theta;
        this.alpha = alpha;

        y0ParamName = getParamName(0);
        timeParamName = getParamName(1);
        diffRateParamName = getParamName(2);
        thetaParamName = getParamName(3);
        alphaParamName = getParamName(4);
    }

    @Override
    public SortedMap<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        map.put(y0ParamName, y0);
        map.put(timeParamName, time);
        map.put(diffRateParamName, diffRate);
        map.put(thetaParamName, theta);
        map.put(alphaParamName, alpha);
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(y0ParamName)) y0 = value;
        else if (paramName.equals(timeParamName)) time = value;
        else if (paramName.equals(diffRateParamName)) diffRate = value;
        else if (paramName.equals(thetaParamName)) theta = value;
        else if (paramName.equals(alphaParamName)) alpha = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);
    }

    public RandomVariable<Double> sample() {
        return new RandomVariable<>("x", sampleNewState(y0.value(), time.value()), this);
    }

    protected double sampleNewState(double initialState, double time) {

        double th = theta.value();
        double a = alpha.value();

        double v = diffRate.value()/(2*a);

        double weight = Math.exp(-a*time);

        double mean = (1.0-weight)*th + weight*initialState;

        double variance = v * (1.0 - Math.exp(-2.0*a*time));

        NormalDistribution distribution = new NormalDistribution(Utils.getRandom(), mean, Math.sqrt(variance));
        return handleBoundaries(distribution.sample());
    }

    protected double handleBoundaries(double rawValue) {
        return rawValue;
    }
}
