package lphy.core.narrative;

import lphy.graphicalModel.Value;
import lphy.graphicalModel.ValueUtils;
import lphy.graphicalModel.Vector;

import static lphy.graphicalModel.VectorUtils.INDEX_SEPARATOR;

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

    /**
     * @param value the value to format the id of for LaTex document
     * @param inline  if true then added $ delimiters, otherwise assume the result will be inserted into an existing math mode environment.
     * @return the id of the given value in an appropriate math mode format for insertion into a LaTeX document, or null if the value is anonymous
     */
    public static String getMathId(Value value, boolean inline, boolean useBoldSymbol) {

        String id = value.getId();
        String canonicalId = value.getCanonicalId();

        if (value.isAnonymous() || id == null) return null;

        boolean useCanonical = !id.equals(canonicalId) && canonicalId != null;
        if (useCanonical) id = canonicalId;

        boolean isVector = value instanceof Vector || ValueUtils.isMultiDimensional(value.value());

        String indexStr = "";

        boolean containsIndexSeparator = id.contains(INDEX_SEPARATOR);
        if (containsIndexSeparator) {
            String[] split = id.split(INDEX_SEPARATOR);
            id = split[0];
            indexStr = split[1];
        }

        boolean isText = id.length() > 1 && !useCanonical;

        StringBuilder builder = new StringBuilder();

        if (inline) builder.append("$");

        if (isVector) {
            builder.append(useBoldSymbol ? "\\boldsymbol{" : "\\bm{" );
            if (isText) {
                builder.append("\\textbf{");
            }
        } else if (isText) {
            builder.append("\\textrm{");
        }

        // if canonical is being used and in a math environment then escape canonical name to get symbol
        if (useCanonical) builder.append("\\");

        builder.append(id);

        if (isText) {
            builder.append("}");
        }

        if (!indexStr.equals("")) {
            builder.append("_");
            builder.append(indexStr);
        }

        if (isVector) {
            builder.append("}");
        }

        if (inline) builder.append("$");

        return builder.toString();
    }

    public static String getFontSize(int fontSize) {

        if (fontSize < 6) return "\\tiny";

        switch (fontSize) {
            case 6:
            case 7:
                return "\\tiny";
            case 8:
            case 9:
                return "\\scriptsize";
            case 10:
                return "\\footnotesize";
            case 11:
                return "\\small";
            case 12:
            case 13:
                return "\\normalsize";
            case 14:
            case 15:
            case 16:
                return "\\large";
            case 17:
            case 18:
            case 19:
                return "\\Large";
            case 20:
            case 21:
            case 22:
            case 23:
            case 24:
                return "\\LARGE";
            case 25:
                return "\\huge";
            default:
                return "\\Huge";
        }
    }
}
