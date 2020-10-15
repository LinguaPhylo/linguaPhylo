package lphy.core.distributions;

import lphy.graphicalModel.*;
import org.apache.commons.math3.distribution.LogNormalDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.TreeMap;

import static lphy.core.distributions.LogNormal.*;
import static lphy.core.distributions.DistributionConstants.nParamName;

/**
 * Created by adru001 on 18/12/19.
 */
public class LogNormalMulti implements GenerativeDistribution<Double[]> {

    private Value<Double> M;
    private Value<Double> S;
    private Value<Integer> n;

    LogNormalDistribution logNormalDistribution;

    public LogNormalMulti(@ParameterInfo(name = meanLogParamName, description = "the mean of the distribution on the log scale.") Value<Double> M,
                          @ParameterInfo(name = sdLogParamName, description = "the standard deviation of the distribution on the log scale.") Value<Double> S,
                          @ParameterInfo(name = nParamName, description = "the dimension of the return.") Value<Integer> n) {

        this.M = M;
        this.S = S;
        this.n = n;
    }

    @GeneratorInfo(name = "LogNormal", description = "The log-normal probability distribution.")
    public RandomVariable<Double[]> sample() {

        logNormalDistribution = new LogNormalDistribution(M.value(), S.value());
        Double[] result = new Double[n.value()];
        for (int i = 0; i < result.length; i++) {
            result[i] = logNormalDistribution.sample();
        }

        return new RandomVariable<>("x", result, this);
    }

    public double logDensity(Double[] x) {

        double logDensity = 0;
        for (Double aDouble : x) {
            logDensity += logNormalDistribution.logDensity(aDouble);
        }
        return logDensity;
    }

    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(meanLogParamName, M);
            put(sdLogParamName, S);
            put(nParamName, n);
        }};
    }

    @Override
    public void setParam(String paramName, Value value) {
        switch (paramName) {
            case meanLogParamName:
                M = value;
                break;
            case sdLogParamName:
                S = value;
                break;
            case nParamName:
                n = value;
                break;
            default:
                throw new RuntimeException("Unrecognised parameter name: " + paramName);
        }
    }

    public String toString() {
        return getName();
    }

    public Value<Double> getMeanLog() {
        return M;
    }

    public Value<Double> getSDLog() {
        return S;
    }
}