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
import static lphy.base.function.GeneralLinearFunction.scaleParamName;
import static lphy.base.function.GeneralLinearFunction.xParamName;

/**
 * General Linear Model (GLM) distribution with optional link function and scale parameter.
 * Samples from y where g(y/scale) ~ Normal(β · x, σ), i.e., error is on the link scale.
 *
 * <h3>Vectorisation</h3>
 * <p>LPhy's implicit vectorisation automatically handles matrix inputs.
 * If x is a Double[][] matrix (design matrix), the distribution is applied to each row,
 * returning a Double[] array of samples. This enables GLM-style modelling:</p>
 * <pre>
 * // X is a design matrix [n x p], beta is coefficients [p]
 * // Returns Double[n] - one sample per row of X
 * y ~ GLM(beta=beta, x=X, sd=sigma, link="log");
 *
 * // With optional scale parameter:
 * y ~ GLM(beta=beta, x=X, sd=sigma, link="log", scale=s);
 * // Returns: scale × exp(X × beta + error)
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

    private Value<Number> scale;

    public GeneralLinearModel(
            @ParameterInfo(name = betaParamName, narrativeName = "beta",
                description = "the coefficients of the general linear model.")
            Value<Number[]> beta,
            @ParameterInfo(name = xParamName, narrativeName = "x",
                description = "the explanatory variables of the general linear model. " +
                              "For GLM with separate scale, do not include an intercept column.")
            Value<Number[]> x,
            @ParameterInfo(name = sdParamName, narrativeName = "stdev",
                description = "the standard deviation of residual error (on the link scale).")
            Value<Number> sd,
            @ParameterInfo(name = linkParamName, optional = true,
                description = "the link function: 'identity' (default), 'log', or 'logit'.")
            Value<String> link,
            @ParameterInfo(name = scaleParamName, optional = true,
                description = "optional scale multiplier applied after the inverse link transformation. " +
                              "Result is: scale × g^{-1}(β · x + error). Default is 1.0.")
            Value<Number> scale) {
        super();
        this.beta = beta;
        this.x = x;
        this.sd = sd;
        this.link = link;
        this.scale = scale;
    }

    @Override
    protected void constructDistribution(RandomGenerator random) {
    }

    @GeneratorInfo(name = "GLM", verbClause = "have", narrativeName = "General linear model",
            category = GeneratorCategory.ALL,
            description = "The general linear model with optional link function and scale. " +
                          "Error is on the link scale: y = scale × g^{-1}(β·x + ε) where ε ~ Normal(0, σ). " +
                          "When x is a matrix (Double[][]), vectorisation applies the model to each row, " +
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

        // Apply scale
        double scaleValue = (scale != null) ? ValueUtils.doubleValue(scale) : 1.0;
        y = scaleValue * y;

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
        if (scale != null) params.put(scaleParamName, scale);
        return params;
    }

    public void setParam(String paramName, Value value) {
        switch (paramName) {
            case betaParamName -> beta = value;
            case xParamName -> x = value;
            case sdParamName -> sd = value;
            case linkParamName -> link = value;
            case scaleParamName -> scale = value;
            default -> throw new RuntimeException("Unrecognised parameter name: " + paramName);
        }
        super.setParam(paramName, value); // constructDistribution
    }
}