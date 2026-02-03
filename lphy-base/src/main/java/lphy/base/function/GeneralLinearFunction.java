package lphy.base.function;

import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.model.datatype.DoubleValue;

/**
 * General linear function with optional link function.
 * Computes y = g^{-1}(β · x) where g^{-1} is the inverse link function.
 *
 * <h3>Vectorization</h3>
 * <p>LPhy's implicit vectorization automatically handles matrix inputs.
 * If x is a Double[][] matrix (design matrix), the function is applied to each row,
 * returning a Double[] array of results. This enables GLM-style computations:</p>
 * <pre>
 * // X is a design matrix [n x p], beta is coefficients [p]
 * // Returns Double[n] - one value per row of X
 * m = generalLinearFunction(beta=beta, x=X, link="log");
 * </pre>
 *
 * @author Alexei Drummond
 */
public class GeneralLinearFunction extends DeterministicFunction<Double> {

    public static final String betaParamName = "beta";
    public static final String xParamName = "x";
    public static final String linkParamName = "link";

    public GeneralLinearFunction(
            @ParameterInfo(name = betaParamName,
                description = "the coefficients of the general linear model.")
            Value<Double[]> b,
            @ParameterInfo(name = xParamName,
                description = "the explanatory variables.")
            Value<Double[]> x,
            @ParameterInfo(name = linkParamName, optional = true,
                description = "the link function: 'identity' (default), 'log', or 'logit'.")
            Value<String> link) {
        setParam(betaParamName, b);
        setParam(xParamName, x);
        if (link != null) setParam(linkParamName, link);
    }

    @GeneratorInfo(name = "generalLinearFunction",
        description = "The general linear function: y = g^{-1}(sum_i b_i*x_i) " +
                      "where g^{-1} is the inverse link function. " +
                      "When x is a matrix (Double[][]), vectorization applies the function to each row, " +
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

        return new DoubleValue(applyInverseLink(eta, link), this);
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
