package lphystudio.app;

import lphy.core.GraphicalLPhyParser;
import lphy.core.narrative.Section;
import lphy.graphicalModel.GraphicalModel;
import lphy.util.IOUtils;
import lphy.util.LoggerUtils;
import lphystudio.app.graphicalmodelcomponent.GraphicalModelComponent;
import lphystudio.app.graphicalmodelpanel.GraphicalModelPanel;
import lphystudio.app.narrative.HTMLNarrative;
import lphystudio.app.narrative.LaTeXNarrative;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;

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
    HTMLNarrative htmlNarrative;
    LaTeXNarrative latexNarrative;

    StringBuilder code = new StringBuilder();
    StringBuilder narrative = new StringBuilder();
    StringBuilder references = new StringBuilder();

//    static Preferences preferences = Preferences.userNodeForPackage(NarrativeCreator.class);

    public NarrativeCreator(String lphyFileName) {
        htmlNarrative = new HTMLNarrative();
        latexNarrative = new LaTeXNarrative();
        Path wd = IOUtils.getUserDir(); // working dir
        Path imgFile = Paths.get(wd.toString(), "GraphicalModel.png");

        try {
            GraphicalLPhyParser parser = Utils.createParser();
            GraphicalModelPanel panel = new GraphicalModelPanel(parser);
            panel.getComponent().setShowConstantNodes(false);
            File lphyFile = new File(lphyFileName);
            // parse and paint
            Utils.readFile(lphyFile, panel);

            createNarrativeExclImg(panel.getParser(), imgFile);
            LoggerUtils.log.warning("Image " + imgFile.getFileName() + " needs to be created separately !");

            // TODO The quality of PNG is too low LinguaPhylo/linguaPhylo#130
            createImage(imgFile.toFile(), panel.getComponent());
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            writeNarrative(wd);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


//    private GraphicalLPhyParser getParser(String lphyFileName) throws IOException {
//        if (!lphyFileName.endsWith(".lphy"))
//            throw new IllegalArgumentException("Invalid LPhy file name " + lphyFileName + " !");
//
//        Path path = IOUtils.getUserPath(lphyFileName);
//        // set user.dir to the folder containing example file,
//        // so that the relative path given in readNexus always refers to it
//        IOUtils.setUserDir(path.getParent().toString());
//
//        BufferedReader reader = new BufferedReader(new FileReader(path.toString()));
//        LPhyParser parser = new REPL();
//        parser.source(reader);
//
//        // A wrapper for any implementation of LPhyParser that will be used in the Studio
//        return new GraphicalLPhyParser(parser);
//    }

    private void createImage(File imgFile, GraphicalModelComponent component) throws IOException {
        Utils.exportToPNG(imgFile, component);
        LoggerUtils.log.info("Save " + imgFile.getAbsolutePath());
    }

    // exclude creating image
    private void createNarrativeExclImg(GraphicalLPhyParser parser, final Path imgFile) {
        // assume Data, Model, Posterior stay together with this order,
        // so wrap them with <div id="auto-generated"> for a box with diff background colour.
        for (Section section : Section.values()) {

            switch (section) {
                case Code -> {
                    code.append(section("Code"));
                    code.append(htmlNarrative.codeBlock(parser, 11));
                    code.append("\n");
                }
                case GraphicalModel -> {
                    code.append(section("Graphical Model"));
                    // create html to load image
                    code.append("\n<figure class=\"image\">\n");
                    code.append("  <a href=\"").append(imgFile.getFileName())
                            .append("\" target=\"_blank\">\n");
                    code.append("    <img src=\"").append(imgFile.getFileName())
                            .append("\" alt=\"").append(imgFile.getFileName())
                            .append("\">\n");
                    code.append("  </a>\n");
                    // replace fignum using Liquid in Jekyll MD
                    code.append("  <figcaption>{{ include.fignum }}: The graphical model</figcaption>\n");
                    code.append("</figure>\n\n");
                    code.append("\n");
                    // create image later
                }
                // move Data, Model, Posterior to the bottom of webpage
                case Data -> {
                    String dataSec = GraphicalModel.Utils.getNarrative(parser, htmlNarrative, true, false);
//                    narrative.append("\n<details>\n");
//                    narrative.append("<summary>Click to expand the auto-generated narrative from LPhyStudio ...</summary>\n");
                    narrative.append("\n" + DIV_BOX_BACKGR_COLOUR + "\n");
                    narrative.append(dataSec);
                    narrative.append("\n");
                }
                case Model -> {
                    String modelSec = GraphicalModel.Utils.getNarrative(parser, htmlNarrative, false, true);
                    narrative.append(modelSec);
                    narrative.append("\n");
                }
                case Posterior -> {
                    narrative.append(htmlNarrative.section("Posterior"));
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

    private void writeNarrative(Path wd) throws IOException {
        String lphyStr = code.toString();
        Path path = Paths.get(wd.toString(), "lphy.md");
        writeToFile(lphyStr, path);

        // validate
        String narStr = narrative.toString();
        validateTags(narStr, "div");
//        validateTags(narStr, "details");
        path = Paths.get(wd.toString(), "narrative.md");
        writeToFile(narStr, path);

        String ref = references.toString();
        // convert MD, otherwise will have a gap when add new ref in MD
        ref = convertHtmlListToMardown(ref);
        // rm all \n, otherwise will have a gap
        ref = trimEndNewLine(ref);
        path = Paths.get(wd.toString(), "references.md");
        writeToFile(ref, path);

    }

    private void writeToFile(String str, Path path) throws IOException {
        PrintWriter writer = new PrintWriter(new FileWriter(path.toString()));
        // references do not new line
        writer.print(str);
        writer.flush();
        writer.close();
        System.out.println("Write \"" + str.substring(0, 5) +
                "\" ... to " + path.toAbsolutePath());
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

        if (args.length != 1)
            throw new IllegalArgumentException("Expecting LPhy file name !");

        // always the last arg, examples/h5n1.lphy
        String lphyFileName = args[args.length - 1];

        NarrativeCreator narrativeCreator = new NarrativeCreator(lphyFileName);

        System.exit(0);
    }

}
