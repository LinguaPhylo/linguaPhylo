package lphy.base.function;

import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

/**
 * Computes rate shift times from date inputs and an interval format string,
 * matching the behaviour of MASCOT's BEAUti rate shift editor.
 * <p>
 * Rate shift times are returned in "time before most recent sample" units
 * (ascending order), suitable for use with {@code StructuredCoalescentRateShifts}.
 *
 * @author Toby (requested), Claude (implementation)
 */
public class RateShiftTimes extends DeterministicFunction<Double[]> {

    public static final String mostRecentSampleDateParamName = "mostRecentSampleDate";
    public static final String fromParamName = "from";
    public static final String toParamName = "to";
    public static final String intervalParamName = "interval";

    public RateShiftTimes(
            @ParameterInfo(name = mostRecentSampleDateParamName,
                    description = "the date of the most recent sample as a decimal year (e.g. 2020.5).")
            Value<Number> mostRecentSampleDate,
            @ParameterInfo(name = fromParamName,
                    description = "the most recent predictor date as a decimal year (e.g. 2020.0).")
            Value<Number> from,
            @ParameterInfo(name = toParamName,
                    description = "the final (oldest) predictor date as a decimal year (e.g. 2018.0).")
            Value<Number> to,
            @ParameterInfo(name = intervalParamName,
                    description = "the predictor interval as a Y-M-D format string, " +
                            "e.g. \"0-1-0\" for monthly, \"1-0-0\" for yearly, \"0-0-7\" for weekly.")
            Value<String> interval) {

        setParam(mostRecentSampleDateParamName, mostRecentSampleDate);
        setParam(fromParamName, from);
        setParam(toParamName, to);
        setParam(intervalParamName, interval);
    }

    @Override
    @GeneratorInfo(name = "rateShiftTimes",
            description = "Computes rate shift times from date inputs and an interval format string, " +
                    "matching MASCOT's BEAUti rate shift editor. Returns times in 'time before most " +
                    "recent sample' units. The interval string uses Y-M-D format (e.g. \"0-1-0\" " +
                    "for monthly intervals).")
    public Value<Double[]> apply() {
        double mrsi = ((Value<Number>) getParams().get(mostRecentSampleDateParamName)).value().doubleValue();
        double fromVal = ((Value<Number>) getParams().get(fromParamName)).value().doubleValue();
        double toVal = ((Value<Number>) getParams().get(toParamName)).value().doubleValue();
        String intervalStr = ((Value<String>) getParams().get(intervalParamName)).value();

        // Parse interval string "Y-M-D"
        String[] parts = intervalStr.split("-");
        if (parts.length != 3) {
            throw new IllegalArgumentException(
                    "Interval string must be in Y-M-D format (e.g. \"0-1-0\"), got: " + intervalStr);
        }
        int years = Integer.parseInt(parts[0].trim());
        int months = Integer.parseInt(parts[1].trim());
        int days = Integer.parseInt(parts[2].trim());

        if (years == 0 && months == 0 && days == 0) {
            throw new IllegalArgumentException("Interval must be non-zero.");
        }

        Period period = Period.of(years, months, days);

        // Convert decimal years to LocalDate for calendar-aware arithmetic
        LocalDate mrsiDate = decimalYearToDate(mrsi);
        LocalDate fromDate = decimalYearToDate(fromVal);
        LocalDate toDate = decimalYearToDate(toVal);

        // Generate rate shift times by stepping backward from 'from' toward 'to'
        List<Double> shifts = new ArrayList<>();
        LocalDate currentDate = fromDate;
        double currentTime = -(dateToDecimalYear(fromDate) - dateToDecimalYear(mrsiDate));
        double endTime = -(dateToDecimalYear(toDate) - dateToDecimalYear(mrsiDate));

        while (currentTime <= endTime) {
            shifts.add(currentTime);
            currentDate = currentDate.minus(period);
            currentTime = -(dateToDecimalYear(currentDate) - dateToDecimalYear(mrsiDate));
        }

        return new Value<>(null, shifts.toArray(new Double[0]), this);
    }

    /**
     * Convert a decimal year (e.g. 2020.5) to a LocalDate.
     */
    static LocalDate decimalYearToDate(double decimalYear) {
        int year = (int) Math.floor(decimalYear);
        boolean isLeap = LocalDate.of(year, 1, 1).isLeapYear();
        int daysInYear = isLeap ? 366 : 365;
        int dayOfYear = (int) Math.round((decimalYear - year) * daysInYear) + 1;
        // clamp to valid range
        dayOfYear = Math.max(1, Math.min(dayOfYear, daysInYear));
        return LocalDate.ofYearDay(year, dayOfYear);
    }

    /**
     * Convert a LocalDate to a decimal year.
     */
    static double dateToDecimalYear(LocalDate date) {
        int daysInYear = date.isLeapYear() ? 366 : 365;
        return date.getYear() + (date.getDayOfYear() - 1.0) / daysInYear;
    }
}
