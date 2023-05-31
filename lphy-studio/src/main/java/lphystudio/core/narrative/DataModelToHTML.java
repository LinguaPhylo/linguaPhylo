package lphystudio.core.narrative;

import lphy.core.parser.LPhyMetaParser;
import lphy.core.parser.LPhyParserAction;
import lphystudio.core.codecolorizer.ColorizerStyles;
import lphystudio.core.codecolorizer.DataModelCodeColorizer;
import lphystudio.core.codecolorizer.TextElement;
import lphystudio.core.theme.ThemeColours;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;

import javax.swing.*;
import javax.swing.text.Style;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DataModelToHTML extends DataModelCodeColorizer {
    // CURRENT MODEL STATE

    static Color randomVarColor = ThemeColours.getRandomVarColor();
    static Color constantColor = ThemeColours.getConstantColor();
    static Color keywordColor = ThemeColours.getDefaultColor();
    static Color argumentNameColor = ThemeColours.getArgumentNameColor().darker();
    static Color functionColor = ThemeColours.getFunctionColor();
    static Color distributionColor = ThemeColours.getGenDistColor();

    List<String> elements = new ArrayList<>();

    private final String fontSize;

    // allow to set font size to html <span style ...
    public DataModelToHTML(LPhyMetaParser parser, JTextPane pane, String fontSize) {
        super(parser, pane);
        this.fontSize = fontSize;
    }

    public class DataModelASTVisitor extends DataModelCodeColorizer.DataModelASTVisitor {

        public DataModelASTVisitor() {
        }

        public void addTextElement(TextElement element) {

            StringBuilder builder = new StringBuilder();

            for (int i = 0; i < element.getSize(); i++) {
                String text = element.getText(i);
                Style style = element.getStyle(i);
                Color c = ThemeColours.getDefaultColor();

                switch (style.getName()) {
                    case ColorizerStyles.function:
                        c = functionColor;
                        break;
                    case ColorizerStyles.distribution:
                        c = distributionColor;
                        break;
                    case ColorizerStyles.argumentName:
                        c = argumentNameColor;
                        break;
                    case ColorizerStyles.constant:
                        c = constantColor;
                        break;
                    case ColorizerStyles.randomVariable:
                        c = randomVarColor;
                }

                if (text.startsWith(indent)) {
                    builder.append(span("&nbsp;".repeat(indent.length()), ThemeColours.getDefaultColor()));
                    text = text.substring(indent.length());
                }
                builder.append(span(text, c));
                if (text.endsWith("\n")) builder.append("<br>\n");
            }
            elements.add(builder.toString());
        }
    }

    private String span(String text, Color color) {
        StringBuilder builder = new StringBuilder();
        builder.append("<span style=\"color: ");
        builder.append(hexCode(color));
        builder.append("; font-size: " + fontSize + "; font-family: monospace,monospace\">");
        builder.append(text);
        builder.append("</span>");
        return builder.toString();
    }

    private String hexCode(Color color) {
        return String.format("#%06x", color.getRGB() & 0x00FFFFFF);
    }

    public String getHTML() {
        StringBuilder html = new StringBuilder();
        for (String element : elements) {
            html.append(element);
        }
        return html.toString();
    }

    public Object parse(String CASentence) {
//        System.out.println("Parsing " + CASentence + " to HTML");

        // Traverse parse tree
        AbstractParseTreeVisitor visitor = new DataModelASTVisitor();

        // containing either or both a data and model block;
        return LPhyParserAction.parse(CASentence, visitor);
    }
}
