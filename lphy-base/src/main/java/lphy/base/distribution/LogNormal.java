package lphy.base.distribution;

import lphy.core.model.GenerativeDistribution1D;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.ValueUtils;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import org.apache.commons.math3.distribution.LogNormalDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.phylospec.types.NonNegativeReal;
import org.phylospec.types.PositiveReal;
import org.phylospec.types.Real;
import org.phylospec.types.impl.PositiveRealImpl;

import java.util.Map;
import java.util.TreeMap;

import static lphy.base.distribution.DistributionConstants.offsetParamName;

/**
 * log-normal prior.
 * @see LogNormalDistribution
 * @author Alexei Drummond
 * @author Walter Xie
 */
public class LogNormal extends ParametricDistribution<PositiveReal> implements GenerativeDistribution1D<PositiveReal, Double> {

    public static final String meanLogParamName = "meanlog";
    public static final String sdLogParamName = "sdlog";
    private Value<Real> M;
    private Value<PositiveReal> SD;
    private Value<NonNegativeReal> offset;

    LogNormalDistribution logNormalDistribution;

    public LogNormal(@ParameterInfo(name = meanLogParamName, narrativeName = "mean in log space",
                             description = "the mean of the distribution on the log scale.")
                     Value<Real> M,
                     @ParameterInfo(name = sdLogParamName, narrativeName = "standard deviation in log space",
                             description = "the standard deviation of the distribution on the log scale.")
                     Value<PositiveReal> SD,
                     @ParameterInfo(name = offsetParamName, optional = true, narrativeName = "offset",
                             description = "optional parameter to shift entire distribution by an offset. default is 0.")
                     Value<NonNegativeReal> offset) {
        super();
        this.M = M;
        this.SD = SD;
        this.offset = offset;

        constructDistribution(random);
    }

    @Override
    protected void constructDistribution(RandomGenerator random) {
        // use code available since apache math 3.1
        logNormalDistribution = new LogNormalDistribution(random,
                ValueUtils.doublePrimitiveValue(M), ValueUtils.doublePrimitiveValue(SD),
                LogNormalDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
    }

    @GeneratorInfo(name = "LogNormal", verbClause = "has", narrativeName = "log-normal prior",
            category = GeneratorCategory.PRIOR, examples = {"hkyCoalescent.lphy","errorModel1.lphy"},
            description = "The log-normal probability distribution.")
    public RandomVariable<PositiveReal> sample() {
        // .sample() is before offset
        double result = logNormalDistribution.sample() + C();
        PositiveReal validRes = new PositiveRealImpl(result);
        // constructDistribution() only required in constructor and setParam
        return new RandomVariable<>(null, validRes, this);
    }

    // default offset=0
    private double C() {
        double C = 0;
        if (offset != null) {
            C = ValueUtils.doublePrimitiveValue(offset);
        }
        return C;
    }

    public double logDensity(PositiveReal x) {
        // x is after offset, so - to get the original point
        return logNormalDistribution.logDensity(x.getPrimitive()-C());
    }

    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(meanLogParamName, M);
            put(sdLogParamName, SD);
            if (offset != null) put(offsetParamName, offset); // optional
        }};
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(meanLogParamName)) M = value;
        else if (paramName.equals(sdLogParamName)) SD = value;
        else if (paramName.equals(offsetParamName)) offset = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);

        super.setParam(paramName, value); // constructDistribution
    }

    public Value<Real> getMeanLog() {
        return M;
    }

    public Value<PositiveReal> getSDLog() {
        return SD;
    }

    public Double[] getDomainBounds() {
        return new Double[] {C(), Double.POSITIVE_INFINITY};
    }
}