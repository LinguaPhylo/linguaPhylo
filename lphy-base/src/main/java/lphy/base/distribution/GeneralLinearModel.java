package lphy.base.distribution;

import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.ValueUtils;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.TreeMap;

import static lphy.base.distribution.DistributionConstants.betaParamName;
import static lphy.base.distribution.DistributionConstants.sdParamName;
import static lphy.base.function.GeneralLinearFunction.xParamName;

/**
 * General Linear Model.
 * @author Alexei Drummond
 */
public class GeneralLinearModel extends ParametricDistribution<Double> {

    public static final String stdevParamName = "stdev";

    private Value<Number[]> beta;

    private Value<Number[]> x;

    private Value<Number> sd;

    public GeneralLinearModel(@ParameterInfo(name = betaParamName, narrativeName = "beta",
                                      description = "the coefficients of the general linear model.")
                              Value<Number[]> beta,
                              @ParameterInfo(name = xParamName, narrativeName = "x",
                                      description = "the explanatory variables of the general linear model.")
                              Value<Number[]> x,
                              @ParameterInfo(name = sdParamName, narrativeName = "stdev",
                                      description = "the standard deviation of the general linear model.")
                              Value<Number> sd) {
        super();
        this.beta = beta;
        this.x = x;
        this.sd = sd;
    }

    @Override
    protected void constructDistribution(RandomGenerator random) {
    }

    @GeneratorInfo(name = "GLM", verbClause = "have", narrativeName = "General linear model",
            category = GeneratorCategory.ALL, description = "The general linear model.")
    public RandomVariable<Double> sample() {

        double mean = 0.0;

        double[] b = ValueUtils.doubleArrayValue(beta);
        double[] xv = ValueUtils.doubleArrayValue(x);

        for (int i = 0; i < b.length; i++) {
            mean += b[i] * xv[i];
        }

        NormalDistribution normalDistribution = new NormalDistribution(mean, ValueUtils.doubleValue(sd));

        return new RandomVariable<>("y", normalDistribution.sample(), this);
    }

    public double density(Double[] d) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(betaParamName, beta);
            put(xParamName, x);
            put(sdParamName, sd);
        }};
    }

    public void setParam(String paramName, Value value) {
        if (paramName.equals(betaParamName)) beta = value;
        else if (paramName.equals(xParamName)) x = value;
        else if (paramName.equals(sdParamName)) sd = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);

        super.setParam(paramName, value); // constructDistribution
    }
}