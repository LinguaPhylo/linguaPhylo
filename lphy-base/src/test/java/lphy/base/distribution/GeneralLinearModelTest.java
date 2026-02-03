package lphy.base.distribution;

import lphy.core.model.Value;
import lphy.core.simulator.RandomUtils;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GeneralLinearModelTest {

    @BeforeEach
    void setUp() {
        RandomUtils.setSeed(777);
    }

    @Test
    public void testIdentityLink() {
        Number[] beta = {2.0, 3.0};
        Number[] x = {1.0, 2.0};
        // eta = 2*1 + 3*2 = 8

        GeneralLinearModel glm = new GeneralLinearModel(
                new Value<>(null, beta),
                new Value<>(null, x),
                new Value<>(null, 0.5),  // small sd for tighter distribution
                new Value<>(null, "identity"),
                null);  // no scale

        SummaryStatistics stats = new SummaryStatistics();
        for (int i = 0; i < 10000; i++) {
            stats.addValue(glm.sample().value());
        }

        // Mean should be close to 8
        assertEquals(8.0, stats.getMean(), 0.05);
        // Variance should be close to 0.25 (sd^2)
        assertEquals(0.25, stats.getVariance(), 0.02);
    }

    @Test
    public void testIdentityLinkDefault() {
        // Test that identity is the default when no link is specified
        Number[] beta = {2.0, 3.0};
        Number[] x = {1.0, 2.0};
        // eta = 8

        GeneralLinearModel glm = new GeneralLinearModel(
                new Value<>(null, beta),
                new Value<>(null, x),
                new Value<>(null, 0.5),
                null,   // no link specified
                null);  // no scale specified

        SummaryStatistics stats = new SummaryStatistics();
        for (int i = 0; i < 10000; i++) {
            stats.addValue(glm.sample().value());
        }

        assertEquals(8.0, stats.getMean(), 0.05);
    }

    @Test
    public void testLogLink() {
        Number[] beta = {1.0};
        Number[] x = {1.0};
        // eta = 1.0
        // On link scale: N(1.0, sd)
        // After inverse link (exp): log-normal distribution

        double sd = 0.1;
        GeneralLinearModel glm = new GeneralLinearModel(
                new Value<>(null, beta),
                new Value<>(null, x),
                new Value<>(null, sd),
                new Value<>(null, "log"),
                null);  // no scale

        SummaryStatistics stats = new SummaryStatistics();
        for (int i = 0; i < 10000; i++) {
            double sample = glm.sample().value();
            assertTrue(sample > 0, "Log link should produce positive values");
            stats.addValue(sample);
        }

        // For log-normal: E[Y] = exp(mu + sigma^2/2)
        // mu = 1.0, sigma = 0.1
        double expectedMean = Math.exp(1.0 + 0.01 / 2);
        assertEquals(expectedMean, stats.getMean(), 0.1);
    }

    @Test
    public void testLogitLink() {
        Number[] beta = {0.0};
        Number[] x = {1.0};
        // eta = 0
        // logit^-1(0) = 0.5

        double sd = 0.1;
        GeneralLinearModel glm = new GeneralLinearModel(
                new Value<>(null, beta),
                new Value<>(null, x),
                new Value<>(null, sd),
                new Value<>(null, "logit"),
                null);  // no scale

        SummaryStatistics stats = new SummaryStatistics();
        for (int i = 0; i < 10000; i++) {
            double sample = glm.sample().value();
            assertTrue(sample > 0 && sample < 1, "Logit link should produce values in (0,1)");
            stats.addValue(sample);
        }

        // Mean should be approximately 0.5 (with small sd)
        assertEquals(0.5, stats.getMean(), 0.05);
    }

    @Test
    public void testLogitLinkBounds() {
        // Test that logit link always produces values between 0 and 1
        Number[] beta = {5.0};  // Large positive
        Number[] x = {1.0};

        GeneralLinearModel glm = new GeneralLinearModel(
                new Value<>(null, beta),
                new Value<>(null, x),
                new Value<>(null, 1.0),  // larger sd
                new Value<>(null, "logit"),
                null);  // no scale

        for (int i = 0; i < 1000; i++) {
            double sample = glm.sample().value();
            assertTrue(sample > 0 && sample < 1,
                "Logit link should always produce values in (0,1), got: " + sample);
        }
    }

    @Test
    public void testGetParams() {
        Number[] beta = {1.0, 2.0};
        Number[] x = {1.0, 1.0};

        // With link parameter
        GeneralLinearModel glmWithLink = new GeneralLinearModel(
                new Value<>("beta", beta),
                new Value<>("x", x),
                new Value<>("sd", 0.5),
                new Value<>("link", "log"),
                null);  // no scale

        assertTrue(glmWithLink.getParams().containsKey("link"));
        assertEquals(4, glmWithLink.getParams().size());

        // Without link parameter
        GeneralLinearModel glmNoLink = new GeneralLinearModel(
                new Value<>("beta", beta),
                new Value<>("x", x),
                new Value<>("sd", 0.5),
                null,   // no link
                null);  // no scale

        assertFalse(glmNoLink.getParams().containsKey("link"));
        assertEquals(3, glmNoLink.getParams().size());

        // With scale parameter
        GeneralLinearModel glmWithScale = new GeneralLinearModel(
                new Value<>("beta", beta),
                new Value<>("x", x),
                new Value<>("sd", 0.5),
                new Value<>("link", "log"),
                new Value<>("scale", 2.5));

        assertTrue(glmWithScale.getParams().containsKey("scale"));
        assertEquals(5, glmWithScale.getParams().size());
    }

    @Test
    public void testSetParam() {
        Number[] beta = {1.0};
        Number[] x = {1.0};

        GeneralLinearModel glm = new GeneralLinearModel(
                new Value<>(null, beta),
                new Value<>(null, x),
                new Value<>(null, 0.5),
                null,   // no link
                null);  // no scale

        // Set link parameter
        glm.setParam("link", new Value<>(null, "log"));
        assertTrue(glm.getParams().containsKey("link"));
        assertEquals("log", glm.getParams().get("link").value());

        // Set scale parameter
        glm.setParam("scale", new Value<>(null, 2.0));
        assertTrue(glm.getParams().containsKey("scale"));
        assertEquals(2.0, ((Number) glm.getParams().get("scale").value()).doubleValue());
    }

    @Test
    public void testUnknownLinkThrows() {
        Number[] beta = {1.0};
        Number[] x = {1.0};

        GeneralLinearModel glm = new GeneralLinearModel(
                new Value<>(null, beta),
                new Value<>(null, x),
                new Value<>(null, 0.5),
                new Value<>(null, "unknown"),
                null);  // no scale

        assertThrows(IllegalArgumentException.class, glm::sample);
    }

    @Test
    public void testScaleParameter() {
        Number[] beta = {1.0};
        Number[] x = {1.0};
        // eta = 1.0
        // exp(1.0) ≈ 2.718
        // scale * exp(1.0) = 2.0 * 2.718 ≈ 5.436

        double sd = 0.1;
        double scale = 2.0;
        GeneralLinearModel glm = new GeneralLinearModel(
                new Value<>(null, beta),
                new Value<>(null, x),
                new Value<>(null, sd),
                new Value<>(null, "log"),
                new Value<>(null, scale));

        SummaryStatistics stats = new SummaryStatistics();
        for (int i = 0; i < 10000; i++) {
            double sample = glm.sample().value();
            assertTrue(sample > 0, "Log link with scale should produce positive values");
            stats.addValue(sample);
        }

        // For log-normal scaled: E[Y] = scale * exp(mu + sigma^2/2)
        double expectedMean = scale * Math.exp(1.0 + 0.01 / 2);
        assertEquals(expectedMean, stats.getMean(), 0.2);
    }

    @Test
    public void testScaleDefaultIsOne() {
        Number[] beta = {1.0};
        Number[] x = {1.0};
        double sd = 0.1;

        // With scale = null (default 1.0)
        GeneralLinearModel glmNoScale = new GeneralLinearModel(
                new Value<>(null, beta),
                new Value<>(null, x),
                new Value<>(null, sd),
                new Value<>(null, "log"),
                null);

        // With scale = 1.0 explicitly
        GeneralLinearModel glmScale1 = new GeneralLinearModel(
                new Value<>(null, beta),
                new Value<>(null, x),
                new Value<>(null, sd),
                new Value<>(null, "log"),
                new Value<>(null, 1.0));

        // Both should produce similar statistics
        SummaryStatistics statsNoScale = new SummaryStatistics();
        SummaryStatistics statsScale1 = new SummaryStatistics();
        for (int i = 0; i < 10000; i++) {
            statsNoScale.addValue(glmNoScale.sample().value());
            statsScale1.addValue(glmScale1.sample().value());
        }

        // Means should be close (within statistical variation)
        assertEquals(statsNoScale.getMean(), statsScale1.getMean(), 0.2);
    }
}
