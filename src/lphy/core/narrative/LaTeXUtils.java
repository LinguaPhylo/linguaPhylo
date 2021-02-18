package lphy.core.narrative;

public class LaTeXUtils {

    public static String sanitizeText(String text) {
        text = text.replace("&", "\\&");
        text = text.replace("–", "--");

        return text;
    }
}
