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
                new Value<>(null, "identity"));

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
                null);  // no link specified

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
                new Value<>(null, "log"));

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
                new Value<>(null, "logit"));

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
                new Value<>(null, "logit"));

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
                new Value<>(null, "logit"));

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
                new Value<>(null, "unknown"));

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
                new Value<>(null, "LOG"));
        assertEquals(Math.exp(eta), funcUpper.apply().value(), 1e-10);

        // Test mixed case
        GeneralLinearFunction funcMixed = new GeneralLinearFunction(
                new Value<>(null, beta),
                new Value<>(null, x),
                new Value<>(null, "Log"));
        assertEquals(Math.exp(eta), funcMixed.apply().value(), 1e-10);
    }

    @Test
    public void testApplyInverseLinkStatic() {
        // Test the static utility method directly
        assertEquals(5.0, GeneralLinearFunction.applyInverseLink(5.0, "identity"), 1e-10);
        assertEquals(Math.exp(2.0), GeneralLinearFunction.applyInverseLink(2.0, "log"), 1e-10);
        assertEquals(0.5, GeneralLinearFunction.applyInverseLink(0.0, "logit"), 1e-10);
    }
}
