package lphy.core.narrative;

import lphy.app.GraphicalLPhyParser;
import lphy.app.graphicalmodelcomponent.GraphicalModelComponent;
import lphy.core.LPhyParser;
import lphy.graphicalModel.GraphicalModel;
import lphy.parser.REPL;

import java.io.*;
import java.nio.file.Path;

/**
 * Create markdown files for Jekyll containing the sections
 * (code, data, model, posterior, graphical model)
 * to introduce LPhy scripts for the tutorial.
 * Input: *.lphy
 * Output: narrative.md, references.md
 */
public class NarrativeCreator {
    public static final String DIV_BOX_BACKGR_COLOUR =
            "<div id=\"auto-generated\" style=\"background-color: #DCDCDC; " +
                    "padding: 10px; border: 1px solid gray; margin: 0; \">";
    Path wd; // working dir
    GraphicalLPhyParser parser;
    HTMLNarrative htmlNarrative;
    LaTeXNarrative latexNarrative;

    StringBuilder code = new StringBuilder();
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

        // assume Data, Model, Posterior stay together with this order,
        // so wrap them with <detail> ... </detail>, which can click to expand,
        // and <div id="auto-generated"> for a box with diff background colour.
        for (Section section : Section.values()) {

            switch (section) {
                case Code -> {
                    code.append(section("Code"));
                    code.append(htmlNarrative.codeBlock(parser, 11));
                    code.append("\n");
                }
                case GraphicalModel -> {
                    code.append(section("Graphical Model"));
                    //TODO narrative.append(latexNarrative.graphicalModelBlock(component));
                    code.append("\n");
                }
                // move Data, Model, Posterior to the bottom of webpage
                case Data -> {
                    String dataSec = GraphicalModel.Utils.getNarrative(parser, htmlNarrative, true, false);
//                    narrative.append("\n<details>\n");
//                    narrative.append("<summary>Click to expand the auto-generated narrative from LPhyStudio ...</summary>\n");
                    narrative.append("\n" + DIV_BOX_BACKGR_COLOUR + "\n");
                    narrative.append(dataSec);
                }
                case Model -> {
                    String modelSec = GraphicalModel.Utils.getNarrative(parser, htmlNarrative, false, true);
                    narrative.append(modelSec);
                }
                case Posterior -> {
                    narrative.append(section("Posterior"));
                    String pos = GraphicalModel.Utils.getInferenceStatement(parser, latexNarrative);
                    pos = htmlNarrative.rmLatexEquation(pos);
                    // replace equation to $$ ... $$
                    narrative.append("$$\n").append(pos).append("\n$$\n\n");
                    narrative.append("\n</div>\n");
//                    narrative.append("\n</details>\n");
                }
                case References -> {
                    String ref = htmlNarrative.referenceSection();
                    references.append(ref);
                }
            }
        }

        // special requirement
        // add link
        code.append("\nFor the details, please read the auto-generated ")
                .append("[narrative](#auto-generated)")
                .append(" from LPhyStudio.\n");
    }

    public void writeNarrative() throws IOException {
        String lphyStr = code.toString();
        writeToFile(lphyStr, "lphy.md");

        // validate
        String narStr = narrative.toString();
        validateTags(narStr, "div");
//        validateTags(narStr, "details");
        writeToFile(narStr, "narrative.md");

        String ref = references.toString();
        // convert MD, otherwise will have a gap when add new ref in MD
        ref = convertHtmlListToMardown(ref);
        // rm all \n, otherwise will have a gap
        ref = trimEndNewLine(ref);
        writeToFile(ref, "references.md");

    }

    private void writeToFile(String str, String fileName) throws IOException {
        File fi = new File(wd.toString(), fileName);
        PrintWriter writer = new PrintWriter(new FileWriter(fi));
        writer.println(str);
        writer.flush();
        writer.close();
        System.out.println("Write narrative to " + fi.getAbsolutePath());
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

    // markdown section
    private String section(String header) {
        return "## " + header + "\n\n";
    }

    // convert html list into markdown
    private String convertHtmlListToMardown(String ref) {
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

    // check if <details> and <div > are in correct position
    private void validateTags(String narStr, String tagStr) {
        int openTag = narStr.toLowerCase().indexOf("<" + tagStr.toLowerCase()); // it may have attrs
        int closeTag = narStr.toLowerCase().indexOf("</" + tagStr.toLowerCase() + ">");
        if (openTag >= closeTag)
            throw new IllegalStateException("Invalid position of <" + tagStr + "> !");
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
