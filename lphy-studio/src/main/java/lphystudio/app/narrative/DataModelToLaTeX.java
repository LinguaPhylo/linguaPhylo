package lphystudio.app.narrative;

import lphy.core.LPhyParser;
import lphy.parser.LPhyParserAction;
import lphy.util.Symbols;
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

    static String randomVarColor = Integer.toHexString(ThemeColours.getRandomVarColor().getRGB());
    static String constantColor = Integer.toHexString(ThemeColours.getConstantColor().getRGB());
    static String keywordColor = Integer.toHexString(ThemeColours.getMainColor().getRGB());
    static String argumentNameColor = Integer.toHexString(ThemeColours.getArgumentNameColor().darker().getRGB());
    static String functionColor = Integer.toHexString(ThemeColours.getFunctionColor().getRGB());
    static String distributionColor = Integer.toHexString(ThemeColours.getGenDistColor().getRGB());

    List<String> elements = new ArrayList<>();

    LaTeXNarrative narrative = new LaTeXNarrative();

    public DataModelToLaTeX(LPhyParser parser, JTextPane pane) {
        super(parser, pane);
    }

    public class DataModelASTVisitor extends DataModelCodeColorizer.DataModelASTVisitor {

        public DataModelASTVisitor() {
        }

        public void addTextElement(TextElement element) {

            StringBuilder builder = new StringBuilder();
            final String latexColTag = "\\color[HTML]{";

            for (int i = 0; i < element.getSize(); i++) {
                String text = element.getText(i);
                Style style = element.getStyle(i);

                switch (style.getName()) {
                    case ColorizerStyles.function:
                        builder.append(latexColTag);
                        builder.append(functionColor);
                        builder.append("}{");
                        break;
                    case ColorizerStyles.distribution:
                        builder.append(latexColTag);
                        builder.append(distributionColor);
                        builder.append("}{");
                        break;
                    case ColorizerStyles.argumentName:
                        builder.append(latexColTag);
                        builder.append(argumentNameColor);
                        builder.append("}{");
                        break;
                    case ColorizerStyles.constant:
                        builder.append(latexColTag);
                        builder.append(constantColor);
                        builder.append("}{");
                        break;
                    case ColorizerStyles.randomVariable:
                        builder.append(latexColTag);
                        builder.append(randomVarColor);
                        builder.append("}{");
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
        return LPhyParserAction.parse(CASentence, visitor, true);
    }
}
