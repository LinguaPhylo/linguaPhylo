package lphy.base.distribution;

import lphy.core.model.Value;
import lphy.core.simulator.RandomUtils;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MultinomialTest {

//    public double DELTA_MEAN = 1.0;
//    public double DELTA_VARIANCE = 1000.0;

    public double DELTA = 1e-6;

    @BeforeEach
    void setUp() {
        RandomUtils.setSeed(123);
    }

    /**
     * testing Multinomial moments
     * E(Xi) = n * pi
     * Var(Xi) = n * pi * (1 - pi)
     */
    //@Test
    public void testMultinomial() {
        int nSamples = 30; // 1% of E(X)
        int nTrials = 10000;
        Double[] prob = {0.3, 0.2, 0.4, 0.1};
        // E(X) = (3000 , 2000, 4000, 1000)

        // repeat 100 times
        // should fail with 1/20 probability
        int reps = 100;
        int[][] totalCount = new int[reps][prob.length];
        for (int i = 0; i < reps; i++) {
            int[] count = sample(nSamples, nTrials, prob);
            for (int j = 0; j < count.length; j++) {
                totalCount[i][j] += count[j];
            }
        }

        int totalFailures = 0;
        // print failure counts
        for (int i = 0; i < reps; i++) {
            boolean rowFailure = false; // rows are reps, cols are k counts
            for (int j = 0; j < prob.length; j++) {
                System.out.print(totalCount[i][j] + " ");
                if (totalCount[i][j] > 0) {
                    totalFailures++;
//                    rowFailure = true;
                }
            }
            System.out.println();
        }

        // assert 1/20 prob for failures
        System.out.println("Total failures: " + totalFailures);
        assertEquals(0.05 * reps * prob.length-1, totalFailures, DELTA);

    }

    private int[] sample(int nSamples, int nTrials, Double[] prob) {
        Value<Integer> n = new Value<>("n", nTrials);
        Value<Double[]> p = new Value<>("p", prob);
        Multinomial multinomial = new Multinomial();
        multinomial.setParam("n", n);
        multinomial.setParam("p", p);

        int k = prob.length;
        double[][] results = new double[k][nSamples];
        double[] expectedMean = new double[k];
        double[] expectedVariance = new double[k];

        Value<Integer[]> result;
        int[] count = new int[k];
        double stdError;
        for (int j = 0; j < nSamples; j++) {
            result = multinomial.sample(); // one sample from multinomial x = (x1, .. xk)
            for (int i = 0; i < k; i++) {
                // expected mean
                expectedMean[i] = n.value() * prob[i];
                expectedVariance[i] = expectedMean[i] * (1 - prob[i]);
                results[i][j] = (double) (result.value()[i]);
            }
        }


        for (int i = 0; i < k; i++) {
//            System.out.println(count[i] + "\t" + nSamples/20 + "\t" + nSamples);
            Mean mean = new Mean();
            double observedMean = mean.evaluate(results[i], 0, nSamples);
//            System.out.println(observedMean);
            Variance variance = new Variance();
            double observedVariance = variance.evaluate(results[i], 0, nSamples);
//            System.out.println(observedVariance);
            stdError =  calculateStdError(expectedVariance[i], nSamples);
            if (observedMean > expectedMean[i] + 2 * stdError || observedMean < expectedMean[i] - 2 * stdError) {
                count[i] = count[i] + 1;
            }
        }

        return count;
    }

    private double calculateStdError(double variance, double sampleSize) {
            return Math.sqrt(variance / sampleSize);
    }

}

