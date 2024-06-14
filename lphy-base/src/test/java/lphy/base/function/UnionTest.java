package lphy.base.function;

import lphy.core.model.Value;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UnionTest {
    @Test
    void applyTest1() {
        String[] set1 = {"1","2","3"};
        String[] set2 = {"4","5"};
        Value<String[]> firstSet = new Value<>("firstSet", set1);
        Value<String[]> secondSet = new Value<>("secondSet", set2);

        Union instance = new Union(firstSet,secondSet);
        String[] observe = (String[]) instance.apply().value();
        String[] expect = {"1","2","3","4","5"};
        for (int i = 0; i < observe.length; i++) {
            assertEquals(expect[i], observe[i]);
        }
    }

    @Test
    void applyTest2() {
        Integer[] set1 = {1,2,3};
        Integer[] set2 = {3,4,5};
        Value<Integer[]> firstSet = new Value<>("firstSet", set1);
        Value<Integer[]> secondSet = new Value<>("secondSet", set2);

        Union instance = new Union(firstSet,secondSet);
        Integer[] observe = (Integer[]) instance.apply().value();
        Integer[] expect = {1,2,3,4,5};
        for (int i = 0; i < observe.length; i++) {
            assertEquals(expect[i], observe[i]);
        }
    }
}
