package lphy.core.distributions;

import lphy.graphicalModel.*;
import org.apache.commons.math3.distribution.LogNormalDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.*;

/**
 * Created by adru001 on 18/12/19.
 */
public class LogNormal implements GenerativeDistribution<Double> {

    private final String meanLogParamName;
    private final String sdLogParamName;
    private Value<Double> M;
    private Value<Double> S;

    private RandomGenerator random;

    LogNormalDistribution logNormalDistribution;

    public LogNormal(@ParameterInfo(name = "meanlog", description = "the mean of the distribution on the log scale.") Value<Double> M,
                     @ParameterInfo(name = "sdlog", description = "the standard deviation of the distribution on the log scale.") Value<Double> S) {

        this.M = M;
        this.S = S;
        this.random = Utils.getRandom();

        meanLogParamName = getParamName(0);
        sdLogParamName = getParamName(1);
    }

    @GenerativeDistributionInfo(name="LogNormal", description="The log-normal probability distribution.")
    public RandomVariable<Double> sample() {

        logNormalDistribution = new LogNormalDistribution(M.value(), S.value());
        return new RandomVariable<>("x",  logNormalDistribution.sample(), this);
    }

    public double logDensity(Double x) {

        return logNormalDistribution.logDensity(x);
    }

    public Map<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        map.put(meanLogParamName, M);
        map.put(sdLogParamName, S);
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(meanLogParamName)) M = value;
        else if (paramName.equals(sdLogParamName)) S = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);
    }

    public String toString() {
        return getName();
    }

}