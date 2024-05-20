package lphy.base;

import lphy.base.function.Difference;
import lphy.core.model.Value;
import org.junit.jupiter.api.Test;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

    @Test
    void sameSet() {
        Double[] set1 = {1.1, 2.2, 3.3};
        Double[] set2 = {1.1, 2.2, 3.3};
        Value<Double[]> mainSet = new Value<>("mainSet", set1);
        Value<Double[]> excludeSet = new Value<>("excludeSet", set2);

        // Capture logs
        Logger logger = Logger.getLogger(Difference.class.getName());
        TestLogHandler handler = new TestLogHandler();
        logger.addHandler(handler);

        new Difference<>(mainSet, excludeSet).apply();

        // Check log message
        assertEquals("The difference set is empty because the main set is equal to the exclude set.", handler.getMessage());

        logger.removeHandler(handler);
    }

    private static class TestLogHandler extends StreamHandler {
        private String message;

        @Override
        public void publish(LogRecord record) {
            if (record.getLevel().equals(Level.WARNING)) {
                message = record.getMessage();
            }
            super.publish(record);
        }

        public String getMessage() {
            return message;
        }
    }
}