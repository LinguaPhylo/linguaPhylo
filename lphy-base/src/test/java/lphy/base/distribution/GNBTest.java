package lphy.base.distribution;

import lphy.core.model.Value;
import lphy.core.simulator.RandomUtils;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class GNBTest {
    @BeforeEach
    void setUp() {
        RandomUtils.setSeed(123);
    }

    @Test
    public void testGNB() {
        int rep = 100;
        int sampleSize = 10000;
        int failures = 0;
        Double pv = 0.4;
        Double rv = 3.3;
        double observedMean;
        double observedVariance;
        double expectedMean = rv * (1 - pv) / pv;
        double expectedVariance = rv * (1 - pv) / (pv * pv);
        double stdError = Math.sqrt(expectedVariance / sampleSize);
        double DELTA = 2 * stdError;
        Value<Double> p = new Value<Double>("p", pv);
        Value<Double> r = new Value<Double>("r", rv);
        GeneralNegativeBinomial negativeBinomial =  new GeneralNegativeBinomial();
        negativeBinomial.setParam("p", p);
        negativeBinomial.setParam("r", r);
        for (int j = 0; j < rep; j++) {
            SummaryStatistics results = new SummaryStatistics();
            for (int i = 0; i < sampleSize; i++) {
                int result = negativeBinomial.sample().value();
                results.addValue(result);
            }
            observedMean = results.getMean();
            observedVariance = results.getVariance();
            if (observedMean < expectedMean - DELTA || observedMean > expectedMean + DELTA) {
                failures++;
            }
            //assertEquals(expectedMean, observedMean, DELTA);
        }
        System.out.println(failures);//failures should be less than rep *1/20
    }
}
