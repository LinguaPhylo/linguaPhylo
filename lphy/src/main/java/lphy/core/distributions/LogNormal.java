package lphy.core.distributions;

import lphy.graphicalModel.*;
import org.apache.commons.math3.distribution.LogNormalDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.TreeMap;

import static lphy.graphicalModel.ValueUtils.doubleValue;
import static org.apache.commons.math3.distribution.LogNormalDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY;

/**
 * log-normal prior.
 * @see LogNormalDistribution
 * @author Alexei Drummond
 * @author Walter Xie
 */
public class LogNormal extends PriorDistributionGenerator<Double> implements GenerativeDistribution1D<Double> {

    public static final String meanLogParamName = "meanlog";
    public static final String sdLogParamName = "sdlog";
    private Value<Number> M;
    private Value<Number> S;

    LogNormalDistribution logNormalDistribution;

    public LogNormal(@ParameterInfo(name = meanLogParamName, narrativeName = "mean in log space", description = "the mean of the distribution on the log scale.") Value<Number> M,
                     @ParameterInfo(name = sdLogParamName, narrativeName = "standard deviation in log space", description = "the standard deviation of the distribution on the log scale.") Value<Number> S) {
        super();
        this.M = M;
        this.S = S;

        constructDistribution(random);
    }

    @Override
    protected void constructDistribution(RandomGenerator random) {
        // use code available since apache math 3.1
        logNormalDistribution = new LogNormalDistribution(random, doubleValue(M), doubleValue(S),
                DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
    }

    @GeneratorInfo(name = "LogNormal", verbClause = "has", narrativeName = "log-normal prior",
            category = GeneratorCategory.PROB_DIST, examples = {"hkyCoalescent.lphy","errorModel1.lphy"},
            description = "The log-normal probability distribution.")
    public RandomVariable<Double> sample() {
        // constructDistribution() only required in constructor and setParam
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