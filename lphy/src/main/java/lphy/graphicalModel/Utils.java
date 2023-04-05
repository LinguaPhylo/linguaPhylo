package lphy.graphicalModel;

import java.text.DecimalFormat;

public class Utils {

    public static DecimalFormat FORMAT = new DecimalFormat();

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
