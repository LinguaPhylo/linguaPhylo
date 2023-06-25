package lphy.core.logger;

import lphy.core.model.RandomVariable;
import lphy.core.model.Value;

public class ValueLoggerUtils {

    public static boolean isValueLoggable(Value randomValue) {
        return randomValue instanceof RandomVariable ||
                // random value but no anonymous
                (randomValue.isRandom() && !randomValue.isAnonymous());
    }

}
