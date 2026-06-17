package lphy.base.evolution.birthdeath;

import lphy.core.model.Value;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests the shared age-conditioning validation lifted into
 * {@link lphy.base.evolution.tree.AgeConditionedTreeGenerator#checkAgeParameters(boolean)},
 * exercised through FullBirthDeath which requires exactly one of rootAge / originAge.
 */
public class AgeConditioningTest {

    private static Value<Number> num(double x) {
        return new Value<>(null, x);
    }

    @Test
    void rootAgeAndOriginAgeAreMutuallyExclusive() {
        assertThrows(IllegalArgumentException.class,
                () -> new FullBirthDeathTree(num(1.0), num(0.5), num(4.0), num(5.0)),
                "specifying both rootAge and originAge must be rejected");
    }

    @Test
    void exactlyOneAgeIsRequired() {
        assertThrows(IllegalArgumentException.class,
                () -> new FullBirthDeathTree(num(1.0), num(0.5), null, null),
                "specifying neither rootAge nor originAge must be rejected");
    }

    @Test
    void eitherAgeAloneIsAccepted() {
        assertDoesNotThrow(() -> new FullBirthDeathTree(num(1.0), num(0.5), num(4.0), null));
        assertDoesNotThrow(() -> new FullBirthDeathTree(num(1.0), num(0.5), null, num(5.0)));
    }
}
