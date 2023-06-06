package lphystudio.app;

import lphy.base.system.UserDir;
import lphy.core.exception.LoggerUtils;
import lphy.core.parser.GraphicalLPhyParser;
import lphy.core.parser.graphicalmodel.GraphicalModelUtils;
import lphystudio.app.graphicalmodelcomponent.GraphicalModelComponent;
import lphystudio.app.graphicalmodelpanel.GraphicalModelPanel;
import lphystudio.core.narrative.HTMLNarrative;
import lphystudio.core.narrative.LaTeXNarrative;
import lphystudio.core.narrative.Section;

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

    public NarrativeCreator(String lphyFileName) throws IOException {
        htmlNarrative = new HTMLNarrative();
        latexNarrative = new LaTeXNarrative();
        Path wd = UserDir.getUserDir(); // working dir
        Path imgFile = Paths.get(wd.toString(), "GraphicalModel.png");
        // assume file under working dir
        Path lphyPath = Paths.get(wd.toString(), lphyFileName);

        if (!lphyPath.toFile().exists())
            throw new IOException("Cannot find lphy script at " + lphyPath);

        try {
            GraphicalLPhyParser parser = Utils.createParser();
            GraphicalModelPanel panel = new GraphicalModelPanel(parser, null);
            panel.getComponent().setShowConstantNodes(false);

            // parse and paint
            Utils.readFile(lphyPath.toFile(), panel);

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
//        Path path = UserDir.getUserPath(lphyFileName);
//        // set user.dir to the folder containing example file,
//        // so that the relative path given in readNexus always refers to it
//        UserDir.setUserDir(path.getParent().toString());
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
                    String dataSec = GraphicalModelUtils.getNarrative(parser, htmlNarrative, true, false);
//                    narrative.append("\n<details>\n");
//                    narrative.append("<summary>Click to expand the auto-generated narrative from LPhyStudio ...</summary>\n");
                    narrative.append("\n" + DIV_BOX_BACKGR_COLOUR + "\n");
                    narrative.append(dataSec);
                    narrative.append("\n");
                }
                case Model -> {
                    String modelSec = GraphicalModelUtils.getNarrative(parser, htmlNarrative, false, true);
                    narrative.append(modelSec);
                    narrative.append("\n");
                }
                case Posterior -> {
                    narrative.append(htmlNarrative.section("Posterior"));
                    String pos = GraphicalModelUtils.getInferenceStatement(parser, latexNarrative);
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
        String lphyStr = replaceEscapeCharInLPhyScript(code.toString());
        Path path = Paths.get(wd.toString(), "lphy.md");
        writeToFile(lphyStr, path);

        // it seems to print as it is if escape chars are inside <div>
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

    // case by case to replace escape char in lphy script.
    // if * is inside html which is inside a markdown (e.g. jekyll), then use \*,
    // also \| to \\|, " to &quot;, and so on
    private String replaceEscapeCharInLPhyScript(String old) {
        // \| to \\|
        String newStr = old.replaceAll("\\\\\\|", "\\\\\\\\|");
        // \. to \\.
        newStr = newStr.replaceAll("\\\\\\.", "\\\\\\\\.");
        // " (must be lphy code, not html) to &quot;
        newStr = newStr.replaceAll(">\"", ">&quot;");
        newStr = newStr.replaceAll("\"<", "&quot;<");
        // ' to &apos;
        newStr = newStr.replaceAll(">'", ">&apos;");
        newStr = newStr.replaceAll("'<", "&apos;<");
        // * to \*
        return newStr.replaceAll("\\*", "\\\\*");
    }
    // if * is inside a jekyll markdown but not inside html tags, use &ast;
//    private String replaceAsteriskInMD(String old) {
//        return old.replaceAll("\\*", "&ast;");
//    }

    private void writeToFile(String str, Path path) throws IOException {
        PrintWriter writer = new PrintWriter(new FileWriter(path.toString()));
        // references do not new line
        writer.print(str);
        writer.flush();
        writer.close();
        System.out.println("Write \"" + (str.length() > 20 ? str.substring(0, 20) : str) +
                "\" ...\nto " + path.toAbsolutePath());
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

    // input: -Duser.dir=/lphy/path/tutorials h3n2.lphy
    public static void main(String[] args) {

        if (args.length != 1)
            throw new IllegalArgumentException("Expecting LPhy file name !");

        // always the last arg, such as h5n1.lphy, assuming the file is under user.dir
        String lphyFileName = args[args.length - 1];

        try {
            NarrativeCreator narrativeCreator = new NarrativeCreator(lphyFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.exit(0);
    }

}
