package lphy.base.distribution;

import lphy.core.model.Value;
import lphy.core.simulator.RandomUtils;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.junit.jupiter.api.BeforeEach;

public class DirichletMultinomialTest {

    @BeforeEach
    void setUp() {
        RandomUtils.setSeed(123);
    }

    /*
     * delta threshold
     * stdErr = stdDev / sqrt(n)
     * delta = 2 * stdErr to fail 1/20 times
     */
    private double getDelta(double n, double variance) {
        double stdDev = Math.sqrt(variance);
        double stdErr = stdDev / Math.sqrt(n);
        return 2.0 * stdErr;
    }

    /**
     * testing DirichletMultinomial moments
     * E(Xi) = n * pi
     * Var(Xi) = n * pi * (1 - pi) * (n + w) / (1 + w)
     * stdDev = sqrt(Var(Xi))
     */
    //@Test
    public void testDirichletMultinomial() {
        int nReplicates = 100000;
        Integer nv = 100000; // sample size
        Value<Integer> n = new Value<>("n", nv);
        Double[] prob = {0.3, 0.2, 0.4, 0.1};
        Double wv = 1000.0;
        Value<Double> w = new Value<>("w", wv);
        Value<Double[]> p = new Value<>("p", prob);
        DirichletMultinomial dirichletMultinomial = new DirichletMultinomial();
        dirichletMultinomial.setParam("n", n);
        dirichletMultinomial.setParam("p", p);
        dirichletMultinomial.setParam("w", w);


        int k = prob.length;
        double[][] results = new double[k][nReplicates];
        double[] expectedMean = new double[k];
        double[] expectedVariance = new double[k];

        Value<Integer[]> result;
        for (int j = 0; j < nReplicates; j++) {
            result = dirichletMultinomial.sample();
            for (int i = 0; i < k; i++) {
                // expected mean
                expectedMean[i] = nv * prob[i];
                expectedVariance[i] = nv * prob[i] * (1 - prob[i]) * ((nv + wv) / (1 + wv));
                results[i][j] = (double) (result.value()[i]);
            }
        }

        k = 1;
        for (int i = 0; i < k; i++) {
            Mean mean = new Mean();
            int numFailures = 0;
            for (int j = 0; j < nReplicates; j++) {
                double observedMean = results[i][j];
//                System.out.println("variance: " + expectedVariance[i]);
                double deltaMean = getDelta(nv, expectedVariance[i]);
                if (observedMean < expectedMean[i] - deltaMean || observedMean > expectedMean[i] + deltaMean) {
                    numFailures++;
                }
                double diff = observedMean - expectedMean[i];
//                System.out.println(diff);
            }
            //assertEquals(nReplicates / 20.0, numFailures);
        }

        Double[] prob2 = {0.3, 0.2, 0.2, 0.3};
        Integer nv2 = 100;
        Value<Integer> n2 = new Value<>("n", nv2);
        Value<Double[]> p2 = new Value<>("p", prob2);
        Double wv2 = 1000.0;
        Value<Double> w2 = new Value<>("w", wv2);
        dirichletMultinomial.setParam("n", new Value<>("n", n2.value()));
        dirichletMultinomial.setParam("p", new Value<>("p", p2.value()));
        dirichletMultinomial.setParam("w", new Value<>("w", w2.value()));
        int k2 = prob2.length;
        double[][] results2 = new double[k2][nReplicates];
        double[] expectedMean2 = new double[k2];
        double[] expectedVariance2 = new double[k2];
        Value<Integer[]> result2;
        for (int j = 0; j < nReplicates; j++) {
            result2 = dirichletMultinomial.sample();
            for (int i = 0; i < k2; i++) {
                // expected mean
                expectedMean2[i] = nv2 * prob2[i];
                expectedVariance2[i] = nv2 * prob2[i] * (1 - prob2[i]) * ((nv2 + wv2) / (1 + wv2));
                results2[i][j] = (double) (result2.value()[i]);
            }
        }
        for (int i = 0; i < k2; i++) {
            Mean mean2 = new Mean();
            double observedMean2 = mean2.evaluate(results2[i], 0, nReplicates);
//            assertEquals(expectedMean2[i], observedMean2, DELTA_MEAN);
            Variance variance2 = new Variance();
            double observedVariance2 = variance2.evaluate(results2[i], 0, nReplicates);
//            assertEquals(expectedVariance2[i], observedVariance2, DELTA_VARIANCE);
        }
    }

}
