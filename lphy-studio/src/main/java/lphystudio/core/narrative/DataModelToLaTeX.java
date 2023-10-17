package lphystudio.core.narrative;

import lphy.core.model.Symbols;
import lphy.core.parser.LPhyParserAction;
import lphy.core.parser.LPhyParserDictionary;
import lphystudio.core.codecolorizer.ColorizerStyles;
import lphystudio.core.codecolorizer.DataModelCodeColorizer;
import lphystudio.core.codecolorizer.TextElement;
import lphystudio.core.theme.ThemeColours;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;

import javax.swing.*;
import javax.swing.text.Style;
import java.util.ArrayList;
import java.util.List;

public class DataModelToLaTeX extends DataModelCodeColorizer {

    // CURRENT MODEL STATE

    static String randomVarColor = ThemeColours.getRandomVarIdLowerCase();
    static String constantColor = ThemeColours.getConstantIdLowerCase();
    static String keywordColor = ThemeColours.getDefaultIdLowerCase();
    static String argumentNameColor = ThemeColours.getArgumentNameIdLowerCase();
    static String functionColor = ThemeColours.getFunctionIdLowerCase();
    static String distributionColor = ThemeColours.getGenDistIdLowerCase();
    final String latexColTag = "\\textcolor{";

    List<String> elements = new ArrayList<>();

    LaTeXNarrative narrative = new LaTeXNarrative();

    public DataModelToLaTeX(LPhyParserDictionary parser, JTextPane pane) {
        super(parser, pane);
    }

    public String getLatexColour(String colorName) {
        return latexColTag + colorName + "}{";
    }

    public class DataModelASTVisitor extends DataModelCodeColorizer.DataModelASTVisitor {

        public DataModelASTVisitor() {
        }

        public void addTextElement(TextElement element) {

            StringBuilder builder = new StringBuilder();

            for (int i = 0; i < element.getSize(); i++) {
                String text = element.getText(i);
                Style style = element.getStyle(i);

                switch (style.getName()) {
                    case ColorizerStyles.function:
                        builder.append(getLatexColour(functionColor));
                        break;
                    case ColorizerStyles.distribution:
                        builder.append(getLatexColour(distributionColor));
                        break;
                    case ColorizerStyles.argumentName:
                        builder.append(getLatexColour(argumentNameColor));
                        break;
                    case ColorizerStyles.constant:
                        builder.append(getLatexColour(constantColor));
                        break;
                    case ColorizerStyles.randomVariable:
                        builder.append(getLatexColour(randomVarColor));
                }

                text = text.replace("{", "\\{");
                text = text.replace("}", "\\}");

                text = Symbols.getCanonical(text, "\\(\\", "\\)");

                builder.append(narrative.code(text));
                switch (style.getName()) {
                    case ColorizerStyles.function:
                    case ColorizerStyles.distribution:
                    case ColorizerStyles.argumentName:
                    case ColorizerStyles.constant:
                    case ColorizerStyles.randomVariable:
                        builder.append("}");
                }


            }
            elements.add(builder.toString());
        }
    }

    public String getLatex() {
        StringBuilder latex = new StringBuilder();
        latex.append("\\begin{alltt}\n");
        for (String element : elements) {
            latex.append(element);
        }
        latex.append("\\end{alltt}\n");
        return latex.toString();
    }

    public Object parse(String CASentence) {
//        System.out.println("Parsing " + CASentence + " to Latex");

        // Traverse parse tree
        AbstractParseTreeVisitor visitor = new DataModelASTVisitor();

        // containing either or both a data and model block;
        return LPhyParserAction.parse(CASentence, visitor);
    }
}
