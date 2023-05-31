package lphy.core.util;

public class DecimalFormat {

    public static java.text.DecimalFormat FORMAT = new java.text.DecimalFormat();

    static {
        FORMAT.setMaximumFractionDigits(6);
    }

    public static boolean isInteger(String str) {
        try {
            int i = Integer.parseInt(str);
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }
}
