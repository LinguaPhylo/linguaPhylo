package lphystudio.app.narrative;

import lphy.core.graphicalmodel.components.Value;
import lphy.core.graphicalmodel.components.ValueUtils;
import lphy.core.graphicalmodel.types.Vector;
import lphy.core.util.Symbols;

import java.util.List;

import static lphy.core.vectorization.VectorUtils.INDEX_SEPARATOR;

public class LaTeXUtils {

    static String[] specials = {
            "&",
            "–", // endash
            "í", "_"};

    static String[] replacements = {
            "\\&",
            "--",
            "\\'{i}", "\\_"
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
        // empty array
//        if (value == null) return "";

        String id = value.getId();
        if (value.isAnonymous() || id == null) return null;

        List<Symbols.Block> blocks = Symbols.getCanonicalizedName(id);

        boolean isVector = value instanceof Vector || ValueUtils.isMultiDimensional(value.value());

        String indexStr = "";

        Symbols.Block lastBlock = blocks.get(blocks.size()-1);

        StringBuilder builder = new StringBuilder();

        if (inline) builder.append("$");

        if (isVector) {
            builder.append(useBoldSymbol ? "\\boldsymbol{" : "\\bm{" );
        }

        for (int i = 0; i < blocks.size(); i++) {
            Symbols.Block block = blocks.get(i);
            String s = block.string;
            boolean isCanonical = block.isCanonicalized;
            boolean isText = !isCanonical && s.length() > 1;

            if (isText) {
                builder.append(isVector ? "\\textbf{" : "\\textrm{" );

                // check for index if this is the last block and is ascii text
                if (block == lastBlock) {
                    boolean containsIndexSeparator = s.contains(INDEX_SEPARATOR);
                    if (containsIndexSeparator) {
                        indexStr = s.substring(s.lastIndexOf(INDEX_SEPARATOR)+1);
                        s = s.substring(0, s.lastIndexOf(INDEX_SEPARATOR));
                    }
                }

                s = LaTeXUtils.sanitizeText(s);

            } else if (isCanonical) {
                builder.append("\\");
            }

            builder.append(s);

            if (isText) {
                builder.append("}");
            }

            if (indexStr.length() > 0) {
                builder.append('_');
                builder.append("{");
                builder.append(indexStr);
                builder.append("}");
            }
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
