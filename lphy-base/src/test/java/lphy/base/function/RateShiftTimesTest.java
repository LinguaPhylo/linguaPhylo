package lphy.base.function;

import lphy.core.model.Value;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class RateShiftTimesTest {

    @Test
    void testMonthlyIntervals() {
        // MRSI = 2020.5 (approx July 2020), from = 2020.0 (Jan 2020), to = 2019.5 (approx July 2019)
        // Monthly intervals should produce ~6 rate shift times
        Value<Number> mrsi = new Value<>("mrsi", 2020.5);
        Value<Number> from = new Value<>("from", 2020.0);
        Value<Number> to = new Value<>("to", 2019.5);
        Value<String> interval = new Value<>("interval", "0-1-0");

        RateShiftTimes fn = new RateShiftTimes(mrsi, from, to, interval);
        Value<Double[]> result = fn.apply();
        Double[] shifts = result.value();

        // Should have roughly 7 entries (from Jan 2020 to Jul 2019 inclusive, monthly)
        assertTrue(shifts.length >= 6 && shifts.length <= 8,
                "Expected ~7 monthly shifts, got " + shifts.length);

        // First shift should be approximately mrsi - from = 0.5
        assertEquals(0.5, shifts[0], 0.02, "First shift should be ~0.5 years before present");

        // Shifts should be ascending
        for (int i = 1; i < shifts.length; i++) {
            assertTrue(shifts[i] > shifts[i - 1],
                    "Shifts should be ascending: " + shifts[i] + " <= " + shifts[i - 1]);
        }

        // Last shift should be approximately mrsi - to = 1.0
        assertEquals(1.0, shifts[shifts.length - 1], 0.1,
                "Last shift should be ~1.0 years before present");
    }

    @Test
    void testYearlyIntervals() {
        // MRSI = 2020.0, from = 2019.0, to = 2015.0, yearly
        Value<Number> mrsi = new Value<>("mrsi", 2020.0);
        Value<Number> from = new Value<>("from", 2019.0);
        Value<Number> to = new Value<>("to", 2015.0);
        Value<String> interval = new Value<>("interval", "1-0-0");

        RateShiftTimes fn = new RateShiftTimes(mrsi, from, to, interval);
        Value<Double[]> result = fn.apply();
        Double[] shifts = result.value();

        // Should have 5 entries: 1.0, 2.0, 3.0, 4.0, 5.0
        assertEquals(5, shifts.length, "Expected 5 yearly shifts");
        assertEquals(1.0, shifts[0], 0.01);
        assertEquals(2.0, shifts[1], 0.01);
        assertEquals(3.0, shifts[2], 0.01);
        assertEquals(4.0, shifts[3], 0.01);
        assertEquals(5.0, shifts[4], 0.01);
    }

    @Test
    void testInvalidIntervalFormat() {
        Value<Number> mrsi = new Value<>("mrsi", 2020.0);
        Value<Number> from = new Value<>("from", 2019.0);
        Value<Number> to = new Value<>("to", 2015.0);
        Value<String> interval = new Value<>("interval", "bad");

        RateShiftTimes fn = new RateShiftTimes(mrsi, from, to, interval);
        assertThrows(IllegalArgumentException.class, fn::apply);
    }

    @Test
    void testZeroIntervalThrows() {
        Value<Number> mrsi = new Value<>("mrsi", 2020.0);
        Value<Number> from = new Value<>("from", 2019.0);
        Value<Number> to = new Value<>("to", 2015.0);
        Value<String> interval = new Value<>("interval", "0-0-0");

        RateShiftTimes fn = new RateShiftTimes(mrsi, from, to, interval);
        assertThrows(IllegalArgumentException.class, fn::apply);
    }

    @Test
    void testDecimalYearToDateRoundTrip() {
        // Test the helper methods
        double decYear = 2020.5;
        LocalDate date = RateShiftTimes.decimalYearToDate(decYear);
        double roundTrip = RateShiftTimes.dateToDecimalYear(date);
        assertEquals(decYear, roundTrip, 0.005, "Round trip should preserve decimal year");
    }
}
