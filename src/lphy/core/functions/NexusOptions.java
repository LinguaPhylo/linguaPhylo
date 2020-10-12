package lphy.core.functions;

import lphy.evolution.alignment.Alignment;
import lphy.evolution.alignment.CharSetAlignment;
import lphy.evolution.alignment.SimpleAlignment;
import lphy.evolution.io.NexusParser;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

/**
 * Cache {@link Alignment} and store options.
 * For example, {@link #AGE_DIRECTION} and {@link #AGE_REGEX}.
 * @author Walter Xie
 */
public class NexusOptions {

    protected static final String AGE_DIRECTION = "ageDirection";
    protected static final String AGE_REGEX = "ageRegex";

    public static final String OPT_DESC = "the map containing optional arguments and their values for reuse, " +
            "                          such as " + AGE_DIRECTION + " and " + AGE_REGEX + ".";

    // cache the data from one Nexus file
    protected Alignment cachedAlignment = null;
    protected String currentFileName = "";
    protected Map<String, String> options;
//    protected NexusParser parser;

    //*** Singleton ***//
    private static NexusOptions instance;
    private NexusOptions(){}
    // cache the data from one Nexus file
    public static NexusOptions getInstance(){
        if(instance == null)
            instance = new NexusOptions();
        return instance;
    }


    public static void validate() {

    }

    /**
     *
     * @param fileName  the name of Nexus file.
     * @param  options  keys are {@link #AGE_DIRECTION} and {@link #AGE_REGEX}.
     *                  If set new Map<String, String> options, then read file again.
     * @return either {@link SimpleAlignment} or {@link CharSetAlignment} if charsets are defined in Nexus.
     */
    public Alignment getAlignment(String fileName, Map<String, String> options, boolean ignoreCharset) {
        // refresh cache
        if (cachedAlignment == null || !currentFileName.equalsIgnoreCase(fileName) || hasOptions(options) ) {
            Objects.requireNonNull(instance).options = options;
            return readAlignment(fileName, ignoreCharset);
        }
        return cachedAlignment;
    }

    /**
     * @see #getAlignment(String, Map, boolean)
     */
    public Alignment getAlignment(String fileName, boolean ignoreCharset) {
        // refresh cache
        if (cachedAlignment == null || !currentFileName.equalsIgnoreCase(fileName) )
            return readAlignment(fileName, ignoreCharset);
        return cachedAlignment;
    }

    // read alignment(s) from file
    protected Alignment readAlignment(String fileName, boolean ignoreCharset) {
        if (fileName == null)
            throw new IllegalArgumentException("The file name can't be null!");
        currentFileName = fileName;

        final Path nexFile = Paths.get(currentFileName);
        // validate postfix
        NexusParser parser = new NexusParser(nexFile);

        String ageDirectionStr = getAgeDirectionStr();
        String ageRegxStr = getAgeRegxStr();

        // either {@link SimpleAlignment} or {@link CharSetAlignment}
        return parser.getLPhyAlignment(ignoreCharset, ageDirectionStr, ageRegxStr);
    }

    protected boolean hasOptions(Map<String, String> options) {
        if (options == null || !( options.containsKey(AGE_DIRECTION) || options.containsKey(AGE_REGEX) ) )
            return false;
        return true;
    }

    // whether to set new options
    protected boolean setOptions(Map<String, String> options) {
        if (this.options == options) return false; // include null == null
        if (this.options == null || options == null) {
            this.options = options;
            return true;
        }
        for (Map.Entry<String, String> entry : this.options.entrySet()) {
            // no key
            if (!options.containsKey(entry.getKey())) {
                this.options = options;
                return true;
            }
            // not same value
            String opt2Val = options.get(entry.getKey());
            if (!entry.getValue().equals(opt2Val)) {
                this.options = options;
                return true;
            }
        }
        return false;
    }

    public String getAgeDirectionStr() {
        return options == null ? null : options.get(AGE_DIRECTION);
    }

    public String getAgeRegxStr() {
        return options == null ? null : options.get(AGE_REGEX);
    }

}
