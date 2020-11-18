package lphy.evolution.io;

import lphy.graphicalModel.Value;

import java.util.Map;

/**
 * The options for {@link lphy.core.functions.ReadNexus}.
 * For example, {@link #AGE_DIRECTION} and {@link #AGE_REGEX},
 * {@link #LOC_REGEX}.
 * @author Walter Xie
 */
public final class MetaDataOptions {

    protected static final String AGE_DIRECTION = "ageDirection";
    protected static final String AGE_REGEX = "ageRegex";
    protected static final String LOC_REGEX = "locRegex";

    public static final String OPT_DESC = "the map containing optional arguments and their values for reuse, " +
            "                          such as " + AGE_DIRECTION + " and " + AGE_REGEX + ".";

    public static String getAgeDirectionStr(Value<Map<String, String>> optionsVal) {
        Map<String, String> options = optionsVal == null ? null : optionsVal.value();
        return options == null ? null : options.get(AGE_DIRECTION);
    }

    /**
     * @param optionsVal
     * @return The string of regex to extract ages from taxa labels.
     */
    public static String getAgeRegxStr(Value<Map<String, String>> optionsVal) {
        Map<String, String> options = optionsVal == null ? null : optionsVal.value();
        return options == null ? null : options.get(AGE_REGEX);
    }

    public static boolean isSame(Map<String, String> options1, Map<String, String> options2) {
        if (options1 == options2) return true; // include null == null
        if (options1 == null || options2 == null) return false;
        for (Map.Entry<String, String> entry : options1.entrySet()) {
            // no key
            if (!options2.containsKey(entry.getKey())) return false;
            // not same value
            String opt2Val = options2.get(entry.getKey());
            if (!entry.getValue().equals(opt2Val)) return false;
        }
        return true;
    }

    /**
     * @param optionsVal
     * @return  The string of regex to extract locations from taxa labels.
     */
    public static String getLocRegxStr(Value<Map<String, String>> optionsVal) {
        Map<String, String> options = optionsVal == null ? null : optionsVal.value();
        return options == null ? null : options.get(LOC_REGEX);
    }

}
