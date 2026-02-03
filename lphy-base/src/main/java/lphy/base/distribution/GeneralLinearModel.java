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
import static lphy.base.function.GeneralLinearFunction.linkParamName;
import static lphy.base.function.GeneralLinearFunction.xParamName;

/**
 * General Linear Model (GLM) distribution with optional link function.
 * Samples from y where g(y) ~ Normal(β · x, σ), i.e., error is on the link scale.
 *
 * <h3>Vectorization</h3>
 * <p>LPhy's implicit vectorization automatically handles matrix inputs.
 * If x is a Double[][] matrix (design matrix), the distribution is applied to each row,
 * returning a Double[] array of samples. This enables GLM-style modeling:</p>
 * <pre>
 * // X is a design matrix [n x p], beta is coefficients [p]
 * // Returns Double[n] - one sample per row of X
 * y ~ GLM(beta=beta, x=X, sd=sigma, link="log");
 * </pre>
 *
 * @author Alexei Drummond
 */
public class GeneralLinearModel extends ParametricDistribution<Double> {

    public static final String stdevParamName = "stdev";

    private Value<Number[]> beta;

    private Value<Number[]> x;

    private Value<Number> sd;

    private Value<String> link;

    public GeneralLinearModel(
            @ParameterInfo(name = betaParamName, narrativeName = "beta",
                description = "the coefficients of the general linear model.")
            Value<Number[]> beta,
            @ParameterInfo(name = xParamName, narrativeName = "x",
                description = "the explanatory variables of the general linear model.")
            Value<Number[]> x,
            @ParameterInfo(name = sdParamName, narrativeName = "stdev",
                description = "the standard deviation of residual error (on the link scale).")
            Value<Number> sd,
            @ParameterInfo(name = linkParamName, optional = true,
                description = "the link function: 'identity' (default), 'log', or 'logit'.")
            Value<String> link) {
        super();
        this.beta = beta;
        this.x = x;
        this.sd = sd;
        this.link = link;
    }

    @Override
    protected void constructDistribution(RandomGenerator random) {
    }

    @GeneratorInfo(name = "GLM", verbClause = "have", narrativeName = "General linear model",
            category = GeneratorCategory.ALL,
            description = "The general linear model with optional link function. " +
                          "Error is on the link scale: g(y) ~ Normal(β·x, σ). " +
                          "When x is a matrix (Double[][]), vectorization applies the model to each row, " +
                          "returning Double[] - useful for sampling multiple GLM responses from a design matrix.")
    public RandomVariable<Double> sample() {

        double[] b = ValueUtils.doubleArrayValue(beta);
        double[] xv = ValueUtils.doubleArrayValue(x);

        // Compute linear predictor
        double eta = 0.0;
        for (int i = 0; i < b.length; i++) {
            eta += b[i] * xv[i];
        }

        // Add error on link scale
        NormalDistribution normalDistribution = new NormalDistribution(
            random, eta, ValueUtils.doubleValue(sd));
        double etaWithError = normalDistribution.sample();

        // Apply inverse link
        String linkFunc = (link != null) ? link.value() : "identity";
        double y = lphy.base.function.GeneralLinearFunction.applyInverseLink(etaWithError, linkFunc);

        return new RandomVariable<>("y", y, this);
    }

    public double density(Double[] d) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Map<String, Value> getParams() {
        Map<String, Value> params = new TreeMap<>();
        params.put(betaParamName, beta);
        params.put(xParamName, x);
        params.put(sdParamName, sd);
        if (link != null) params.put(linkParamName, link);
        return params;
    }

    public void setParam(String paramName, Value value) {
        switch (paramName) {
            case betaParamName -> beta = value;
            case xParamName -> x = value;
            case sdParamName -> sd = value;
            case linkParamName -> link = value;
            default -> throw new RuntimeException("Unrecognised parameter name: " + paramName);
        }
        super.setParam(paramName, value); // constructDistribution
    }
}