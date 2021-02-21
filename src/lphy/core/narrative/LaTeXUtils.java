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

    public static String getFontSize(int fontSize) {

        if (fontSize < 6) return "\\tiny";

        switch (fontSize) {
            case 6: case 7: return "\\tiny";
            case 8: case 9: return "\\scriptsize";
            case 10: return "\\footnotesize";
            case 11: return "\\small";
            case 12: case 13: return "\\normalsize";
            case 14: case 15: case 16: return "\\large";
            case 17: case 18: case 19: return "\\Large";
            case 20: case 21: case 22: case 23: case 24: return "\\LARGE";
            case 25: return "\\huge";
            default: return "\\Huge";
        }
    }
}
