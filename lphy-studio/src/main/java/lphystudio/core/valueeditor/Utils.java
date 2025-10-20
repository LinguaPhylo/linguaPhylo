package lphystudio.core.valueeditor;

public final class Utils {

    /**
     * IEEE-754 keeps track of the sign bit, even when the numeric value is zero.
     * This allows certain mathematical operations to preserve direction of approach to zero
     */

    public static double cleanZero(double x) {
        return x == 0.0 ? 0.0 : x;
    }

    public static Double cleanZero(Double x) {
        return x != null && x == 0.0 ? Double.valueOf(0.0) : x;
    }
}
