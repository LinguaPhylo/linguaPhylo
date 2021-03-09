package lphy.core.narrative;

import lphy.app.GraphicalLPhyParser;
import lphy.app.GraphicalModelListener;
import lphy.app.LinguaPhyloStudio;
import lphy.app.NarrativePanel;
import lphy.app.graphicalmodelcomponent.GraphicalModelComponent;
import lphy.core.LPhyParser;
import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GenerativeDistribution;
import lphy.graphicalModel.GraphicalModel;
import lphy.graphicalModel.Value;
import lphy.parser.REPL;
import lphy.utils.LoggerUtils;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Create markdown files for Jekyll containing the sections
 * (code, data, model, posterior, graphical model)
 * to introduce LPhy scripts for the tutorial.
 * Input: *.lphy
 * Output: narrative.md, references.md
 */
public class NarrativeCreator {
    public static final String DETAILS = "details";
    Path wd; // working dir
    GraphicalLPhyParser parser;
    HTMLNarrative htmlNarrative;
    LaTeXNarrative latexNarrative;

    StringBuilder narrative = new StringBuilder();
    StringBuilder references = new StringBuilder();

//    static Preferences preferences = Preferences.userNodeForPackage(NarrativeCreator.class);

    public NarrativeCreator(String lphyFileName) throws IOException {
        LPhyParser simplyParser = readFile(lphyFileName);
        // A wrapper for any implementation of LPhyParser that will be used in the Studio
        this.parser = new GraphicalLPhyParser(simplyParser);

        htmlNarrative = new HTMLNarrative();
        latexNarrative = new LaTeXNarrative();

        GraphicalModelComponent component = new GraphicalModelComponent(this.parser);

        createNarrative(component);

    }


    private void createNarrative(GraphicalModelComponent component) {

        // assume Data, Model, Posterior stay together with this order
        // so wrap them with <detail> ... </detail>, which can click to expand.
        for (Section section : Section.values()) {

            switch (section) {
                case Data -> {
                    String dataSec = GraphicalModel.Utils.getNarrative(parser, htmlNarrative, true, false);
                    narrative.append("<" + DETAILS + ">\n");
                    narrative.append("<summary>Click to expand the auto-generated narrative from LPhyStudio ...</summary>\n");
                    narrative.append(dataSec);
                }
                case Model -> {
                    String modelSec = GraphicalModel.Utils.getNarrative(parser, htmlNarrative, false, true);
                    narrative.append(modelSec);
                }
                case Code -> {
                    narrative.append(section("Code"));
                    narrative.append(htmlNarrative.codeBlock(parser, 11));
                    narrative.append("\n");
                }
                case Posterior -> {
                    narrative.append(section("Posterior"));
                    String pos = GraphicalModel.Utils.getInferenceStatement(parser, latexNarrative);
                    pos = htmlNarrative.rmLatexEquation(pos);
                    // replace equation to $$ ... $$
                    narrative.append("$$\n").append(pos).append("\n$$\n\n");
                    narrative.append("</" + DETAILS + ">\n");
                }
                case References -> {
                    String ref = htmlNarrative.referenceSection();
                    references.append(ref);
                }
                case GraphicalModel -> {
                    narrative.append(section("Graphical Model"));
                    //TODO narrative.append(latexNarrative.graphicalModelBlock(component));
                    narrative.append("\n");
                }
            }
        }

        validateTags(narrative);
    }

    public void writeNarrative() throws IOException {
        File narF = new File(wd.toString(), "narrative.md");
        PrintWriter writer = new PrintWriter(new FileWriter(narF));
        writer.println(narrative.toString());
        writer.flush();
        writer.close();
        System.out.println("Write narrative to " + narF.getAbsolutePath());

        File refFN = new File(wd.toString(), "references.md");
        writer = new PrintWriter(new FileWriter(refFN));
        String ref = references.toString();
        // convert MD, otherwise will have a gap when add new ref in MD
        ref = refHtmlToMardown(ref);
        // rm all \n, otherwise will have a gap
        ref = trimEndNewLine(ref);
        writer.print(ref);
        writer.flush();
        writer.close();
        System.out.println("Write references to " + refFN.getAbsolutePath());
    }


    private LPhyParser readFile(String lphyFileName) throws IOException {
        if (!lphyFileName.endsWith(".lphy"))
            throw new IllegalArgumentException("Invalid LPhy file name " + lphyFileName + " !");
        File file = new File(lphyFileName);

        BufferedReader reader = new BufferedReader(new FileReader(file));
        LPhyParser parser = new REPL();
        parser.source(reader);

        wd = file.toPath().getParent();
        return parser;
    }

    private String section(String header) {
        return "## " + header + "\n\n";
    }

    private String refHtmlToMardown(String ref) {
        ref = ref.replace("<ul>", "");
        ref = ref.replace("</ul>", "");
        ref = ref.replace("<li>", "* ");
        ref = ref.replace("</li>", "");
        return ref;
    }

    private String trimEndNewLine(String str) {
        final int tailIndex = 3;
        //  a plain-text match, no regex
        if (str.endsWith("\n") && str.length() > tailIndex) {
            int mark = str.lastIndexOf("\n")-tailIndex;
            StringBuilder builder = new StringBuilder(str.substring(0, mark));
            String end = str.substring(mark).replace("\n", "");
            builder.append(end);
            return builder.toString();
        }
        return str;
    }

    private void validateTags(StringBuilder narrative) {
        int openTag = narrative.indexOf("<" + DETAILS + ">");
        int closeTag = narrative.indexOf("</" + DETAILS + ">");
        if (openTag >= closeTag)
            throw new IllegalStateException("Invalid position of <" + DETAILS + "> !");
    }

    private String replaceHTMLSection(String html) {
        return html.replaceFirst("<" + HTMLNarrative.SECTION_TAG +
                ">(.*)</" + HTMLNarrative.SECTION_TAG + ">", section("$1"));
    }

    // input: examples/h5n1.lphy
    public static void main(String[] args) throws IOException {
        // TODO set wd
        if (args.length != 1)
            throw new IllegalArgumentException("Expecting LPhy file name !");

        // always the last arg, examples/h5n1.lphy
        String lphyFileName = args[args.length - 1];

        NarrativeCreator narrativeCreator = new NarrativeCreator(lphyFileName);
        narrativeCreator.writeNarrative();
     }

}
