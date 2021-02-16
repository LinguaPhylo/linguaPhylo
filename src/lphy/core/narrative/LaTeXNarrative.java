package lphy.core.narrative;

import lphy.app.Symbols;
import lphy.graphicalModel.Citation;
import lphy.graphicalModel.Value;

import java.util.ArrayList;
import java.util.List;

import static lphy.graphicalModel.NarrativeUtils.sanitizeDOI;

public class LaTeXNarrative implements Narrative {

    List<Citation> references = new ArrayList<>();
    List<String> keys = new ArrayList<>();
    boolean mathMode = false;
    boolean mathModeInline = false;

    /**
     * @param header the heading of the section
     * @return a string representing the start of a new section
     */
    public String section(String header) {
        return "\\section{" + header + "}\n\n";
    }

    public String text(String text) {
        return text;
    }

    public String cite(Citation citation) {

        if (citation != null) {
            if (!references.contains(citation)) {
                references.add(citation);
                keys.add(generateKey(citation, keys));
            }

            return "\\cite{" + getKey(citation) + "}";
        }

        return null;
    }

    public String getId(Value value, boolean inlineMath) {

        String id = value.getId();
        String canonicalId = value.getCanonicalId();

        if (value.isAnonymous()) return null;

        boolean useCanonical = !id.equals(canonicalId);

        if (useCanonical) {
            if (inlineMath) {
                return "$\\" + canonicalId + "$";
            } else if (mathMode) {
                return "\\" + canonicalId;
            } else return canonicalId;
        } else {
            if (inlineMath) {
                return "{\\it " + id + "}";
            } else if (mathMode) {
                return "\\text{" + id + "}";
            } else return id;
        }
    }

    public String symbol(String symbol) {
        String canonical = Symbols.getCanonical(symbol);
        if (!canonical.equals(symbol)) return "\\" + canonical;
        return symbol;
    }

    @Override
    public String startMathMode(boolean inline) {
        mathMode = true;
        mathModeInline = inline;
        if (inline) return "$";
        return "$$";
    }

    @Override
    public String endMathMode() {
        if (mathModeInline) return "$";
        return "$$";
    }


    private String getKey(Citation citation) {
        return keys.get(references.indexOf(citation));
    }

    private String generateKey(Citation citation, List<String> existingKeys) {

        String title = citation.title();
        String firstWord = title.split(" ")[0];

        String key = citation.authors()[0] + citation.year() + firstWord;

        if (existingKeys.contains(key)) {
            int index = 1;
            while (existingKeys.contains(key)) {
                key = citation.authors()[0] + citation.year() + firstWord + index;
                index += 1;
            }
        }
        return key;
    }

    @Override
    public void clearReferences() {
        references.clear();
    }

    public String referenceSection() {
        StringBuilder builder = new StringBuilder();
        if (references.size() > 0) {
            builder.append("<h2>References</h2>\n");

            builder.append("<ul>");
            for (Citation citation : references) {
                builder.append("<li>");
                builder.append(citation.value());
                if (citation.DOI().length() > 0) {
                    String url = sanitizeDOI(citation.DOI());
                    builder.append(" <a href=\"" + url + "\">" + url + "</a>");
                }
                builder.append("</li>\n");
            }
            builder.append("</ul>\n");
        }
        return builder.toString();
    }
}
