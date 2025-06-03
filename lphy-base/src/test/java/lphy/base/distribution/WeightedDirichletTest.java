package lphy.base.distribution;

import lphy.core.model.Value;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WeightedDirichletTest {

    @Test
    void sample() {
        Value<Number[]> conc = new Value<>(null, new Number[]{2,2,2});
        Value<Integer[]> weights = new Value<>(null, new Integer[]{100,200,700});
        // weighted mean default to 1
        WeightedDirichlet weightedDirichlet = new WeightedDirichlet(conc, weights, null);

        Integer[] weightsArr = weights.value();
        for (int n = 0; n < 10; n++) {
            Double[] val = weightedDirichlet.sample().value();

            // the weight mean = sum(x[i] * weight[i]) / sum(weight[i])
            double weightedSumX = IntStream.range(0, val.length)
                    .mapToDouble(i -> val[i] * weightsArr[i].doubleValue()).sum();
            double wSum = Arrays.stream(weightsArr).mapToDouble(Integer::doubleValue).sum();
            double weightedMeanX = weightedSumX / wSum;

            assertEquals(1.0, weightedMeanX, 1e-6,
                    "Replicate " + n + ", values = " + Arrays.toString(val));
        }
    }
}