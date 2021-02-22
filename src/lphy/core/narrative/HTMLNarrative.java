package lphy.core.narrative;

import lphy.app.graphicalmodelcomponent.GraphicalModelComponent;
import lphy.core.LPhyParser;
import lphy.graphicalModel.Citation;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.code.CanonicalCodeBuilder;
import lphy.graphicalModel.code.CodeBuilder;
import lphy.parser.codecolorizer.DataModelToHTML;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import static lphy.graphicalModel.NarrativeUtils.sanitizeDOI;
import static lphy.graphicalModel.VectorUtils.INDEX_SEPARATOR;

public class HTMLNarrative implements Narrative {

    List<Citation> references = new ArrayList<>();
    boolean mathModeInline = false;

    static Preferences preferences = Preferences.userNodeForPackage(HTMLNarrative.class);

    public Preferences getPreferences() {
        return preferences;
    }

    @Override
    public String beginDocument(String title) {
        references.clear();
        StringBuilder builder = new StringBuilder("<html>\n\n<body>");

        if (title != "" && title != null) {
            builder.append("<h1>");
            builder.append(title);
            builder.append("</h1>");
        }
        return builder.toString();
    }

    @Override
    public String endDocument() {
        references.clear();
        return "</body>\n</html>";
    }

    /**
     * @param header the heading of the section
     * @return a string representing the start of a new section
     */
    public String section(String header) {
        return "<h2>" + header + "</h2>\n\n";
    }

    public String text(String text) {
        return text;
    }

    public String getId(Value value, boolean inlineMath) {

        String id = value.getId();



        if (inlineMath)  {
            StringBuilder builder = new StringBuilder();
            builder.append("<i>");
            if (id.indexOf(INDEX_SEPARATOR)>0) {
                String[] split = id.split("\\"+INDEX_SEPARATOR);
                if (split.length == 2) {
                    id = split[0] + subscript(split[1]);
                }
            }
            builder.append(id);
            builder.append("</i>");
            return builder.toString();
        }
        return id;
    }

    @Override
    public String startMathMode(boolean inline) {
        mathModeInline = inline;
        if (inline) {
            return "<i>";
        } else return "<p>";
    }

    @Override
    public String endMathMode() {
        if (mathModeInline) {
            return "</i>";
        } else return "</p>";
    }


    public String codeBlock(LPhyParser parser, int fontSize) {

        JTextPane dummyPane = new JTextPane();

        DataModelToHTML dataModelToHTML = new DataModelToHTML(parser, dummyPane, fontSize + "pt");

        CodeBuilder codeBuilder = new CanonicalCodeBuilder();
        String text = codeBuilder.getCode(parser);
        dataModelToHTML.parse(text);

        return dataModelToHTML.getHTML();
    }

    @Override
    public String graphicalModelBlock(GraphicalModelComponent component) {
        return "";
    }

    // JLatexMath might be useful: https://github.com/opencollab/jlatexmath
    // Maybe JEuclid to: http://jeuclid.sourceforge.net/
    @Override
    public String sum(String index, int start, int end) {
        return "∑ <sub>"+ index + "=" + start + "</sub> <sup>" + end + "</sup>";
    }

    @Override
    public String subscript(String index) {
        return "<sub>" + index + "</sub>";
    }

    public String cite(Citation citation) {

        if (citation != null && !references.contains(citation)) references.add(citation);

        if (citation == null) return "";
        StringBuilder builder = new StringBuilder();
        builder.append("<a href=\"");
        builder.append(sanitizeDOI(citation.DOI()));
        builder.append("\">(");
        String[] authors = citation.authors();
        if (authors.length > 2) {
            builder.append(authors[0]);
            builder.append(" <i>et al</i>");
        } else {
            for (int i = 0; i < authors.length; i++) {
                if (i > 0) {
                    if (i == authors.length - 1) {
                        builder.append(" and ");
                    } else {
                        builder.append(", ");
                    }
                }
                builder.append(authors[i]);
            }
        }
        builder.append("; ");
        builder.append(citation.year());
        builder.append(")</a>");
        return builder.toString();
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
