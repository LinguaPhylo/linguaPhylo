package lphy.parser.codecolorizer;

import lphy.app.Symbols;
import lphy.core.LPhyParser;
import lphy.core.narrative.LaTeXNarrative;
import lphy.parser.DataModelLexer;
import lphy.parser.DataModelParser;
import lphy.parser.SimulatorParsingException;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

import javax.swing.*;
import javax.swing.text.Style;
import java.util.ArrayList;
import java.util.List;

public class DataModelToLaTeX extends DataModelCodeColorizer {

    // CURRENT MODEL STATE

    static String randomVarColor = "green";
    static String constantColor = "magenta";
    static String keywordColor = "black";
    static String argumentNameColor = "gray";
    static String functionColor = "magenta!80!black";
    static String distributionColor = "blue";

    List<String> elements = new ArrayList<>();

    LaTeXNarrative narrative = new LaTeXNarrative();

    public DataModelToLaTeX(LPhyParser parser, JTextPane pane) {
        super(parser, pane);
    }

    public class DataModelASTVisitor extends DataModelCodeColorizer.DataModelASTVisitor {

        public DataModelASTVisitor() {
        }

        void addTextElement(TextElement element) {

            StringBuilder builder = new StringBuilder();

            for (int i = 0; i < element.text.size(); i++) {
                String text = element.text.get(i);
                Style style = element.style.get(i);

                switch (style.getName()) {
                    case ColorizerStyles.function:
                        builder.append("\\textcolor{");
                        builder.append(functionColor);
                        builder.append("}{");
                        break;
                    case ColorizerStyles.distribution:
                        builder.append("\\textcolor{");
                        builder.append(distributionColor);
                        builder.append("}{");
                        break;
                    case ColorizerStyles.argumentName:
                        builder.append("\\textcolor{");
                        builder.append(argumentNameColor);
                        builder.append("}{");
                        break;
                    case ColorizerStyles.constant:
                        builder.append("\\textcolor{");
                        builder.append(constantColor);
                        builder.append("}{");
                        break;
                    case ColorizerStyles.randomVariable:
                        builder.append("\\textcolor{");
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
