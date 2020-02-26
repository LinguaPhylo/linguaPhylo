package james.core.distributions;

import james.graphicalModel.*;
import org.apache.commons.math3.distribution.LogNormalDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by adru001 on 18/12/19.
 */
public class LogNormalMulti implements GenerativeDistribution<Double[]> {

    private final String meanLogParamName;
    private final String sdLogParamName;
    private final String nParamName;
    private Value<Double> M;
    private Value<Double> S;
    private Value<Integer> n;

    private RandomGenerator random;

    LogNormalDistribution logNormalDistribution;

    public LogNormalMulti(@ParameterInfo(name = "meanlog", description = "the mean of the distribution on the log scale.") Value<Double> M,
                          @ParameterInfo(name = "sdlog", description = "the standard deviation of the distribution on the log scale.") Value<Double> S,
                          @ParameterInfo(name = "n", description = "the dimension of the return.") Value<Integer> n) {

        this.M = M;
        this.S = S;
        this.n = n;
        this.random = Utils.getRandom();

        meanLogParamName = getParamName(0);
        sdLogParamName = getParamName(1);
        nParamName = getParamName(2);
    }

    @GenerativeDistributionInfo(name="LogNormal", description="The log-normal probability distribution.")
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
        for (int i = 0; i < x.length; i++) {
            logDensity += logNormalDistribution.logDensity(x[i]);
        }
        return logDensity;
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
        else if (paramName.equals(nParamName)) n = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);
    }

    public String toString() {
        return getName();
    }
}