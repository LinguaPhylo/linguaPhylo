package james.core.distributions;

import james.graphicalModel.*;
import org.apache.commons.math3.distribution.LogNormalDistribution;

import java.util.*;

/**
 * Created by adru001 on 18/12/19.
 */
public class LogNormal implements GenerativeDistribution<Double> {

    private final String meanLogParamName;
    private final String sdLogParamName;
    private Value<Double> M;
    private Value<Double> S;

    private Random random;

    LogNormalDistribution logNormalDistribution;

    public LogNormal(@ParameterInfo(name = "meanlog", description = "the mean of the distribution on the log scale.") Value<Double> M,
                     @ParameterInfo(name = "sdlog", description = "the standard deviation of the distribution on the log scale.") Value<Double> S,
                     Random random) {

        this.M = M;
        this.S = S;
        this.random = random;

        meanLogParamName = getParamName(0);
        sdLogParamName = getParamName(1);
    }

    @GenerativeDistributionInfo(description="The log-normal probability distribution.")
    public RandomVariable<Double> sample() {

        logNormalDistribution = new LogNormalDistribution(M.value(), S.value());
        double x = logNormalDistribution.sample();
        return new RandomVariable<>("x", x, this);
    }

    @Override
    public double density(Double x) {
        return logNormalDistribution.density(x);
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