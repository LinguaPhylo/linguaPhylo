package lphy.core.model.component;

public class NumberUtils {

    public static boolean isInteger(String str) {
        try {
            int i = Integer.parseInt(str);
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

}
