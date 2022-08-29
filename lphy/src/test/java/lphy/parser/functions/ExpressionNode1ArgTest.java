package lphy.parser.functions;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Walter Xie
 */
class ExpressionNode1ArgTest {

    // https://en.wikipedia.org/wiki/Probit
    @Test
    void probit() {
        // standard NormalDistribution, m = 0, sd = 1
        NormalDistribution normalDistribution = new NormalDistribution();
        double y = normalDistribution.cumulativeProbability(-1.96);
        System.out.println("NormalCDF(-1.96) = " + y);
        assertEquals(0.025, y, 1e-4, "NormalCDF(-1.96) = 0.025 , but get" + y);

        y = normalDistribution.cumulativeProbability(1.96);
        System.out.println("NormalCDF(1.96) = " + y);
        assertEquals(0.975, y, 1e-4, "NormalCDF(1.96) = 0.975 , but get" + y);

        double x = ExpressionNode1Arg.probit().apply(0.025);
        System.out.println("probit(0.025) = " + x);
        assertEquals(-1.96, x, 1e-4, "probit(0.025) = -1.96 , but get" + x);

        x = ExpressionNode1Arg.probit().apply(0.975);
        System.out.println("probit(0.975) = " + x);
        assertEquals(1.96, x, 1e-4,  "probit(0.975) = 1.96, but get" + x);
    }


}