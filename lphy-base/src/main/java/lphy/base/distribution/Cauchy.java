package lphy.base.distribution;

import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import org.apache.commons.math3.distribution.CauchyDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.phylospec.types.PositiveReal;
import org.phylospec.types.Real;
import org.phylospec.types.impl.RealImpl;

import java.util.Map;
import java.util.TreeMap;

import static lphy.base.distribution.DistributionConstants.medianParamName;
import static lphy.base.distribution.DistributionConstants.scaleParamName;

/**
 * Cauchy distribution.
 * @see CauchyDistribution
 * @author Alexei Drummond
 * @author Walter Xie
 */
public class Cauchy extends ParametricDistribution<Real> {

    private Value<Real> median;
    private Value<PositiveReal> scale;

    CauchyDistribution cauchyDistribution;

    public Cauchy(@ParameterInfo(name = DistributionConstants.medianParamName,
                          description = "the median of the Cauchy distribution.")
                  Value<Real> median,
                  @ParameterInfo(name = scaleParamName, description = "the scale of the Cauchy distribution.")
                  Value<PositiveReal> scale) {
        super();
        this.median = median;
        this.scale = scale;

        constructDistribution(random);
    }

    @Override
    protected void constructDistribution(RandomGenerator random) {
        if (median == null) throw new IllegalArgumentException("The median value can't be null!");
        if (scale == null) throw new IllegalArgumentException("The scale value can't be null!");
        // use code available since apache math 3.1
        cauchyDistribution = new CauchyDistribution(random,
                median.value().getPrimitive(), scale.value().getPrimitive(),
                CauchyDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
    }

    @GeneratorInfo(name = "Cauchy", verbClause = "has", narrativeName = "Cauchy distribution prior",
            category = GeneratorCategory.PRIOR, description = "The Cauchy distribution.")
    public RandomVariable<Real> sample() {
        double x = cauchyDistribution.sample();
        Real real = new RealImpl(x);
        return new RandomVariable<>("x", real, this);
    }

    @Override
    public double density(Real x) {
        return cauchyDistribution.density(x.getPrimitive());
    }

    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(DistributionConstants.medianParamName, median);
            put(scaleParamName, scale);
        }};
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(medianParamName)) median = value;
        else if (paramName.equals(scaleParamName)) scale = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);

        super.setParam(paramName, value); // constructDistribution
    }
}