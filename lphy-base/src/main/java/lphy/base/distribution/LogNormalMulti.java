package lphy.base.distribution;

import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.ValueUtils;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import org.apache.commons.math3.distribution.LogNormalDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.TreeMap;

import static lphy.base.distribution.DistributionConstants.nParamName;
import static lphy.base.distribution.LogNormal.meanLogParamName;
import static lphy.base.distribution.LogNormal.sdLogParamName;

/**
 * Created by Alexei Drummond on 18/12/19.
 */

@Deprecated()
public class LogNormalMulti extends ParametricDistribution<Double[]> {

    private Value<Number> M;
    private Value<Number> S;
    private Value<Integer> n;

    LogNormalDistribution logNormalDistribution;

    public LogNormalMulti(@ParameterInfo(name = meanLogParamName, narrativeName = "mean in log space", description = "the mean of the distribution on the log scale.") Value<Number> M,
                          @ParameterInfo(name = sdLogParamName, narrativeName = "standard deviation in log space", description = "the standard deviation of the distribution on the log scale.") Value<Number> S,
                          @ParameterInfo(name = nParamName, narrativeName = "dimension", description = "the dimension of the return.") Value<Integer> n) {
        super();
        this.M = M;
        this.S = S;
        this.n = n;

        constructDistribution(random);
    }

    @GeneratorInfo(name = "LogNormal", narrativeName = "i.i.d. log-normal prior", description = "The log-normal probability distribution.")
    public RandomVariable<Double[]> sample() {
        Double[] result = new Double[n.value()];
        for (int i = 0; i < result.length; i++) {
            result[i] = logNormalDistribution.sample();
        }

        return new RandomVariable<>(null, result, this);
    }

    @Override
    protected void constructDistribution(RandomGenerator random) {
        logNormalDistribution = new LogNormalDistribution(random, ValueUtils.doubleValue(M), ValueUtils.doubleValue(S));
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

        super.setParam(paramName, value); // constructDistribution
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
}