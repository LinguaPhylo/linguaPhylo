package lphy.evolution.alignment;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class GT16ErrorModelTest {

    private static double DELTA = 1e-15;

    public double[][] getExpectedMatrix(double epsilon, double delta) {
        // Y is homozygous
        double a = 1 - epsilon + (1/2.0) * delta * epsilon;
        double b = (1 - delta) * (1/6.0) * epsilon;
        double c = (1/6.0) * delta * epsilon;
        // Y is heterozygous
        double d = (1/2.0) * delta + (1/6.0) * epsilon - (1/3.0) * delta * epsilon;
        double e = (1/6.0) * delta * epsilon;
        double f = (1 - delta) * (1/6.0) * epsilon;
        double g = (1 - delta) * (1 - epsilon);
        // rows are true states Y, columns are observed states X
        // the entries in the matrix are P(X | Y)
        double[][] expectedMatrix = {
                //      AA AC AG AT CA CC CG CT GA GC GG GT TA TC TG TT
                {a, b, b, b, b, c, 0, 0, b, 0, c, 0, b, 0, 0, c}, // AA
                {d, g, f, f, 0, d, 0, 0, 0, f, e, 0, 0, f, 0, e}, // AC
                {d, f, g, f, 0, e, f, 0, 0, 0, d, 0, 0, 0, f, e}, // AG
                {d, f, f, g, 0, e, 0, f, 0, 0, e, f, 0, 0, 0, d}, // AT
                {d, 0, 0, 0, g, d, f, f, f, 0, e, 0, f, 0, 0, e}, // CA
                {c, b, 0, 0, b, a, b, b, 0, b, c, 0, 0, b, 0, c}, // CC
                {e, 0, f, 0, f, d, g, f, 0, 0, d, 0, 0, 0, f, e}, // CG
                {e, 0, 0, f, f, d, f, g, 0, 0, e, f, 0, 0, 0, d}, // CT
                {d, 0, 0, 0, f, e, 0, 0, g, f, d, f, f, 0, 0, e}, // GA
                {e, f, 0, 0, 0, d, 0, 0, f, g, d, f, 0, f, 0, e}, // GC
                {c, 0, b, 0, 0, c, b, 0, b, b, a, b, 0, 0, b, c}, // GG
                {e, 0, 0, f, 0, e, 0, f, f, f, d, g, 0, 0, 0, d}, // GT
                {d, 0, 0, 0, f, e, 0, 0, f, 0, e, 0, g, f, f, d}, // TA
                {e, f, 0, 0, 0, d, 0, 0, 0, f, e, 0, f, g, f, d}, // TC
                {e, 0, f, 0, 0, e, f, 0, 0, 0, d, 0, f, f, g, d}, // TG
                {c, 0, 0, b, 0, c, 0, b, 0, 0, c, b, b, b, b, a}  // TT
        };

        return expectedMatrix;
    }

    @Test
    public void errorMatrixTestSmallErrors() {
        double epsilon = 0.01;
        double delta = 0.02;

        double[][] observedMatrix = new GT16ErrorModel().errorMatrix(epsilon, delta);
        double[][] expectedMatrix = getExpectedMatrix(epsilon, delta);

        double[] observed = Arrays.stream(observedMatrix).flatMapToDouble(Arrays::stream).toArray();
        double[] expected = Arrays.stream(expectedMatrix).flatMapToDouble(Arrays::stream).toArray();

        assertArrayEquals(expected, observed, DELTA);
    }

    @Test
    public void errorMatrixTestLargeErrors() {
        double epsilon = 0.1;
        double delta = 0.5;

        double[][] observedMatrix = new GT16ErrorModel().errorMatrix(epsilon, delta);
        double[][] expectedMatrix = getExpectedMatrix(epsilon, delta);

        double[] observed = Arrays.stream(observedMatrix).flatMapToDouble(Arrays::stream).toArray();
        double[] expected = Arrays.stream(expectedMatrix).flatMapToDouble(Arrays::stream).toArray();

        assertArrayEquals(expected, observed, DELTA);
    }

    @Test
    public void errorMatrixRowSumsToOne() {
        double delta = 0.1;
        double epsilon = 0.2;
        double expected = 1.0;
        double[][] observedMatrix = new GT16ErrorModel().errorMatrix(epsilon, delta);
        for (int row = 0; row < observedMatrix.length; row++) {
            double sum = 0.0;
            for (int col = 0; col < observedMatrix[row].length; col++) {
                sum += observedMatrix[row][col];
            }
            assertEquals(expected, sum, DELTA);
        }
    }
}
