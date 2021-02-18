package lphy.parser.codecolorizer;

import lphy.core.LPhyParser;
import lphy.parser.DataModelLexer;
import lphy.parser.DataModelParser;
import lphy.parser.SimulatorParsingException;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

import javax.swing.*;
import javax.swing.text.Style;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DataModelToHTML extends DataModelCodeColorizer {
    // CURRENT MODEL STATE

    static Color randomVarColor = Color.green;
    static Color constantColor = Color.magenta;
    static Color keywordColor = Color.black;
    static Color argumentNameColor = Color.gray;
    static Color functionColor = Color.magenta.darker();
    static Color distributionColor = Color.blue;

    List<String> elements = new ArrayList<>();

    private final String fontSize;

    // allow to set font size to html <span style ...
    public DataModelToHTML(LPhyParser parser, JTextPane pane, String fontSize) {
        super(parser, pane);
        this.fontSize = fontSize;
    }

    public class DataModelASTVisitor extends DataModelCodeColorizer.DataModelASTVisitor {

        public DataModelASTVisitor() {
        }

        void addTextElement(TextElement element) {

            StringBuilder builder = new StringBuilder();

            for (int i = 0; i < element.text.size(); i++) {
                String text = element.text.get(i);
                Style style = element.style.get(i);
                Color c = Color.black;

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
                    builder.append(span("&nbsp;".repeat(indent.length()), Color.black));
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

        System.out.println("Parsing " + CASentence);

        // Custom parse/lexer error listener
        BaseErrorListener errorListener = new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer,
                                    Object offendingSymbol, int line, int charPositionInLine,
                                    String msg, RecognitionException e) {
                e.printStackTrace();
                if (e instanceof NoViableAltException) {
                    NoViableAltException nvae = (NoViableAltException) e;
                    System.out.println(nvae.getLocalizedMessage());
//              msg = "X no viable alt; token="+nvae.token+
//                 " (decision="+nvae.decisionNumber+
//                 " state "+nvae.stateNumber+")"+
//                 " decision=<<"+nvae.grammarDecisionDescription+">>";
                } else {
                }
                throw new SimulatorParsingException(msg, charPositionInLine, line);
            }

//            @Override
//            public void syntaxError(Recognizer<?, ?> recognizer,
//                                    Object offendingSymbol,
//                                    int line, int charPositionInLine,
//                                    String msg, RecognitionException e) {
//                throw new SimulatorParsingException(msg, charPositionInLine, line);
//            }
        };

        // Get our lexer
        DataModelLexer lexer = new DataModelLexer(CharStreams.fromString(CASentence));
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);

        // Get a list of matched tokens
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // Pass the tokens to the parser
        DataModelParser parser = new DataModelParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        ParseTree parseTree = parser.input();
//	    // Specify our entry point
//	    CasentenceContext CASentenceContext = parser.casentence();
//
//	    // Walk it and attach our listener
//	    ParseTreeWalker walker = new ParseTreeWalker();
//	    AntlrCompactAnalysisListener listener = new AntlrCompactAnalysisListener();
//	    walker.walk(listener, CASentenceContext);


        // Traverse parse tree, constructing BEAST tree along the way
        DataModelASTVisitor visitor = new DataModelASTVisitor();

        return visitor.visit(parseTree);
    }
}
