package lphy.core.distributions;

import lphy.graphicalModel.*;
import org.apache.commons.math3.distribution.LogNormalDistribution;

import java.util.*;

import static lphy.graphicalModel.ValueUtils.doubleValue;

/**
 * Created by adru001 on 18/12/19.
 */
public class LogNormal implements GenerativeDistribution1D<Double> {

    public static final String meanLogParamName = "meanlog";
    public static final String sdLogParamName = "sdlog";
    private Value<Number> M;
    private Value<Number> S;

    LogNormalDistribution logNormalDistribution;

    public LogNormal(@ParameterInfo(name = meanLogParamName, narrativeName = "mean in log space", description = "the mean of the distribution on the log scale.") Value<Number> M,
                     @ParameterInfo(name = sdLogParamName, narrativeName = "standard deviation in log space", description = "the standard deviation of the distribution on the log scale.") Value<Number> S) {

        this.M = M;
        this.S = S;
    }

    @GeneratorInfo(name = "LogNormal", verbClause = "has", narrativeName = "log-normal distribution prior", description = "The log-normal probability distribution.")
    public RandomVariable<Double> sample() {

        logNormalDistribution = new LogNormalDistribution(doubleValue(M), doubleValue(S));
        return new RandomVariable<>(null, logNormalDistribution.sample(), this);
    }

    public double logDensity(Double x) {

        return logNormalDistribution.logDensity(x);
    }

    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(meanLogParamName, M);
            put(sdLogParamName, S);
        }};
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