package james.core.distributions;

import java.util.Random;

public class Utils {

    private static Random random = new Random();

    public static Random getRandom() {
        return random;
    }

    public static void setRandom(Random r) {
        random = r;
    }
}
