package lphystudio.core.codecolorizer;

import lphy.core.codebuilder.CanonicalCodeBuilder;
import lphy.core.parser.LPhyParserDictionary;
import lphystudio.app.graphicalmodelpanel.GraphicalModelParserDictionary;
import lphystudio.app.graphicalmodelpanel.StudioConsoleInterpreter;
import lphystudio.core.narrative.DataModelToHTML;
import lphystudio.core.narrative.DataModelToLaTeX;
import lphystudio.core.theme.ThemeColours;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Also include DataModelToHTML and DataModelToLaTeX tests
 * @author Walter Xie
 */
public class LineCodeColorizerTest {

    final String taxa = "taxa = taxa(names=1:10);";

    final String coal = "Θ ~ LogNormal(meanlog=3.0, sdlog=1.0); ψ ~ Coalescent(theta=Θ, taxa=taxa);";

    GraphicalModelParserDictionary parserDictionary;
    String cmd;

    @BeforeEach
    public void setUp() {
        parserDictionary = new GraphicalModelParserDictionary();
        StudioConsoleInterpreter dataInterpreter = new StudioConsoleInterpreter(parserDictionary, LPhyParserDictionary.Context.data, null);
        StudioConsoleInterpreter modelInterpreter = new StudioConsoleInterpreter(parserDictionary, LPhyParserDictionary.Context.model, null);

        dataInterpreter.interpretInput(taxa, LPhyParserDictionary.Context.data);
        modelInterpreter.interpretInput(coal, LPhyParserDictionary.Context.model);

        CanonicalCodeBuilder codeBuilder = new CanonicalCodeBuilder();
        cmd = codeBuilder.getCode(parserDictionary);
    }

    @Test
    public void parse() {
        parseDataModelCodeColorizer();

        parseDataModelToHTML();

        parseDataModelToLaTeX();
    }

    private void parseDataModelCodeColorizer() {
        String paneText = null;
        try {
            JTextPane textPane = new JTextPane();
            DataModelCodeColorizer codeColorizer = new DataModelCodeColorizer(parserDictionary, textPane);
            codeColorizer.parse(cmd);

            paneText = textPane.getDocument().getText(0, textPane.getDocument().getLength());
        } catch (Exception e) {
            fail("CMD " + cmd + " failed to parse in code colorizer, Exception :\n" + e.getMessage());
        }
        assertNotNull(paneText);
        System.out.println(paneText);
        // brief test the substr
        assertTrue(paneText.contains("data {") && paneText.contains("model {") &&
                paneText.contains(taxa) && paneText.contains("LogNormal") && paneText.contains("Coalescent"));
    }

    private void parseDataModelToHTML() {
        String html = null;
        try {
            DataModelToHTML dataModelToHTML = new DataModelToHTML(parserDictionary, new JTextPane(), "11pt");
            // somehow return null
            dataModelToHTML.parse(cmd);

            html = dataModelToHTML.getHTML();
        } catch (Exception e) {
            fail("CMD " + cmd + " failed to parse to HTML, Exception :\n" + e.getMessage());
        }
        assertNotNull(html);
        System.out.println(html);
        // brief test the substr
        assertTrue(html.contains("<span style=\"color") && html.contains("data {") && html.contains("model {") &&
                        html.contains("names=") && html.contains("LogNormal") && html.contains("Coalescent"));
    }

    private void parseDataModelToLaTeX() {
        String latex = null;
        try {
            DataModelToLaTeX dataModelToLaTeX = new DataModelToLaTeX(parserDictionary, new JTextPane());
            // somehow return null
            dataModelToLaTeX.parse(cmd);

            latex = dataModelToLaTeX.getLatex();
        } catch (Exception e) {
            fail("CMD " + cmd + " failed to parse to Latex, Exception :\n" + e.getMessage());
        }
        assertNotNull(latex);
        System.out.println(latex);
        // brief test the substr
        assertTrue(latex.contains("\\begin{alltt}") && latex.contains("\\end{alltt}") &&
                latex.contains("data \\{") && latex.contains("model \\{") &&
                latex.contains("\\textcolor{" + ThemeColours.getGenDistIdLowerCase() + "}{LogNormal}") &&
                latex.contains("\\textcolor{" + ThemeColours.getGenDistIdLowerCase() + "}{Coalescent}"));

    }

}