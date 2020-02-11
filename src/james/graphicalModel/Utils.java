package james.graphicalModel;

import java.text.DecimalFormat;

public class Utils {

    public static DecimalFormat FORMAT = new DecimalFormat();

    static {
        FORMAT.setMaximumFractionDigits(8);
    }
}
