package lphy.parser;


import lphy.*;
import lphy.core.ErrorModel;
import lphy.core.PhyloBrownian;
import lphy.core.PhyloCTMC;
import lphy.core.distributions.*;
import lphy.core.distributions.Exp;
import lphy.core.functions.*;
import lphy.graphicalModel.*;
import lphy.parser.SimulatorParser.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class CodeColorizer extends AbstractBaseListener {

    // CURRENT MODEL STATE

    private JTextPane textPane;

    static Color randomVarColor = new Color(0, 196, 0);
    static Color constantColor = Color.magenta;
    static Color argumentNameColor = Color.gray;
    static Color functionColor = new Color(196,0,196);

    static int argumentNameSize = 10;

    Style randomVarStyle;
    Style constantStyle;
    Style argumentNameStyle;
    Style functionStyle;
    Style genDistStyle;
    Style punctuationStyle;
    Style funValueStyle;

    public CodeColorizer(Map<String, Value<?>> dictionary, JTextPane pane) {

        this.dictionary = dictionary;
        textPane = pane;

        functionStyle = textPane.addStyle("functionStyle", null);
        StyleConstants.setForeground(functionStyle, functionColor);

        genDistStyle = textPane.addStyle("genDistStyle", null);
        StyleConstants.setForeground(genDistStyle, Color.blue);
        StyleConstants.setBold(genDistStyle, true);

        randomVarStyle = textPane.addStyle("randomVarStyle", null);
        StyleConstants.setForeground(randomVarStyle, randomVarColor);

        funValueStyle = textPane.addStyle("funValueStyle", null);
        StyleConstants.setForeground(funValueStyle, Color.black);

        argumentNameStyle = textPane.addStyle("argumentNameStyle", null);
        StyleConstants.setForeground(argumentNameStyle, argumentNameColor);
        //StyleConstants.setFontSize(argumentNameStyle, argumentNameSize);

        constantStyle = textPane.addStyle("constantStyle", null);
        StyleConstants.setForeground(constantStyle, constantColor);

        punctuationStyle = textPane.addStyle("punctuationStyle", null);
        StyleConstants.setForeground(punctuationStyle, Color.black);
    }

    public class SimulatorASTVisitor extends SimulatorBaseVisitor<Object> {

        public SimulatorASTVisitor() {
            //initNameMap();

            bivarOperators = new HashSet<>();
            for (String s : new String[]{"+", "-", "*", "/", "**", "&&", "||", "<=", "<", ">=", ">", "%", ":", "^", "!=", "==", "&", "|", "<<", ">>", ">>>"}) {
                bivarOperators.add(s);
            }
            univarfunctions = new HashSet<>();
            for (String s : new String[]{"abs", "acos", "acosh", "asin", "asinh", "atan", "atanh", "cLogLog", "cbrt", "ceil", "cos", "cosh", "exp", "expm1", "floor", "log", "log10", "log1p", "logFact", "logGamma", "logit", "phi", "probit", "round", "signum", "sin", "sinh", "sqrt", "step", "tan", "tanh"}) {
                univarfunctions.add(s);
            }

        }

        private void addTextElement(TextElement element) {
            StyledDocument doc = textPane.getStyledDocument();

            for (int i = 0; i < element.text.size(); i++) {
                try {
                    doc.insertString(doc.getLength(), element.text.get(i), element.style.get(i));
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public Object visitConstant(ConstantContext ctx) {

            return new TextElement(ctx.getText(), constantStyle);
        }

        @Override
        public Object visitDeterm_relation(Determ_relationContext ctx) {

            TextElement element = new TextElement(ctx.getChild(0).getText(), funValueStyle);

            element.add(" = ", punctuationStyle);

            TextElement expr = (TextElement) visit(ctx.getChild(2));

            element.add(expr);
            element.add(";\n", punctuationStyle);

            addTextElement(element);
            return element;
        }

        @Override
        public Object visitStoch_relation(Stoch_relationContext ctx) {

            TextElement var = new TextElement(ctx.getChild(0).getText(), randomVarStyle);

            var.add(" " + ctx.getChild(1).getText() + " ", punctuationStyle);

            addTextElement(var);

            TextElement distributionElement = (TextElement) visit(ctx.getChild(2));

            addTextElement(distributionElement);

            return ctx.getText();
        }

        @Override
        public Object visitVar(VarContext ctx) {
            return new TextElement(ctx.getText(), randomVarStyle);
        }

        @Override
        public Object visitExpression(ExpressionContext ctx) {
            if (ctx.getChildCount() == 1) {
                String key = ctx.getChild(0).getText();
                if (dictionary.containsKey(key)) {
                    return new TextElement(key, randomVarStyle);
                }
            }

            if (ctx.getChildCount() >= 2) {
                String s = ctx.getChild(1).getText();
                if (bivarOperators.contains(s)) {
                    TextElement element = (TextElement) visit(ctx.getChild(0));

                    element.add(s, punctuationStyle);

                    element.add((TextElement) visit(ctx.getChild(ctx.getChildCount() - 1)));
                    return element;
                }
                s = ctx.getChild(0).getText();

                if (s.equals("!")) {
                    TextElement element = new TextElement(s, punctuationStyle);

                    element.add(ctx.getChild(0).getText(), punctuationStyle);
                    element.add((TextElement) visit(ctx.getChild(2)));
                    return element;
                } else if (s.equals("[")) {

                    TextElement e = new TextElement("[", punctuationStyle);
                    e.add((TextElement) visit(ctx.getChild(1)));
                    e.add("]", punctuationStyle);

                    return e;
                }
            }

            Object exp = super.visitExpression(ctx);
            if (exp instanceof TextElement) {
                System.out.println("exp was a text element: " + exp.toString());
                return exp;
            }

            if (exp instanceof String) {
                System.out.println("exp was a String: " + exp);
                return new TextElement((String) exp, constantStyle);
            }

            throw new RuntimeException(exp + " of type " + exp.getClass());

            //return new TextElement(ctx.getText(), Color.magenta);
        }

        @Override
        public Object visitNamed_expression(Named_expressionContext ctx) {
            String name = ctx.getChild(0).getText();
            TextElement element = new TextElement(name + "=", argumentNameStyle);
            element.add((TextElement) visit(ctx.getChild(2)));

            return element;
        }

        @Override
        public Object visitDistribution(DistributionContext ctx) {

            TextElement name = new TextElement(ctx.getChild(0).getText(), genDistStyle);

            TextElement arguments = (TextElement) visit(ctx.getChild(2));

            name.add("(", punctuationStyle);
            name.add(arguments);
            name.add(");\n", punctuationStyle);

            return name;
        }

        @Override
        public Object visitExpression_list(Expression_listContext ctx) {

            TextElement element = new TextElement();

            for (int i = 0; i < ctx.getChildCount(); i += 2) {
                element.add((TextElement) visit(ctx.getChild(i)));
                if (i < ctx.getChildCount() - 1) {
                    element.add(", ", punctuationStyle);
                }
            }
            return element;
        }

        @Override
        public Object visitUnnamed_expression_list(Unnamed_expression_listContext ctx) {
            TextElement element = new TextElement();

            for (int i = 0; i < ctx.getChildCount(); i += 2) {
                element.add((TextElement) visit(ctx.getChild(i)));
                if (i < ctx.getChildCount() - 1) {
                    element.add(", ", punctuationStyle);
                }
            }
            return element;        }

        @Override
        public Object visitMethodCall(MethodCallContext ctx) {

            String functionName = ctx.children.get(0).getText();

            TextElement e = new TextElement(functionName, functionStyle);

            e.add("(", punctuationStyle);

            ParseTree ctx2 = ctx.getChild(2);
            if (ctx2.getText().equals(")")) {
                // no arguments
            } else {
                e.add((TextElement)visit(ctx2));
            }
            e.add(")", punctuationStyle);


            return e;
        }
    }

    public Object parse(String CASentence) {
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
        SimulatorLexer lexer = new SimulatorLexer(CharStreams.fromString(CASentence));
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);

        // Get a list of matched tokens
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // Pass the tokens to the parser
        SimulatorParser parser = new SimulatorParser(tokens);
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
        SimulatorASTVisitor visitor = new SimulatorASTVisitor();

        return visitor.visit(parseTree);
    }

    private class TextElement {
        List<String> text = new ArrayList<>();
        List<Style> style = new ArrayList<>();

        public TextElement() {
        }

        public TextElement(String text, Style style) {
            add(text, style);
        }

        void add(String text, Style style) {
            this.text.add(text);
            this.style.add(style);
        }

        void add(TextElement e) {
            for (int i = 0; i < e.text.size(); i++) {
                add(e.text.get(i), e.style.get(i));
            }
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            for (String t : text) {
                builder.append(t);
            }
            return builder.toString();
        }
    }
}
