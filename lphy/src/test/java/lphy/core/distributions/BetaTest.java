package lphy.core.distributions;

import lphy.graphicalModel.Value;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static lphy.graphicalModel.ValueUtils.doubleValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class BetaTest {
    @Parameterized.Parameter(value = 0)
    public Value<Number> alpha;
    @Parameterized.Parameter(value = 1)
    public Value<Number> beta;

    @Parameterized.Parameter(value = 2)
    public double[] logDensityArr;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {new Value<>(null, 5.1), new Value<>(null, 1.9),
                        // expected log density dbeta(seq(0,1,by=.2), 5.1, 1.9, log = T)
                        new double[]{Double.NEGATIVE_INFINITY,-3.5110533,-0.9280637,0.3694246,0.9250886,Double.NEGATIVE_INFINITY}},
                {new Value<>(null, 2), new Value<>(null, 6),
                        // expected log density
                        new double[]{Double.NEGATIVE_INFINITY,1.0125139, 0.2672508,-1.3546097,-4.5326635,Double.NEGATIVE_INFINITY}}
        });
    }

    Beta bt;

    @Before
    public void setUp() throws Exception {
        bt = new Beta(alpha, beta);
    }

    @Test
    public void sample() {
        SummaryStatistics summ = new SummaryStatistics();
        for (int i = 0; i < 10000; i++) {
            summ.addValue(bt.sample().value());
        }
        double m = summ.getMean();
        double var = summ.getVariance();
        double a = doubleValue(alpha);
        double b = doubleValue(beta);

        // https://en.wikipedia.org/wiki/Beta_distribution
        double expectedMean =  a / (a + b);
        double expectedVar =  a * b / ( (a + b) * (a + b) * (a + b + 1) );
        System.out.println(a + " " + b + " : " + expectedMean + " " + expectedVar + "\n"  + summ );

        assertEquals(expectedMean, m, 5e-3);
        assertEquals(expectedVar, var, 1e-3);
        // [0,1]
        assertTrue(summ.getMin() >= bt.getDomainBounds()[0]);
        assertTrue(summ.getMax() <= bt.getDomainBounds()[1]);
    }

    @Test
    public void logDensity() {
        System.out.println(alpha + " " + beta);
        // 0 to 1 step .2
        for (int i = 0; i < logDensityArr.length; i++) {
            double x = (double) i/(logDensityArr.length-1);
            // b.betaDistribution.density(x)
            double ld = bt.logDensity(x);
            System.out.println(x + "  " + ld);

            assertEquals(logDensityArr[i], ld, 1e-5);
        }
    }
}