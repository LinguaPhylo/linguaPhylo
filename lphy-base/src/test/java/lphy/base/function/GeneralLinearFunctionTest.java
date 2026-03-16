package lphy.base.function;

import lphy.core.model.Value;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GeneralLinearFunctionTest {

    @Test
    public void testIdentityLink() {
        Double[] beta = {2.0, 3.0};
        Double[] x = {1.0, 2.0};
        // eta = 2*1 + 3*2 = 8

        GeneralLinearFunction func = new GeneralLinearFunction(
                new Value<>(null, beta),
                new Value<>(null, x),
                new Value<>(null, "identity"),
                null, null, null);

        Value<Double> result = func.apply();
        assertEquals(8.0, result.value(), 1e-10);
    }

    @Test
    public void testIdentityLinkDefault() {
        // Test that identity is the default when no link is specified
        Double[] beta = {2.0, 3.0};
        Double[] x = {1.0, 2.0};
        // eta = 2*1 + 3*2 = 8

        GeneralLinearFunction func = new GeneralLinearFunction(
                new Value<>(null, beta),
                new Value<>(null, x),
                null,   // no link specified
                null, null, null);  // no scale, indicator, or error specified

        Value<Double> result = func.apply();
        assertEquals(8.0, result.value(), 1e-10);
    }

    @Test
    public void testLogLink() {
        Double[] beta = {1.0, 0.5};
        Double[] x = {1.0, 2.0};
        // eta = 1*1 + 0.5*2 = 2
        // exp(2) = 7.389...

        GeneralLinearFunction func = new GeneralLinearFunction(
                new Value<>(null, beta),
                new Value<>(null, x),
                new Value<>(null, "log"),
                null, null, null);

        Value<Double> result = func.apply();
        assertEquals(Math.exp(2.0), result.value(), 1e-10);
    }

    @Test
    public void testLogitLink() {
        Double[] beta = {0.0};
        Double[] x = {1.0};
        // eta = 0
        // logit^-1(0) = 1/(1+exp(0)) = 0.5

        GeneralLinearFunction func = new GeneralLinearFunction(
                new Value<>(null, beta),
                new Value<>(null, x),
                new Value<>(null, "logit"),
                null, null, null);

        Value<Double> result = func.apply();
        assertEquals(0.5, result.value(), 1e-10);
    }

    @Test
    public void testLogitLinkPositive() {
        Double[] beta = {2.0};
        Double[] x = {1.0};
        // eta = 2
        // logit^-1(2) = 1/(1+exp(-2)) ≈ 0.8808

        GeneralLinearFunction func = new GeneralLinearFunction(
                new Value<>(null, beta),
                new Value<>(null, x),
                new Value<>(null, "logit"),
                null, null, null);

        Value<Double> result = func.apply();
        double expected = 1.0 / (1.0 + Math.exp(-2.0));
        assertEquals(expected, result.value(), 1e-10);
    }

    @Test
    public void testLogitLinkNegative() {
        Double[] beta = {-2.0};
        Double[] x = {1.0};
        // eta = -2
        // logit^-1(-2) = 1/(1+exp(2)) ≈ 0.1192

        GeneralLinearFunction func = new GeneralLinearFunction(
                new Value<>(null, beta),
                new Value<>(null, x),
                new Value<>(null, "logit"),
                null, null, null);

        Value<Double> result = func.apply();
        double expected = 1.0 / (1.0 + Math.exp(2.0));
        assertEquals(expected, result.value(), 1e-10);
    }

    @Test
    public void testUnknownLinkThrows() {
        Double[] beta = {1.0};
        Double[] x = {1.0};

        GeneralLinearFunction func = new GeneralLinearFunction(
                new Value<>(null, beta),
                new Value<>(null, x),
                new Value<>(null, "unknown"),
                null, null, null);

        assertThrows(IllegalArgumentException.class, func::apply);
    }

    @Test
    public void testCaseInsensitiveLink() {
        Double[] beta = {1.0, 0.5};
        Double[] x = {1.0, 2.0};
        double eta = 2.0;

        // Test uppercase
        GeneralLinearFunction funcUpper = new GeneralLinearFunction(
                new Value<>(null, beta),
                new Value<>(null, x),
                new Value<>(null, "LOG"),
                null, null, null);
        assertEquals(Math.exp(eta), funcUpper.apply().value(), 1e-10);

        // Test mixed case
        GeneralLinearFunction funcMixed = new GeneralLinearFunction(
                new Value<>(null, beta),
                new Value<>(null, x),
                new Value<>(null, "Log"),
                null, null, null);
        assertEquals(Math.exp(eta), funcMixed.apply().value(), 1e-10);
    }

    @Test
    public void testApplyInverseLinkStatic() {
        // Test the static utility method directly
        assertEquals(5.0, GeneralLinearFunction.applyInverseLink(5.0, "identity"), 1e-10);
        assertEquals(Math.exp(2.0), GeneralLinearFunction.applyInverseLink(2.0, "log"), 1e-10);
        assertEquals(0.5, GeneralLinearFunction.applyInverseLink(0.0, "logit"), 1e-10);
    }

    @Test
    public void testScaleParameter() {
        Double[] beta = {1.0, 0.5};
        Double[] x = {1.0, 2.0};
        // eta = 1*1 + 0.5*2 = 2
        // exp(2) = 7.389...
        // scale * exp(2) = 2.5 * 7.389... = 18.47...

        double scale = 2.5;
        GeneralLinearFunction func = new GeneralLinearFunction(
                new Value<>(null, beta),
                new Value<>(null, x),
                new Value<>(null, "log"),
                new Value<>(null, scale), null, null);

        Value<Double> result = func.apply();
        assertEquals(scale * Math.exp(2.0), result.value(), 1e-10);
    }

    @Test
    public void testScaleDefaultIsOne() {
        Double[] beta = {1.0, 0.5};
        Double[] x = {1.0, 2.0};
        // eta = 2, exp(2) = 7.389...

        // With scale = null (default 1.0)
        GeneralLinearFunction funcNoScale = new GeneralLinearFunction(
                new Value<>(null, beta),
                new Value<>(null, x),
                new Value<>(null, "log"),
                null, null, null);

        // With scale = 1.0 explicitly
        GeneralLinearFunction funcScale1 = new GeneralLinearFunction(
                new Value<>(null, beta),
                new Value<>(null, x),
                new Value<>(null, "log"),
                new Value<>(null, 1.0), null, null);

        assertEquals(funcNoScale.apply().value(), funcScale1.apply().value(), 1e-10);
    }

    @Test
    public void testScaleWithIdentityLink() {
        Double[] beta = {2.0, 3.0};
        Double[] x = {1.0, 2.0};
        // eta = 8
        // scale * eta = 0.5 * 8 = 4

        double scale = 0.5;
        GeneralLinearFunction func = new GeneralLinearFunction(
                new Value<>(null, beta),
                new Value<>(null, x),
                new Value<>(null, "identity"),
                new Value<>(null, scale), null, null);

        Value<Double> result = func.apply();
        assertEquals(4.0, result.value(), 1e-10);
    }

    @Test
    public void testIndicatorExcludesPredictor() {
        Double[] beta = {2.0, 3.0};
        Double[] x = {1.0, 2.0};
        // Without indicator: eta = 2*1 + 3*2 = 8
        // With indicator [true, false]: eta = 2*1 = 2

        Boolean[] indicator = {true, false};
        GeneralLinearFunction func = new GeneralLinearFunction(
                new Value<>(null, beta),
                new Value<>(null, x),
                new Value<>(null, "identity"),
                null,
                new Value<>(null, indicator),
                null);

        Value<Double> result = func.apply();
        assertEquals(2.0, result.value(), 1e-10);
    }

    @Test
    public void testIndicatorAllTrue() {
        Double[] beta = {2.0, 3.0};
        Double[] x = {1.0, 2.0};
        // indicator all true should give same result as no indicator
        // eta = 2*1 + 3*2 = 8

        Boolean[] indicator = {true, true};
        GeneralLinearFunction func = new GeneralLinearFunction(
                new Value<>(null, beta),
                new Value<>(null, x),
                new Value<>(null, "identity"),
                null,
                new Value<>(null, indicator),
                null);

        Value<Double> result = func.apply();
        assertEquals(8.0, result.value(), 1e-10);
    }

    @Test
    public void testIndicatorAllFalse() {
        Double[] beta = {2.0, 3.0};
        Double[] x = {1.0, 2.0};
        // indicator all false: eta = 0

        Boolean[] indicator = {false, false};
        GeneralLinearFunction func = new GeneralLinearFunction(
                new Value<>(null, beta),
                new Value<>(null, x),
                new Value<>(null, "log"),
                null,
                new Value<>(null, indicator),
                null);

        Value<Double> result = func.apply();
        // exp(0) = 1.0
        assertEquals(1.0, result.value(), 1e-10);
    }

    @Test
    public void testErrorTerm() {
        Double[] beta = {1.0, 0.5};
        Double[] x = {1.0, 2.0};
        // eta = 1*1 + 0.5*2 = 2
        // eta + error = 2 + 0.5 = 2.5
        // exp(2.5) = 12.182...

        double error = 0.5;
        GeneralLinearFunction func = new GeneralLinearFunction(
                new Value<>(null, beta),
                new Value<>(null, x),
                new Value<>(null, "log"),
                null,
                null,
                new Value<>(null, error));

        Value<Double> result = func.apply();
        assertEquals(Math.exp(2.5), result.value(), 1e-10);
    }

    @Test
    public void testIndicatorAndError() {
        Double[] beta = {2.0, 3.0};
        Double[] x = {1.0, 2.0};
        // indicator [true, false]: eta = 2*1 = 2
        // eta + error = 2 + 1.0 = 3
        // scale * exp(3) = 0.5 * exp(3)

        Boolean[] indicator = {true, false};
        double error = 1.0;
        double scale = 0.5;
        GeneralLinearFunction func = new GeneralLinearFunction(
                new Value<>(null, beta),
                new Value<>(null, x),
                new Value<>(null, "log"),
                new Value<>(null, scale),
                new Value<>(null, indicator),
                new Value<>(null, error));

        Value<Double> result = func.apply();
        assertEquals(0.5 * Math.exp(3.0), result.value(), 1e-10);
    }
}
