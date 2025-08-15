package lphy.base.distribution;

import lphy.core.model.GenerativeDistribution1D;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.model.datatype.RealValue;
import lphy.core.simulator.RandomUtils;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.phylospec.types.PositiveReal;
import org.phylospec.types.Real;

import java.util.Map;
import java.util.TreeMap;

import static lphy.base.distribution.DistributionConstants.meanParamName;
import static lphy.base.distribution.DistributionConstants.sdParamName;

/**
 * Normal distribution prior.
 * @see NormalDistribution
 * @author Alexei Drummond
 * @author Walter Xie
 */
public class Normal extends ParametricDistribution<Real> implements GenerativeDistribution1D<Real,Double> {

    private Value<Real> mean;
    private Value<PositiveReal> sd;

    NormalDistribution normalDistribution;

    public Normal(@ParameterInfo(name = "mean", description = "the mean of the distribution.")
                  Value<Real> mean,
                  @ParameterInfo(name = "sd", narrativeName = "standard deviation",
                          description = "the standard deviation of the distribution.")
                  Value<PositiveReal> sd) {
        super();
        this.mean = mean;
        this.sd = sd;

        constructDistribution(random);
    }

    @Override
    protected void constructDistribution(RandomGenerator random) {
        if (mean == null) throw new IllegalArgumentException("The mean value can't be null!");
        if (sd == null) throw new IllegalArgumentException("The sd value can't be null!");

        normalDistribution = new NormalDistribution(RandomUtils.getRandom(),
                mean.value().getPrimitive(), sd.value().getPrimitive(),
                NormalDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
    }

    @GeneratorInfo(name = "Normal", verbClause = "has", narrativeName = "normal prior",
            category = GeneratorCategory.PRIOR, examples = {"simplePhyloBrownian.lphy","simplePhyloOU.lphy"},
            description = "The normal probability distribution.")
    public RandomVariable<Real> sample() {
        // constructDistribution() only required in constructor and setParam
        double x = normalDistribution.sample();
        Real real = new RealValue(x);
        return new RandomVariable<>("x", real, this);
    }

    @Override
    public double density(Real x) {
        return normalDistribution.density(x.getPrimitive());
    }

    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(meanParamName, mean);
            put(sdParamName, sd);
        }};
    }

    public void setParam(String paramName, Value value) {
        if (paramName.equals(meanParamName)) mean = value;
        else if (paramName.equals(sdParamName)) sd = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);

        super.setParam(paramName, value); // constructDistribution
    }

    public Value<Real> getMean() {
        return mean;
    }

    public Value<PositiveReal> getSd() {
        return sd;
    }

    private static final Double[] domainBounds = {Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY};

    public Double[] getDomainBounds() {
        return domainBounds;
    }
}