package lphy.base.function;

import lphy.core.model.Value;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The constructor used to call {@code setParam(startParamName, step)}, so the step value
 * overwrote start and nothing was ever stored under "step". Nothing else populates the
 * param map -- {@code Generator.setInputs} is defined but never called by the parser -- so
 * {@code step()} returned null and {@code apply()} threw NPE for every input.
 *
 * <p>This made {@code arange} unusable, which mattered for rate shifts because
 * {@code rateShiftTimes = arange(start=0.0, stop=98.0, step=1.0)} is the natural way to
 * write the 99 weekly intervals in the MASCOT-GLM examples.
 */
public class ARangeTest {

    private static Double[] arange(double start, double stop, double step) {
        return new ARange(new Value<>("start", start),
                          new Value<>("stop", stop),
                          new Value<>("step", step)).apply().value();
    }

    @Test
    public void appliesWithoutThrowing() {
        assertDoesNotThrow(() -> arange(0.0, 98.0, 1.0));
    }

    @Test
    public void producesInclusiveRange() {
        Double[] r = arange(0.0, 98.0, 1.0);
        assertEquals(99, r.length, "stop is inclusive: 0..98 is 99 values");
        assertEquals(0.0, r[0], 1e-12);
        assertEquals(98.0, r[98], 1e-12);
    }

    @Test
    public void honoursTheStepValue() {
        // the bug specifically lost `step`, so vary it and check the spacing
        Double[] byTwo = arange(0.0, 10.0, 2.0);
        assertEquals(6, byTwo.length);
        for (int i = 0; i < byTwo.length; i++) assertEquals(i * 2.0, byTwo[i], 1e-12);

        Double[] byHalf = arange(0.0, 2.0, 0.5);
        assertEquals(5, byHalf.length);
        for (int i = 0; i < byHalf.length; i++) assertEquals(i * 0.5, byHalf[i], 1e-12);
    }

    @Test
    public void startIsNotOverwrittenByStep() {
        // the exact shape of the bug: start took on the step value
        Double[] r = arange(5.0, 9.0, 1.0);
        assertEquals(5.0, r[0], 1e-12, "start must survive construction");
        assertEquals(9.0, r[r.length - 1], 1e-12);
    }

    @Test
    public void noNullElements() {
        // regression for #162, which the original fix targeted
        for (Double v : arange(0.0, 2.0, 0.1)) assertNotNull(v);
    }
}
