package lphystudio.app.narrative;

import lphy.core.codebuilder.CanonicalCodeBuilder;
import lphy.core.graphicalmodel.GraphicalModel;
import lphy.core.graphicalmodel.components.Citation;
import lphy.core.graphicalmodel.components.Value;
import lphy.core.layeredgraph.ProperLayeredGraph;
import lphy.core.narrative.Narrative;
import lphy.core.parser.LPhyMetaParser;
import lphystudio.core.theme.ThemeColours;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import static lphy.core.graphicalmodel.vectorization.VectorUtils.INDEX_SEPARATOR;
import static lphy.core.narrative.NarrativeUtils.getURL;

public class HTMLNarrative implements Narrative {

    public static final String TITLE_TAG = "h1";
    public static final String SECTION_TAG = "h2";

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
            builder.append("<" + TITLE_TAG + ">");
            builder.append(title);
            builder.append("</" + TITLE_TAG + ">");
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
        return "<" + SECTION_TAG + ">" + header + "</" + SECTION_TAG + ">\n\n";
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
    public String startMathMode(boolean inline, boolean allowMultiline) {
        mathModeInline = inline;
        if (inline) {
            return "<i>";
        } else return "<p>";
    }

    @Override
    public String mathAlign() {
        return "";
    }

    @Override
    public String mathNewLine() {
        return "<br>";
    }

    @Override
    public String endMathMode() {
        if (mathModeInline) {
            return "</i>";
        } else return "</p>";
    }


    public String codeBlock(LPhyMetaParser parser, int fontSize) {

        JTextPane dummyPane = new JTextPane();

        DataModelToHTML dataModelToHTML = new DataModelToHTML(parser, dummyPane, fontSize + "pt");

        CanonicalCodeBuilder codeBuilder = new CanonicalCodeBuilder();
        String text = codeBuilder.getCode(parser);
        dataModelToHTML.parse(text);

        return dataModelToHTML.getHTML();
    }

    @Override
    public String graphicalModelBlock(LPhyMetaParser parser, ProperLayeredGraph properLayeredGraph) {
        return "";
    }

    /**
     * @param latex   Latex contents.
     * @return  string after removing begin equation and end equation if they exist
     */
    public String rmLatexEquation(String latex) {
        latex = latex.replaceAll("\\\\begin\\{equation.}", "");
        latex = latex.replaceAll("\\\\end\\{equation.}", "");
        return latex;
    }

    public String posterior(LPhyMetaParser parser) {

        String latex = GraphicalModel.Utils.getInferenceStatement(parser, new LaTeXNarrative());

        // remove begin equation and end equation if they exist
        latex = rmLatexEquation(latex);
        try {
            Path tempFile = Files.createTempFile("temp-", ".png");
            generateLatexImage(latex, tempFile.toFile());

            StringBuilder builder = new StringBuilder();
            builder.append("<img src=\"");
            builder.append(tempFile.toUri());
            builder.append("\"></img>");

            return builder.toString();

        } catch (IOException ex) {
            String html = GraphicalModel.Utils.getInferenceStatement(parser, this);
            return html;
        }
    }

    public void generateLatexImage(String formula, File out) throws IOException {
        TeXIcon ti = generateLatexIcon(formula);
        BufferedImage bimg = new BufferedImage(ti.getIconWidth(), ti.getIconHeight(), BufferedImage.TYPE_4BYTE_ABGR);

        Graphics2D g2d = bimg.createGraphics();

        g2d.setColor(ThemeColours.getBackgroundColor());
        g2d.fillRect(0,0,ti.getIconWidth(),ti.getIconHeight());
        JLabel jl = new JLabel();
        jl.setForeground(ThemeColours.getDefaultColor());
        ti.paintIcon(jl, g2d, 0, 0);

        ImageIO.write(bimg, "png", out);
    }

    public TeXIcon generateLatexIcon(String formula) {
        TeXFormula tf = new TeXFormula(formula);
        return tf.createTeXIcon(TeXConstants.STYLE_DISPLAY, 18);
    }

    // JLatexMath might be useful: https://github.com/opencollab/jlatexmath
    // Maybe JEuclid to: http://jeuclid.sourceforge.net/
    @Override
    public String product(String index, String start, String end) {
        return "∏ <sub>"+ index + "=" + start + "</sub> <sup>" + end + "</sup>";
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
        builder.append(getURL(citation));
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
            builder.append("<" + SECTION_TAG + ">References</h2>\n");

            builder.append("<ul>");
            for (Citation citation : references) {
                builder.append("<li>");
                builder.append(citation.value());
                String url = getURL(citation);
                if (url.length() > 0) {
                    builder.append(" <a href=\"" + url + "\">" + url + "</a>");
                }
                builder.append("</li>\n");
            }
            builder.append("</ul>\n");
        }
        return builder.toString();
    }
}
