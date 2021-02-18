package lphy.core.narrative;

import lphy.app.Symbols;
import lphy.core.LPhyParser;
import lphy.graphicalModel.Citation;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.code.CanonicalCodeBuilder;
import lphy.graphicalModel.code.CodeBuilder;
import lphy.parser.codecolorizer.DataModelToLaTeX;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

import static lphy.graphicalModel.NarrativeUtils.sanitizeDOI;

public class LaTeXNarrative implements Narrative {

    List<Citation> references = new ArrayList<>();
    List<String> keys = new ArrayList<>();
    boolean mathMode = false;
    boolean mathModeInline = false;

    public String beginDocument() {
        StringBuilder builder = new StringBuilder();
        builder.append("\\documentclass{article}\n\n");
        builder.append("\\usepackage{color}\n");
        builder.append("\\usepackage{xcolor}\n");
        builder.append("\\usepackage{alltt}\n\n");
        builder.append("\\begin{document}\n");

        return  builder.toString();
    }

    public String endDocument() {
        return "\\end{document}\n";
    }

    /**
     * @param header the heading of the section
     * @return a string representing the start of a new section
     */
    public String section(String header) {
        return "\\section{" + header + "}\n\n";
    }


    //static String[] specials = {"&", "%", "$", "#", "_", "{", "}"};

    public String text(String text) {
        if (text.startsWith("\"") && text.endsWith("\"")) {
//            // sanitize specials
//            for (String s : specials) {
//                text = text.replace(s, "\\" + s);
//            }

            // sanitize backslash
            text = text.replace("\\", "\\textbackslash{}");

//            // sanitize backslash
//            text = text.replace("~", "\\textasciitilde{}");
//
//            // sanitize backslash
//            text = text.replace("^", "\\textasciicircum{}");

        }
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

        boolean containsUnderscore = id.indexOf('_') >= 0;

        if (useCanonical) {
            if (inlineMath) {
                return "$\\" + canonicalId + "$";
            } else if (mathMode) {
                return "\\" + canonicalId;
            } else return canonicalId;
        } else if (containsUnderscore) {
            if (mathMode) {
                return id;
            } else return "$" + id + "$";
        } else {
            if (inlineMath) {
                return "{\\it " + id + "}";
            } else if (mathMode) {
                return "\\textrm{" + id + "}";
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
        mathMode = false;
        if (mathModeInline) return "$";
        return "$$";
    }

    @Override
    public String codeBlock(LPhyParser parser) {

        JTextPane dummyPane = new JTextPane();

        DataModelToLaTeX dataModelToLaTeX = new DataModelToLaTeX(parser, dummyPane);
        CodeBuilder codeBuilder = new CanonicalCodeBuilder();
        String text = codeBuilder.getCode(parser);
        dataModelToLaTeX.parse(text);

        return dataModelToLaTeX.getLatex();
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
            // \begin{thebibliography}{9}
            //
            //\bibitem{lamport94}
            //  Leslie Lamport,
            //  \textit{\LaTeX: a document preparation system},
            //  Addison Wesley, Massachusetts,
            //  2nd edition,
            //  1994.
            //
            //\end{thebibliography}

            builder.append("\\begin{thebibliography}{9}\n\n");
            for (int i = 0; i < references.size(); i++) {
                builder.append("\\bibitem{");
                builder.append(keys.get(i));
                builder.append("}\n");

                String ref = LaTeXUtils.sanitizeText(references.get(i).value());

                builder.append(ref);
                builder.append("\n\n");
            }
            builder.append("\\end{thebibliography}\n");
        }
        return builder.toString();
    }
}
