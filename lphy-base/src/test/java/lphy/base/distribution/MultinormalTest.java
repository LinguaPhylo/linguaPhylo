package lphy.base.distribution;

import lphy.core.model.Value;
import lphy.core.simulator.RandomUtils;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MultinormalTest {

    public double DELTA_MEAN = 1.0;
    public double DELTA_VARIANCE = 1000.0;

    @BeforeEach
    void setUp() {
        RandomUtils.setSeed(777);
    }

    /**
     * testing Multinomial moments
     * E(Xi) = n * pi
     * Var(Xi) = n * pi * (1 - pi)
     */
    @Test
    public void testMultinomial() {
        int nReplicates = 100000;
        Value<Integer> n = new Value<>("n", 100000);
        Double[] prob = {0.3, 0.2, 0.4, 0.1};
        Value<Double[]> p = new Value<>("p", prob);
        Multinomial multinomial = new Multinomial();
        multinomial.setParam("n", n);
        multinomial.setParam("p", p);

        int k = prob.length;
        double[][] results = new double[k][nReplicates];
        double[] expectedMean = new double[k];
        double[] expectedVariance = new double[k];

        Value<Integer[]> result;
        for (int j = 0; j < nReplicates; j++) {
            result = multinomial.sample();
            for (int i = 0; i < k; i++) {
                // expected mean
                expectedMean[i] = n.value() * prob[i];
                expectedVariance[i] = expectedMean[i] * (1 - prob[i]);
                results[i][j] = (double) (result.value()[i]);
            }
        }
        for (int i = 0; i < k; i++) {
            Mean mean = new Mean();
            double observedMean = mean.evaluate(results[i], 0, nReplicates);
            assertEquals(expectedMean[i], observedMean, DELTA_MEAN);
            Variance variance = new Variance();
            double observedVariance = variance.evaluate(results[i], 0, nReplicates);
            assertEquals(expectedVariance[i], observedVariance, DELTA_VARIANCE);
        }

        Double[] prob2 = {0.3, 0.2, 0.2, 0.3};
        Value<Integer> n2 = new Value<>("n", 1000);
        Value<Double[]> p2 = new Value<>("p", prob2);
        multinomial.setParam("n", new Value<>("n", n2.value()));
        multinomial.setParam("p", new Value<>("p", p2.value()));
        int k2 = prob2.length;
        double[][] results2 = new double[k2][nReplicates];
        double[] expectedMean2 = new double[k2];
        double[] expectedVariance2 = new double[k2];
        Value<Integer[]> result2;
        for (int j = 0; j < nReplicates; j++) {
            result2 = multinomial.sample();
            for (int i = 0; i < k2; i++) {
                // expected mean
                expectedMean2[i] = n2.value() * prob2[i];
                expectedVariance2[i] = expectedMean2[i] * (1 - prob2[i]);
                results2[i][j] = (double) (result2.value()[i]);
            }
        }
        for (int i = 0; i < k2; i++) {
            Mean mean2 = new Mean();
            double observedMean2 = mean2.evaluate(results2[i], 0, nReplicates);
            assertEquals(expectedMean2[i], observedMean2, DELTA_MEAN);
            Variance variance2 = new Variance();
            double observedVariance2 = variance2.evaluate(results2[i], 0, nReplicates);
            assertEquals(expectedVariance2[i], observedVariance2, DELTA_VARIANCE);
        }
    }

}
