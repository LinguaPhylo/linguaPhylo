package lphy.base.distribution;

import lphy.core.model.Value;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WeightedDirichletTest {

    @Test
    void sample() {
        Value<Number[]> conc = new Value<>(null, new Number[]{1,1,1});
        Value<Integer[]> weights = new Value<>(null, new Integer[]{100,200,700});
        Integer[] weightsArr = weights.value();
        assertEquals(conc.value().length, weightsArr.length);
        // weighted mean default to 1
        WeightedDirichlet weightedDirichlet = new WeightedDirichlet(conc, weights, null);

        int dim = weightsArr.length;
        double[] mean = new double[dim];
        final int sampleSize = 100;
        for (int n = 0; n < sampleSize; n++) {
            Double[] val = weightedDirichlet.sample().value();

            // the weight mean = sum(x[i] * weight[i]) / sum(weight[i])
            double weightedSumX = IntStream.range(0, val.length)
                    .mapToDouble(i -> val[i] * weightsArr[i].doubleValue()).sum();
            double wSum = Arrays.stream(weightsArr).mapToDouble(Integer::doubleValue).sum();
            double weightedMeanX = weightedSumX / wSum;

            assertEquals(1.0, weightedMeanX, 1e-6,
                    "Replicate " + n + ", values = " + Arrays.toString(val));

            for (int i = 0; i < dim; i++) {
                mean[i] += val[i];
            }

        }

        for (int i = 0; i < dim; i++) {
            mean[i] /= sampleSize;
        }

        System.out.println("mean = " + Arrays.toString(mean));
        // high weight then slow relative rate, low weight fast rate
        assertTrue(mean[0] > mean[1]);
        assertTrue(mean[1] > mean[2]);
    }
}