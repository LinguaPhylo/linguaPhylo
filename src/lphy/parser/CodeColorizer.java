package lphy.parser;


import lphy.*;
import lphy.core.ErrorModel;
import lphy.core.PhyloBrownian;
import lphy.core.PhyloCTMC;
import lphy.core.distributions.*;
import lphy.core.distributions.Exp;
import lphy.core.functions.*;
import lphy.graphicalModel.*;
import lphy.graphicalModel.types.*;
import lphy.parser.SimulatorParser.*;
import lphy.utils.LoggerUtils;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.format.TextStyle;
import java.util.*;
import java.util.List;
import java.util.logging.Level;

public class CodeColorizer extends SimulatorBaseListener {

    // CURRENT MODEL STATE

    Map<String, Value<?>> dictionary;

    private JTextPane textPane;

    // PARSER STATE
    static Map<String, Set<Class<?>>> genDistDictionary;
    static Map<String, Set<Class<?>>> functionDictionary;
    static Set<String> bivarOperators, univarfunctions;

    static Color randomVarColor = new Color(0,196,0);

    static {
        genDistDictionary = new TreeMap<>();
        functionDictionary = new TreeMap<>();

        Class<?>[] genClasses = {BirthDeathTree.class, BirthDeathTreeDT.class, Normal.class, LogNormal.class, LogNormalMulti.class, Exp.class, Coalescent.class,
                PhyloCTMC.class, PhyloBrownian.class, Dirichlet.class, Gamma.class, DiscretizedGamma.class,
                ErrorModel.class, Yule.class, Beta.class, SerialCoalescent.class, StructuredCoalescent.class};

        for (Class<?> genClass : genClasses) {
            String name = Generator.getGeneratorName(genClass);

            Set<Class<?>> genDistSet = genDistDictionary.computeIfAbsent(name, k -> new HashSet<>());
            genDistSet.add(genClass);
        }

        Class<?>[] functionClasses = {lphy.core.functions.Exp.class, JukesCantor.class, K80.class, HKY.class, GTR.class,
                Newick.class, BinaryRateMatrix.class, NodeCount.class, MigrationMatrix.class, MigrationCount.class};

        for (Class<?> functionClass : functionClasses) {

            String name = Generator.getGeneratorName(functionClass);

            Set<Class<?>> funcSet = functionDictionary.computeIfAbsent(name, k -> new HashSet<>());
            funcSet.add(functionClass);
        }
        System.out.println(Arrays.toString(genDistDictionary.keySet().toArray()));
        System.out.println(Arrays.toString(functionDictionary.keySet().toArray()));
    }

    public CodeColorizer(Map<String, Value<?>> dictionary, JTextPane pane) {

        this.dictionary = dictionary;
        textPane = pane;
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
                Style style = textPane.addStyle("Color Style", null);
                StyleConstants.setForeground(style, element.color.get(i));
                try {
                    doc.insertString(doc.getLength(), element.text.get(i), style);
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public Object visitConstant(ConstantContext ctx) {

            return new TextElement(ctx.getText(),Color.cyan);
        }

        @Override
        public Object visitDeterm_relation(Determ_relationContext ctx) {

            TextElement element = new TextElement(ctx.getChild(0).getText(),Color.black);

            element.add(" = ", Color.black);

            TextElement expr = (TextElement)visit(ctx.getChild(2));

            element.add(expr);
            element.add("\n", Color.black);

            addTextElement(element);
            return element;
        }

        @Override
        public Object visitStoch_relation(Stoch_relationContext ctx) {

            TextElement var = new TextElement(ctx.getChild(0).getText(), randomVarColor);

            var.add(" " + ctx.getChild(1).getText() + " ", Color.black);

            addTextElement(var);

            TextElement distributionElement = (TextElement) visit(ctx.getChild(2));

            addTextElement(distributionElement);

            return ctx.getText();
        }

        @Override
        public Object visitVar(VarContext ctx) {
            return new TextElement(ctx.getText(), randomVarColor);
        }

        @Override
        public Object visitExpression(ExpressionContext ctx) {
            if (ctx.getChildCount() == 1) {
                String key = ctx.getChild(0).getText();
                if (dictionary.containsKey(key)) {
                    return new TextElement(key, randomVarColor);
                }
            }

            return new TextElement(ctx.getText(), Color.magenta);
        }

        @Override
        public Object visitNamed_expression(Named_expressionContext ctx) {
            String name = ctx.getChild(0).getText();
            TextElement element = new TextElement(name + "=", Color.darkGray);
            element.add((TextElement)visit(ctx.getChild(2)));

            return element;
        }

        @Override
        public Object visitDistribution(DistributionContext ctx) {

            TextElement name = new TextElement(ctx.getChild(0).getText(), Color.blue);

            TextElement arguments = (TextElement)visit(ctx.getChild(2));

            name.add("(", Color.black);
            name.add(arguments);
            name.add(")\n", Color.black);

            return name;
        }

        @Override
        public Object visitExpression_list(Expression_listContext ctx) {

            TextElement element = new TextElement();

            for (int i = 0; i < ctx.getChildCount(); i += 2) {
                element.add((TextElement) visit(ctx.getChild(i)));
                if (i < ctx.getChildCount()-1) {
                    element.add(", ", Color.black);
                }
            }
            return element;
        }

        @Override
        public Object visitUnnamed_expression_list(Unnamed_expression_listContext ctx) {
            return new TextElement(ctx.getText(), Color.black);
        }

        @Override
        public Object visitMethodCall(MethodCallContext ctx) {

            addTextElement(new TextElement(ctx.getText(), Color.darkGray));
            return ctx.getText();
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
        List<Color> color = new ArrayList<>();

        public TextElement() {}

        public TextElement(String text, Color color) {
            add(text, color);
        }

        void add(String text, Color color) {
            this.text.add(text);
            this.color.add(color);
        }

        void add(TextElement e) {
            for (int i = 0; i < e.text.size(); i++) {
                add(e.text.get(i), e.color.get(i));
            }
        }

        void insert(int pos, String text, Color color) {
            this.text.set(pos,text);
            this.color.set(pos, color);
        }
    }
}
