package lphy.core.io;

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

    public MacroProcessor(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    public MacroProcessor() {
        this(CONST_MACRO);
    }

    // Singleton
    private static MacroProcessor INSTANCE;
    public static MacroProcessor getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new MacroProcessor();
        }
        return INSTANCE;
    }

    public static String process(String input) {
        Map<String, String> macroMap = getInstance().getMacroMap(input);
        // TODO modify the macroMap based on user input?
        return getInstance().getResult(input, macroMap);
    }

    public Map<String, String> getMacroMap(String input) {
        Map<String, String> macroMap = new HashMap<>();

        // Populate the macroMap based on the initial matches
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            String identifier = matcher.group(1);
            String literal = matcher.group(2);
            macroMap.put(identifier, literal);
        }
        return macroMap;
    }

    public String getResult(String input, Map<String, String> macroMap) {
        Matcher replaceMatcher = pattern.matcher(input);
        StringBuilder stringBuffer = new StringBuilder();

        while (replaceMatcher.find()) {
            String replacement = macroMap.get(replaceMatcher.group(1));
            if (replacement != null) {
                // The String produced will match the sequence of characters in s treated as a literal sequence.
                // Slashes ('\') and dollar signs ('$') will be given no special meaning.
                replacement = Matcher.quoteReplacement(replacement);
                replaceMatcher.appendReplacement(stringBuffer, replacement);
            }
        }
        replaceMatcher.appendTail(stringBuffer);
        // Result contains the modified string
        return stringBuffer.toString();
    }

    public static void main(String[] args) {
        String input = "{{identifier1 = value1}} some text {{identifier2 = value2}}";

        String result = MacroProcessor.process(input);

        System.out.println(result);
    }

}

