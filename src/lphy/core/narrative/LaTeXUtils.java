package lphy.core.narrative;

public class LaTeXUtils {

    static String[] specials = {
            "&",
            "–", // endash
            "í"};

    static String[] replacements = {
            "\\&",
            "--",
            "\\'{i}"
    };

    public static String sanitizeText(String text) {
        for (int i = 0; i < specials.length; i++) {
            text = text.replace(specials[i], replacements[i]);
        }
        return text;
    }
}
