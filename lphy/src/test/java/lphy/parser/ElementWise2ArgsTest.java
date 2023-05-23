package lphy.parser;

import lphy.graphicalModel.Value;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Walter Xie
 */
class ElementWise2ArgsTest {

    @Test
    void test2DTimes2D() {
        // Double[][] * Double[][]
        Object res = ParserTest.parse("prod = [[1.0,2.0,3.0],[3.0,2.0,1.0]] * [[1.0,2.0,3.0],[3.0,2.0,1.0]];");
        assertTrue(res instanceof Value<?>, "Result = " + res);

        Object rV = ((Value) res).value();
        assertTrue(rV instanceof Double[][], "Result value = " + rV);
        final Double[][] expect1 = {  {1.0, 4.0, 9.0}, {9.0, 4.0, 1.0}  };
        assertTrue(Arrays.deepEquals((Double[][]) rV, expect1), "Result value = " + rV);

        // Double[][] * Integer[][]
        res = ParserTest.parse("prod = [[1.0,2.0,3.0],[3.0,2.0,1.0]] * [[1,2,3],[3,2,1]];");
        assertTrue(res instanceof Value<?>, "Result = " + res);

        rV = ((Value) res).value();
        assertTrue(rV instanceof Double[][], "Result value = " + rV);
        assertTrue(Arrays.deepEquals((Double[][]) rV, expect1), "Result value = " + rV);

        // Integer[][] * Double[][]
        res = ParserTest.parse("prod = [[1,2,3],[3,2,1]] * [[1.0,2.0,3.0],[3.0,2.0,1.0]];");
        assertTrue(res instanceof Value<?>, "Result = " + res);

        rV = ((Value) res).value();
        assertTrue(rV instanceof Double[][], "Result value = " + rV);
        assertTrue(Arrays.deepEquals((Double[][]) rV, expect1), "Result value = " + rV);

        // Integer[][] * Integer[][]
        res = ParserTest.parse("prod = [[1,2,3],[3,2,1]] * [[1,2,3],[3,2,1]];");
        assertTrue(res instanceof Value<?>, "Result = " + res);

        rV = ((Value) res).value();
        assertTrue(rV instanceof Integer[][], "Result value = " + rV);
        final Integer[][] expect2 = {  {1, 4, 9}, {9, 4, 1}  };
        assertTrue(Arrays.deepEquals((Integer[][]) rV, expect2), "Result value = " + rV);
    }

    @Test
    void test1DPlus2D() {
        // Double[] + Double[][]
        Object res = ParserTest.parse("sum = [1.0,2.0,3.0] + [[1.0,2.0,3.0],[3.0,2.0,1.0]];");
        assertTrue(res instanceof Value<?>, "Result = " + res);

        Object rV = ((Value) res).value();
        assertTrue(rV instanceof Double[][], "Result value = " + rV);
        final Double[][] expect1 = {  {2.0, 4.0, 6.0}, {4.0, 4.0, 4.0}  };
        assertTrue(Arrays.deepEquals((Double[][]) rV, expect1), "Result value = " + rV);

        // Double[] + Integer[][]
        res = ParserTest.parse("sum = [1.0,2.0,3.0] + [[1,2,3],[3,2,1]];");
        assertTrue(res instanceof Value<?>, "Result = " + res);

        rV = ((Value) res).value();
        assertTrue(rV instanceof Double[][], "Result value = " + rV);
        assertTrue(Arrays.deepEquals((Double[][]) rV, expect1), "Result value = " + rV);

        // Integer[] + Double[][]
        res = ParserTest.parse("sum = [1,2,3] + [[1.0,2.0,3.0],[3.0,2.0,1.0]];");
        assertTrue(res instanceof Value<?>, "Result = " + res);

        rV = ((Value) res).value();
        assertTrue(rV instanceof Double[][], "Result value = " + rV);
        assertTrue(Arrays.deepEquals((Double[][]) rV, expect1), "Result value = " + rV);

        // Integer[] + Integer[][]
        res = ParserTest.parse("sum = [1,2,3] + [[1,2,3],[3,2,1]];");
        assertTrue(res instanceof Value<?>, "Result = " + res);

        rV = ((Value) res).value();
        assertTrue(rV instanceof Integer[][], "Result value = " + rV);
        final Integer[][] expect2 = {  {2, 4, 6}, {4, 4, 4}  };
        assertTrue(Arrays.deepEquals((Integer[][]) rV, expect2), "Result value = " + rV);
    }


}