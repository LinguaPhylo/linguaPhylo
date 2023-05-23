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
        // TODO Double[][] * Integer[][]
        //  Integer[][] * Double[][]
        // Double[][] * Double[][]



        // Integer[][] * Integer[][]
        Object res = ParserTest.parse("prod = [[1,2,3],[3,2,1]] * [[1,2,3],[3,2,1]];");
        assertTrue(res instanceof Value<?>, "Result = " + res);

        Object rV = ((Value) res).value();
        assertTrue(rV instanceof Integer[][], "Result value = " + rV);
        Integer[][] expect = {  {1, 4, 9}, {9, 4, 1}  };
        assertTrue(Arrays.deepEquals((Integer[][]) rV, expect), "Result value = " + rV);




    }

}