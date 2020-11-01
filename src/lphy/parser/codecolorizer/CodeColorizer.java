package lphy.parser.codecolorizer;


import lphy.core.LPhyParser;
import lphy.graphicalModel.RandomVariable;
import lphy.graphicalModel.Value;
import lphy.parser.*;
import lphy.parser.SimulatorParser.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

import javax.swing.*;
import javax.swing.text.*;
import java.util.*;
import java.util.List;

public class CodeColorizer extends SimulatorBaseListener {

    // CURRENT MODEL STATE

    private JTextPane textPane;

    LPhyParser parser;
    LPhyParser.Context context;

    Style punctuationStyle;
    Style randomStyle;
    Style constantStyle;
    Style genDistStyle;
    Style argumentNameStyle;
    Style functionStyle;
    Style valueStyle;

    public CodeColorizer(LPhyParser parser, LPhyParser.Context context, JTextPane pane) {

        this.parser = parser;
        this.context = context;
        textPane = pane;

        ColorizerStyles.addStyles(pane);
        punctuationStyle = pane.getStyle("punctuationStyle");
        constantStyle = pane.getStyle("constantStyle");
        genDistStyle = pane.getStyle("genDistStyle");
        argumentNameStyle = pane.getStyle("argumentNameStyle");
        functionStyle = pane.getStyle("functionStyle");
        randomStyle = pane.getStyle("randomVarStyle");
        valueStyle = pane.getStyle("valueStyle");
    }

    public class SimulatorASTVisitor extends SimulatorBaseVisitor<Object> {

        public SimulatorASTVisitor() { }

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
            return new TextElement(ctx.getText(), textPane.getStyle("constantStyle"));
        }

        public Object visitMapFunction(MapFunctionContext ctx) {
            TextElement element = new TextElement("{", textPane.getStyle("punctuationStyle"));
            element.add((TextElement)visit(ctx.getChild(1)));
            element.add(new TextElement("}", textPane.getStyle("punctuationStyle")));
            return element;
        }

        @Override
        public Object visitDeterm_relation(Determ_relationContext ctx) {

            TextElement element = new TextElement(ctx.getChild(0).getText(), textPane.getStyle("valueStyle"));

            element.add(" = ", textPane.getStyle("punctuationStyle"));

            TextElement expr = (TextElement) visit(ctx.getChild(2));

            element.add(expr);
            element.add(";\n", textPane.getStyle("punctuationStyle"));

            addTextElement(element);
            return element;
        }

        @Override
        public Object visitStoch_relation(Stoch_relationContext ctx) {

            TextElement var = new TextElement(ctx.getChild(0).getText(), textPane.getStyle("randomVarStyle"));

            var.add(" " + ctx.getChild(1).getText() + " ", textPane.getStyle("punctuationStyle"));

            addTextElement(var);

            TextElement distributionElement = (TextElement) visit(ctx.getChild(2));

            addTextElement(distributionElement);

            return ctx.getText();
        }

        @Override
        public Object visitVar(VarContext ctx) {
            return new TextElement(ctx.getText(), textPane.getStyle("randomVarStyle"));
        }

        @Override
        public Object visitExpression(ExpressionContext ctx) {
            if (ctx.getChildCount() == 1) {

                ParseTree childContext = ctx.getChild(0);
                String key = childContext.getText();
                if (parser.hasValue(key, context)) {
                    Value value = parser.getValue(key, context);
                    return new TextElement(key, value instanceof RandomVariable ? randomStyle : valueStyle);
                }

                // else let subordinate method handle it.
                return visit(childContext);
            }
            if (ctx.getChildCount() >= 2) {
                String s = ctx.getChild(1).getText();
                if (ParserUtils.bivarOperators.contains(s)) {
                    TextElement element = (TextElement) visit(ctx.getChild(0));

                    element.add(s, punctuationStyle);

                    element.add((TextElement) visit(ctx.getChild(ctx.getChildCount() - 1)));
                    return element;
                } else if (s.equals("[")) {

                    TextElement e = (TextElement) visit(ctx.getChild(0));
                    e.add(new TextElement("[", punctuationStyle));
                    e.add((TextElement) visit(ctx.getChild(2)));
                    e.add("]", punctuationStyle);

                    return e;
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
                return exp;
            }

            if (exp instanceof String) {
                System.out.println("exp was a String: " + exp);
                return new TextElement((String) exp, constantStyle);
            }

            if (exp == null) {
                return new TextElement("null", constantStyle);
                //throw new RuntimeException("exp is null for expression context: " + ctx.getText());
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

        @Override
        public Object visitObjectMethodCall(ObjectMethodCallContext ctx) {

            String objectName = ctx.children.get(0).getText();
            String methodName = ctx.children.get(2).getText();

            TextElement e = getIDElement(objectName);

            e.add(new TextElement(".", punctuationStyle));
            e.add(new TextElement(methodName, functionStyle));

            e.add("(", punctuationStyle);

            ParseTree ctx2 = ctx.getChild(4);
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

    private TextElement getIDElement(String key) {
        if (parser.hasValue(key, context)) {
            Value value = parser.getValue(key, context);
            return new TextElement(key, value instanceof RandomVariable ? randomStyle : valueStyle);
        } 
        return new TextElement(key, constantStyle);
    }
}
