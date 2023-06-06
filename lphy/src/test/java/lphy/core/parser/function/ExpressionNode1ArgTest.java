package lphy.core.parser.function;

import lphy.core.model.Value;
import lphy.core.model.datatype.IntegerValue;
import lphy.core.parser.ParserTest;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Walter Xie
 */
class ExpressionNode1ArgTest {

    @Test
    void testRound() {
        Object res = ParserTest.parse("x=round(10/3);");
        assertTrue(res instanceof Value<?>, "Result = " + res);

        Object rV = ((Value) res).value();
        assertTrue(rV instanceof Integer, "Result value = " + rV);
        assertEquals(3, ((Integer) rV).intValue(), "Result value = " + rV);
    }

    @Test
    void testIdNull() {
        final String exprStr = "round(10/3)";
        Value[] values = new Value[]{new IntegerValue(null, 3)};
        ExpressionNode expr = new ExpressionNode1Arg(exprStr, ExpressionNode1Arg.round(), values);
        
        assertNotNull(expr, "test expression " + exprStr);
        assertNotNull(expr.getParams(), "test expression " + exprStr);
        // key cannot be null
        expr.getParams().forEach((key, value) ->
                assertNotNull(key, "test key of " + expr.getName()));
    }

    // https://en.wikipedia.org/wiki/Probit
    @Test
    void testProbit() {
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