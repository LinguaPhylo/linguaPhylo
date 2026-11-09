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

        // Values are interval START times: the most recent predictor window ends at
        // mrsi - from = 0.5 and therefore starts one month closer to the present.
        assertEquals(0.5 - 1.0/12, shifts[0], 0.02,
                "First shift is the START of the most recent window, ~one month before 0.5");

        // Shifts should be ascending
        for (int i = 1; i < shifts.length; i++) {
            assertTrue(shifts[i] > shifts[i - 1],
                    "Shifts should be ascending: " + shifts[i] + " <= " + shifts[i - 1]);
        }

        // Last shift starts the oldest window, one month more recent than mrsi - to = 1.0
        assertEquals(1.0 - 1.0/12, shifts[shifts.length - 1], 0.1,
                "Last shift is the START of the oldest window");
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

        // One value per predictor window, giving each window's START.
        // The windows end at 1..5 years before the most recent sample (which is what
        // MASCOT's BEAUti editor lists), so they start at 0..4.
        assertEquals(5, shifts.length, "Expected 5 yearly shifts");
        assertEquals(0.0, shifts[0], 0.01);
        assertEquals(1.0, shifts[1], 0.01);
        assertEquals(2.0, shifts[2], 0.01);
        assertEquals(3.0, shifts[3], 0.01);
        assertEquals(4.0, shifts[4], 0.01);
    }

    @Test
    void startTimesAreOneWindowAheadOfTheBeautiEndTimes() {
        // MASCOT's BEAUti rate shift editor lists the END of each predictor window.
        // StructuredCoalescentRateShifts consumes START times, so shifts[i+1] must equal
        // the BEAUti value for window i -- that is what keeps predictor row i attached to
        // window i once LPhyBEAST translates back to end times for MASCOT. Verified against
        // the Ebola GLM tutorial, where being one out lags every weekly case count.
        Value<Number> mrsi = new Value<>("mrsi", 2020.0);
        Value<Number> from = new Value<>("from", 2019.0);
        Value<Number> to = new Value<>("to", 2015.0);
        Value<String> interval = new Value<>("interval", "1-0-0");

        Double[] starts = new RateShiftTimes(mrsi, from, to, interval).apply().value();
        double[] beautiEnds = {1.0, 2.0, 3.0, 4.0, 5.0};   // what BEAUti writes for this grid

        assertEquals(beautiEnds.length, starts.length,
                "one value per predictor window, same count as BEAUti");
        for (int i = 0; i < starts.length - 1; i++)
            assertEquals(beautiEnds[i], starts[i + 1], 1e-9,
                    "start of window i+1 must be the end of window i");
        assertEquals(0.0, starts[0], 1e-9, "the most recent window starts at the present");
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
