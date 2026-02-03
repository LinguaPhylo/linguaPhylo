package lphy.base.function;

import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.model.datatype.DoubleValue;

/**
 * General linear function with optional link function and scale parameter.
 * Computes y = scale × g^{-1}(β · x) where g^{-1} is the inverse link function.
 *
 * <h3>Vectorisation</h3>
 * <p>LPhy's implicit vectorisation automatically handles matrix inputs.
 * If x is a Double[][] matrix (design matrix), the function is applied to each row,
 * returning a Double[] array of results. This enables GLM-style computations:</p>
 * <pre>
 * // X is a design matrix [n x p], beta is coefficients [p]
 * // Returns Double[n] - one value per row of X
 * m = generalLinearFunction(beta=beta, x=X, link="log");
 *
 * // With optional scale parameter (useful for MASCOT GLM conversion):
 * m = generalLinearFunction(beta=beta, x=X, link="log", scale=s);
 * // Returns: scale × exp(X × beta)
 * </pre>
 *
 * @author Alexei Drummond
 */
public class GeneralLinearFunction extends DeterministicFunction<Double> {

    public static final String betaParamName = "beta";
    public static final String xParamName = "x";
    public static final String linkParamName = "link";
    public static final String scaleParamName = "scale";

    public GeneralLinearFunction(
            @ParameterInfo(name = betaParamName,
                description = "the coefficients of the general linear model.")
            Value<Double[]> b,
            @ParameterInfo(name = xParamName,
                description = "the explanatory variables (predictors). " +
                              "For GLM with separate scale, do not include an intercept column.")
            Value<Double[]> x,
            @ParameterInfo(name = linkParamName, optional = true,
                description = "the link function: 'identity' (default), 'log', or 'logit'.")
            Value<String> link,
            @ParameterInfo(name = scaleParamName, optional = true,
                description = "optional scale multiplier applied after the inverse link transformation. " +
                              "Result is: scale × g^{-1}(β · x). Default is 1.0.")
            Value<Double> scale) {
        setParam(betaParamName, b);
        setParam(xParamName, x);
        if (link != null) setParam(linkParamName, link);
        if (scale != null) setParam(scaleParamName, scale);
    }

    @GeneratorInfo(name = "generalLinearFunction",
        description = "The general linear function: y = scale × g^{-1}(sum_i b_i*x_i) " +
                      "where g^{-1} is the inverse link function and scale is an optional multiplier (default 1.0). " +
                      "When x is a matrix (Double[][]), vectorisation applies the function to each row, " +
                      "returning Double[] - useful for computing multiple GLM predictions from a design matrix.")
    public Value<Double> apply() {
        Value<Double[]> b = getParams().get(betaParamName);
        Value<Double[]> x = getParams().get(xParamName);

        double eta = 0.0;
        for (int i = 0; i < b.value().length; i++) {
            eta += b.value()[i] * x.value()[i];
        }

        String link = "identity";
        Value<String> linkValue = getParams().get(linkParamName);
        if (linkValue != null) link = linkValue.value();

        double scale = 1.0;
        Value<Double> scaleValue = getParams().get(scaleParamName);
        if (scaleValue != null) scale = scaleValue.value();

        return new DoubleValue(scale * applyInverseLink(eta, link), this);
    }

    /**
     * Apply the inverse link function to transform the linear predictor.
     * @param eta the linear predictor (sum of beta_i * x_i)
     * @param link the link function name
     * @return the transformed value
     */
    public static double applyInverseLink(double eta, String link) {
        return switch (link.toLowerCase()) {
            case "identity" -> eta;
            case "log" -> Math.exp(eta);
            case "logit" -> 1.0 / (1.0 + Math.exp(-eta));
            default -> throw new IllegalArgumentException(
                "Unknown link function: " + link + ". Use 'identity', 'log', or 'logit'.");
        };
    }
}
