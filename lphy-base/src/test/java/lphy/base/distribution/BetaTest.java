package lphy.base.distribution;

import lphy.core.model.Value;
import lphy.core.simulator.RandomUtils;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.phylospec.types.PositiveReal;
import org.phylospec.types.impl.PositiveRealImpl;

import java.util.stream.Stream;

import static lphy.core.model.ValueUtils.doublePrimitiveValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class BetaTest {
    // alpha, beta, expected log density
    static Stream<Arguments> betaArgProvider() {
        return Stream.of(
                arguments(new Value<>(null, new PositiveRealImpl(5.1)),
                        new Value<>(null, new PositiveRealImpl(1.9)),
                        // expected log density dbeta(seq(0,1,by=.2), 5.1, 1.9, log = T)
                        new double[]{Double.NEGATIVE_INFINITY,-3.5110533,-0.9280637,0.3694246,0.9250886,Double.NEGATIVE_INFINITY}),
                arguments(new Value<>(null, new PositiveRealImpl(2)),
                        new Value<>(null, new PositiveRealImpl(6)),
                        // expected log density
                        new double[]{Double.NEGATIVE_INFINITY,1.0125139, 0.2672508,-1.3546097,-4.5326635,Double.NEGATIVE_INFINITY})
        );
    }

    @BeforeEach
    void setUp() {
        RandomUtils.setSeed(777);
    }

    @ParameterizedTest
    @MethodSource("betaArgProvider")
    public void sample(Value<PositiveReal> alpha, Value<PositiveReal> beta) {
        System.out.println("alpha = " + alpha + ", beta = " + beta);
        Beta bt = new Beta(alpha, beta);

        SummaryStatistics summ = new SummaryStatistics();
        for (int i = 0; i < 10000; i++) {
            summ.addValue(bt.sample().value().getPrimitive());
        }
        double m = summ.getMean();
        double var = summ.getVariance();
        double a = doublePrimitiveValue(alpha);
        double b = doublePrimitiveValue(beta);

        // https://en.wikipedia.org/wiki/Beta_distribution
        double expectedMean =  a / (a + b);
        double expectedVar =  a * b / ( (a + b) * (a + b) * (a + b + 1) );
        System.out.println("expectedMean = " + expectedMean + ", expectedVar = " + expectedVar + "\n"  + summ );

        assertEquals(expectedMean, m, 5e-3);
        assertEquals(expectedVar, var, 1e-3);
        // [0,1]
        assertTrue(summ.getMin() >= bt.getDomainBounds()[0]);
        assertTrue(summ.getMax() <= bt.getDomainBounds()[1]);
    }

    @ParameterizedTest
    @MethodSource("betaArgProvider")
    public void logDensity(Value<PositiveReal> alpha, Value<PositiveReal> beta, double[] logDensityArr) {
        System.out.println("alpha = " + alpha + ", beta = " + beta);
        Beta bt = new Beta(alpha, beta);
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