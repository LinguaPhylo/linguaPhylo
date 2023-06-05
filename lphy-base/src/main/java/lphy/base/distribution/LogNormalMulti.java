package lphy.base.distribution;

import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.model.component.GenerativeDistribution;
import lphy.core.model.component.RandomVariable;
import lphy.core.model.component.Value;
import lphy.core.model.component.ValueUtils;
import org.apache.commons.math3.distribution.LogNormalDistribution;

import java.util.Map;
import java.util.TreeMap;

import static lphy.base.distribution.LogNormal.meanLogParamName;
import static lphy.base.distribution.LogNormal.sdLogParamName;

/**
 * Created by Alexei Drummond on 18/12/19.
 */

@Deprecated()
public class LogNormalMulti implements GenerativeDistribution<Double[]> {

    private Value<Number> M;
    private Value<Number> S;
    private Value<Integer> n;

    LogNormalDistribution logNormalDistribution;

    public LogNormalMulti(@ParameterInfo(name = meanLogParamName, narrativeName = "mean in log space", description = "the mean of the distribution on the log scale.") Value<Number> M,
                          @ParameterInfo(name = sdLogParamName, narrativeName = "standard deviation in log space", description = "the standard deviation of the distribution on the log scale.") Value<Number> S,
                          @ParameterInfo(name = DistributionConstants.nParamName, narrativeName = "dimension", description = "the dimension of the return.") Value<Integer> n) {

        this.M = M;
        this.S = S;
        this.n = n;
    }

    @GeneratorInfo(name = "LogNormal", narrativeName = "i.i.d. log-normal prior", description = "The log-normal probability distribution.")
    public RandomVariable<Double[]> sample() {

        logNormalDistribution = new LogNormalDistribution(ValueUtils.doubleValue(M), ValueUtils.doubleValue(S));
        Double[] result = new Double[n.value()];
        for (int i = 0; i < result.length; i++) {
            result[i] = logNormalDistribution.sample();
        }

        return new RandomVariable<>(null, result, this);
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
            put(DistributionConstants.nParamName, n);
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
            case DistributionConstants.nParamName:
                n = value;
                break;
            default:
                throw new RuntimeException("Unrecognised parameter name: " + paramName);
        }
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