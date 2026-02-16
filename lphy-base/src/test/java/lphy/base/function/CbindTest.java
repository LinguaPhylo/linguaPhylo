package lphy.base.function;

import lphy.core.model.Value;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CbindTest {

    @Test
    void testBasicCbind() {
        // Two [3][1] matrices => [3][2]
        Double[][] a = {{1.0}, {2.0}, {3.0}};
        Double[][] b = {{4.0}, {5.0}, {6.0}};

        Cbind cbind = new Cbind(new Value<>("a", a), new Value<>("b", b));
        Value<Double[][]> result = cbind.apply();
        Double[][] matrix = result.value();

        assertEquals(3, matrix.length);
        assertEquals(2, matrix[0].length);

        assertArrayEquals(new Double[]{1.0, 4.0}, matrix[0]);
        assertArrayEquals(new Double[]{2.0, 5.0}, matrix[1]);
        assertArrayEquals(new Double[]{3.0, 6.0}, matrix[2]);
    }

    @Test
    void testCbindMultiColumn() {
        // [2][2] + [2][1] => [2][3]
        Double[][] a = {{1.0, 2.0}, {3.0, 4.0}};
        Double[][] b = {{5.0}, {6.0}};

        Cbind cbind = new Cbind(new Value<>("a", a), new Value<>("b", b));
        Value<Double[][]> result = cbind.apply();
        Double[][] matrix = result.value();

        assertEquals(2, matrix.length);
        assertEquals(3, matrix[0].length);

        assertArrayEquals(new Double[]{1.0, 2.0, 5.0}, matrix[0]);
        assertArrayEquals(new Double[]{3.0, 4.0, 6.0}, matrix[1]);
    }

    @Test
    void testCbindRowMismatchThrows() {
        // [2][1] + [3][1] => error
        Double[][] a = {{1.0}, {2.0}};
        Double[][] b = {{3.0}, {4.0}, {5.0}};

        Cbind cbind = new Cbind(new Value<>("a", a), new Value<>("b", b));
        assertThrows(IllegalArgumentException.class, cbind::apply,
                "Should throw when row counts do not match");
    }
}
