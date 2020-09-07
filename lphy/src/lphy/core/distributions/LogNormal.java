package lphy.core.distributions;

import lphy.graphicalModel.*;
import org.apache.commons.math3.distribution.LogNormalDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.*;

import static lphy.graphicalModel.ValueUtils.doubleValue;

/**
 * Created by adru001 on 18/12/19.
 */
public class LogNormal implements GenerativeDistribution1D<Double> {

    private final String meanLogParamName;
    private final String sdLogParamName;
    private Value<Number> M;
    private Value<Number> S;

    LogNormalDistribution logNormalDistribution;

    public LogNormal(@ParameterInfo(name = "meanlog", description = "the mean of the distribution on the log scale.") Value<Number> M,
                     @ParameterInfo(name = "sdlog", description = "the standard deviation of the distribution on the log scale.") Value<Number> S) {

        this.M = M;
        this.S = S;

        meanLogParamName = getParamName(0);
        sdLogParamName = getParamName(1);
    }

    @GeneratorInfo(name="LogNormal", description="The log-normal probability distribution.")
    public RandomVariable<Double> sample() {

        logNormalDistribution = new LogNormalDistribution(doubleValue(M), doubleValue(S));
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

    public Value<Number> getMeanLog() {
        return M;
    }

    public Value<Number> getSDLog() {
        return S;
    }

    private static final Double[] domainBounds = {0.0, Double.POSITIVE_INFINITY};
    public Double[] getDomainBounds() {
        return domainBounds;
    }
}