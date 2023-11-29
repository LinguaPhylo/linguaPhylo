package lphy.core.io;

import lphy.core.logger.LoggerUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Inspired by templating languages e.g. Mustache.
 * Only support {{identifier = default_value}}
 */
public class MacroProcessor {

    // {{n = 10}}
    public static final String CONST_MACRO = "\\{\\{\\s*(\\w+)\\s*=\\s*(.*?)\\s*\\}\\}";
    private final Pattern pattern;

    // init by user input (e.g. cmd args), if map key does not have the var name when per-processing script,
    // then use the default from macro.
    Map<String, String> macroMap;

    public MacroProcessor(String regex, String[] constants) {
        this.pattern = Pattern.compile(regex);
        macroMap = new HashMap<>();
        if (constants != null)
            // init macroMap based on user input
            parseConstants(constants);
    }

    /**
     * Process the marco in a lphy script, such as {{n = 10}}.
     * @param constants   constants inputted by user (e.g. command line arguments),
     *                    where each string element looks like n=10.
     *                    Null, if no user input.
     */
    public MacroProcessor(String[] constants) {
        this(CONST_MACRO, constants);
    }


    /**
     * @param input lphy string before preprocessing macros.
     * @return a lphy string after replacing all macro into actual values.
     */
    public String process(String input) {
        assignDefaultValues(input);
        return getResult(input);
    }


    /**
     * Parse command line argument into {@link #macroMap}.
     * E.g. -D n=12;L=100 or -D n=20"
     *
     * @param constants
     */
    private void parseConstants(String[] constants) {
        for (String arg : constants) {
            // n = 12
            if (arg.contains("=")) {
                // the pattern will be applied at most limit - 1 times,
                // the array's length will be no greater than limit,
                // and the array's last entry will contain all input beyond the last matched delimiter.
                String[] varVal = arg.split("=", 2);
                if (varVal.length == 2) {
                    // trim spaces
                    macroMap.put(varVal[0].trim(), varVal[1].trim());
                }
            }
        }
    }

    private void assignDefaultValues(String input) {
        // Populate the macroMap based on the initial matches
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            String identifier = matcher.group(1);
            String literal = matcher.group(2);
            // use the default from macro, only when the var name not exist
            if (!macroMap.containsKey(identifier))
                macroMap.put(identifier, literal);
        }
    }

    private String getResult(String input) {
        if (macroMap.isEmpty()) return input;

        Matcher replaceMatcher = pattern.matcher(input);
        StringBuilder stringBuffer = new StringBuilder();

        while (replaceMatcher.find()) {
            String replacement = macroMap.get(replaceMatcher.group(1));
            if (replacement != null) {
                // The String produced will match the sequence of characters in s treated as a literal sequence.
                // Slashes ('\') and dollar signs ('$') will be given no special meaning.
                replacement = Matcher.quoteReplacement(replacement);
                replaceMatcher.appendReplacement(stringBuffer, replacement);

                LoggerUtils.log.info("Macro replaces the value of constant var : " + replaceMatcher.group(1) +
                        " = " + replacement);
            }
        }
        replaceMatcher.appendTail(stringBuffer);
        // Result contains the modified string
        return stringBuffer.toString();
    }

    public static void main(String[] args) {
        String input = "{{identifier1 = value1}} some text {{identifier2 = value2}}";

        MacroProcessor macroProcessor = new MacroProcessor(null);
        String result = macroProcessor.process(input);

        System.out.println(result);
    }

}

