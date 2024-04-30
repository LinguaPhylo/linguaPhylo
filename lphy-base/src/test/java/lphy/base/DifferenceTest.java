package lphy.base;

import lphy.base.function.Difference;
import lphy.core.model.Value;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class DifferenceTest {
    @Test
    void DifferenceString() {
        String[] set1 = {"1","2","3"};
        String[] set2 = {"1","2"};
        Value<String[]> wholeSet = new Value<>("wholeSet", set1);
        Value<String[]> subSet = new Value<>("subSet", set2);

        Difference instance = new Difference(wholeSet, subSet);
        Value<String[]> observe = instance.apply();
        String[] observeValue = observe.value();
        String[] expect = {"3"};
        assertArrayEquals(expect , observeValue);
    }

    @Test
    void SecondSetHasOtherElements() {
        String[] set1 = {"1","2","3"};
        String[] set2 = {"1","4"};
        Value<String[]> wholeSet = new Value<>("wholeSet", set1);
        Value<String[]> subSet = new Value<>("subSet", set2);

        Difference instance = new Difference(wholeSet, subSet);
        Value<String[]> observe = instance.apply();
        String[] observeValue = observe.value();
        String[] expect = {"2","3"};
        assertArrayEquals(expect , observeValue);
    }

    @Test
    void DifferenceInteger() {
        Integer[] set1 = {1,2,3};
        Integer[] set2 = {1,2};
        Value<Integer[]> wholeSet = new Value<>("wholeSet", set1);
        Value<Integer[]> subSet = new Value<>("subSet", set2);

        Difference instance = new Difference(wholeSet, subSet);
        Value<Integer[]> observe = instance.apply();
        Integer[] observeValue = observe.value();
        Integer[] expect = {3};
        assertArrayEquals(expect , observeValue);
    }

    @Test
    void DifferenceDouble() {
        Double[] set1 = {1.1, 2.2, 3.3};
        Double[] set2 = {1.1, 2.2};
        Value<Double[]> wholeSet = new Value<>("wholeSet", set1);
        Value<Double[]> subSet = new Value<>("subSet", set2);

        Difference instance = new Difference(wholeSet, subSet);
        Value<Double[]> observe = instance.apply();
        Double[] observeValue = observe.value();
        Double[] expect = {3.3};
        assertArrayEquals(expect , observeValue);
    }

}
