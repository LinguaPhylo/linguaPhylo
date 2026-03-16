package lphy.base.function;

import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.model.datatype.DoubleValue;

/**
 * General linear function with optional link function, scale parameter, indicator, and error term.
 * Computes y = scale × g^{-1}(sum_j indicator_j * beta_j * x_j + error) where g^{-1} is the inverse link function.
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
 *
 * // With BSSVS indicators for predictor selection:
 * indicator ~ Bernoulli(p=0.5, replicates=nPredictors);
 * m = generalLinearFunction(beta=beta, x=X, link="log", scale=s, indicator=indicator);
 *
 * // With error term (additive on the linear predictor):
 * error ~ Normal(mean=0.0, sd=sigma);
 * m = generalLinearFunction(beta=beta, x=X, link="log", scale=s, error=error);
 * </pre>
 *
 * @author Alexei Drummond
 */
public class GeneralLinearFunction extends DeterministicFunction<Double> {

    public static final String betaParamName = "beta";
    public static final String xParamName = "x";
    public static final String linkParamName = "link";
    public static final String scaleParamName = "scale";
    public static final String indicatorParamName = "indicator";
    public static final String errorParamName = "error";

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
                              "Result is: scale × g^{-1}(linear predictor). Default is 1.0.")
            Value<Double> scale,
            @ParameterInfo(name = indicatorParamName, optional = true,
                description = "optional Boolean array for BSSVS (Bayesian stochastic search variable selection). " +
                              "When provided, only predictors with indicator=true contribute to the linear predictor. " +
                              "Must have same length as beta.")
            Value<Boolean[]> indicator,
            @ParameterInfo(name = errorParamName, optional = true,
                description = "optional error term added to the linear predictor before the inverse link transformation. " +
                              "Result is: scale × g^{-1}(sum_j indicator_j * beta_j * x_j + error).")
            Value<Double> error) {
        setParam(betaParamName, b);
        setParam(xParamName, x);
        if (link != null) setParam(linkParamName, link);
        if (scale != null) setParam(scaleParamName, scale);
        if (indicator != null) setParam(indicatorParamName, indicator);
        if (error != null) setParam(errorParamName, error);
    }

    @GeneratorInfo(name = "generalLinearFunction",
        description = "The general linear function: y = scale × g^{-1}(sum_j indicator_j * beta_j * x_j + error) " +
                      "where g^{-1} is the inverse link function, scale is an optional multiplier (default 1.0), " +
                      "indicator is an optional Boolean array for BSSVS predictor selection (default all true), " +
                      "and error is an optional additive term on the linear predictor (default 0.0). " +
                      "When x is a matrix (Double[][]), vectorisation applies the function to each row, " +
                      "returning Double[] - useful for computing multiple GLM predictions from a design matrix.")
    public Value<Double> apply() {
        Value<Double[]> b = getParams().get(betaParamName);
        Value<Double[]> x = getParams().get(xParamName);

        // Optional indicator for BSSVS
        Value<Boolean[]> indicatorValue = getParams().get(indicatorParamName);
        Boolean[] indicators = (indicatorValue != null) ? indicatorValue.value() : null;

        double eta = 0.0;
        for (int i = 0; i < b.value().length; i++) {
            if (indicators == null || indicators[i]) {
                eta += b.value()[i] * x.value()[i];
            }
        }

        // Optional error term
        Value<Double> errorValue = getParams().get(errorParamName);
        if (errorValue != null) {
            eta += errorValue.value();
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
