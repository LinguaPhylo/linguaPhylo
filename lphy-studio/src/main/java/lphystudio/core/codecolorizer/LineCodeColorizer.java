package lphystudio.core.codecolorizer;


import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.parser.LPhyParserAction;
import lphy.core.parser.LPhyParserDictionary;
import lphy.core.parser.antlr.LPhyBaseListener;
import lphy.core.parser.antlr.LPhyBaseVisitor;
import lphy.core.parser.antlr.LPhyParser.*;
import lphy.core.spi.LoaderManager;
import org.antlr.v4.runtime.tree.ParseTree;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

/**
 * Implement the listener for the parse tree colouring LPhy code.
 */
public class LineCodeColorizer extends LPhyBaseListener implements CodeColorizer, LPhyParserAction {

    // CURRENT MODEL STATE

    private JTextPane textPane;

    LPhyParserDictionary parser;
    LPhyParserDictionary.Context context;

    Style punctuationStyle;
    Style randomStyle;
    Style constantStyle;
    Style genDistStyle;
    Style argumentNameStyle;
    Style functionStyle;
    Style valueStyle;
    Style clampedStyle;

    public LineCodeColorizer(LPhyParserDictionary parser, LPhyParserDictionary.Context context, JTextPane pane) {

        this.parser = parser;
        this.context = context;
        textPane = pane;

        ColorizerStyles.addStyles(pane);
        punctuationStyle = pane.getStyle("punctuationStyle");
        constantStyle = pane.getStyle("constantStyle");
        genDistStyle = pane.getStyle("distributionStyle");
        argumentNameStyle = pane.getStyle("argumentNameStyle");
        functionStyle = pane.getStyle("functionStyle");
        randomStyle = pane.getStyle("randomVarStyle");
        valueStyle = pane.getStyle("valueStyle");
        clampedStyle = pane.getStyle(ColorizerStyles.clampedVariable);
    }

    @Override
    public Style getStyle(CodeColorizer.ElementType elementType) {
        switch (elementType) {
            case value -> {return valueStyle;}
            case keyword -> {return punctuationStyle;}
            case randomVariable -> {return randomStyle;}
            case literal -> {return constantStyle;}
            case argumentName -> {return argumentNameStyle;}
            case function -> {return functionStyle;}
            case distibution -> {return genDistStyle;}
            case punctuation -> {return punctuationStyle;}
            case clampedVar -> {return clampedStyle;}
        }
        return punctuationStyle;
    }

    public class LPhyASTVisitor extends LPhyBaseVisitor<Object> {

        public LPhyASTVisitor() { }

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


        /**
         * @param ctx
         * @return a RangeList function.
         */
        @Override
        public Object visitRange_list(Range_listContext ctx) {

            TextElement textElement = (TextElement)visit(ctx.getChild(0));
            for (int i = 1; i < ctx.getChildCount(); i++) {
                TextElement element = (TextElement)visit(ctx.getChild(i));
                if (element != null) {
                    textElement.add(new TextElement(",", punctuationStyle));
                    textElement.add(element);
                }
            }
            return textElement;
        }

        @Override
        public lphystudio.core.codecolorizer.Var visitVar(VarContext ctx) {
            String id = ctx.getChild(0).getText();
            TextElement rangeList = null;
            if (ctx.getChildCount() > 1) {
                // variable of the form NAME '[' range ']'
                rangeList = (TextElement)visit(ctx.getChild(2));

            }

            return new Var(LineCodeColorizer.this, id, rangeList);
        }


        @Override
        public Object visitLiteral(LiteralContext ctx) {
            return new TextElement(ctx.getText(), textPane.getStyle("constantStyle"));
        }

        @Override
        public Object visitMapFunction(MapFunctionContext ctx) {
            TextElement element = new TextElement("{", textPane.getStyle("punctuationStyle"));
            element.add((TextElement)visit(ctx.getChild(1)));
            element.add(new TextElement("}", textPane.getStyle("punctuationStyle")));
            return element;
        }

        //TODO Can access the model tab from data tab, so cannot determine isClamped
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
            ParseTree childContext = ctx.getChild(0);
            String key = childContext.getText();
            TextElement var;
            if (parser.hasValue(key, context) && parser.isClamped(key)) {
                // data clamping
                var = new TextElement(key, textPane.getStyle(ColorizerStyles.clampedVariable));
            } else
                var = new TextElement(key, textPane.getStyle("randomVarStyle"));

            var.add(" " + ctx.getChild(1).getText() + " ", textPane.getStyle("punctuationStyle"));

            addTextElement(var);

            TextElement distributionElement = (TextElement) visit(ctx.getChild(2));

            addTextElement(distributionElement);

            return ctx.getText();
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
                if (LoaderManager.getBivarOperators().contains(s)) {
                    TextElement element = (TextElement) visit(ctx.getChild(0));

                    element.add(s, punctuationStyle);

                    element.add((TextElement) visit(ctx.getChild(ctx.getChildCount() - 1)));
                    return element;
                } else if (s.equals("[")) {
                    // getChild(1) to parse the array index, e.g. x[0]
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

                } else if (s.equals("(")) {

                    TextElement e = new TextElement("(", punctuationStyle);
                    e.add((TextElement) visit(ctx.getChild(1)));
                    e.add(")", punctuationStyle);

                    return e;
                }
                // parsing array moves to visitArray_expression
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

        /**
         * @param ctx the array, e.g. [1,2,3]
         * @return {@link TextElement} of an array, which can be an empty array.
         */
        @Override
        public Object visitArray_construction(Array_constructionContext ctx) {
            if (ctx.getChildCount() >= 2) {

                String s = ctx.getChild(0).getText();

                if (s.equals("[")) {

                    TextElement e = new TextElement("[", punctuationStyle);
                    TextElement textElement = (TextElement) visit(ctx.getChild(1));
                    if (textElement != null)
                        e.add(textElement);
                    e.add("]", punctuationStyle);

                    return e;
                }
            }

            throw new IllegalArgumentException("[ ] are required ! " + ctx.getText());
        }

        @Override
        public Object visitNamed_expression(Named_expressionContext ctx) {
            String name = ctx.getChild(0).getText();
            TextElement element = new TextElement(name + "=", argumentNameStyle);

            Object child = visit(ctx.getChild(2));

            element.add((TextElement) child);

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
            return element;
        }

        @Override
        public Object visitFunction(FunctionContext ctx) {

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
        public Object visitMethodCall(MethodCallContext ctx) {

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
        System.out.println("Parsing " + CASentence + " in code colouriser");

        LPhyASTVisitor visitor = new LPhyASTVisitor();
        // no data and model blocks
        return LPhyParserAction.parse(CASentence, visitor);
    }

    private TextElement getIDElement(String key) {
        if (parser.hasValue(key, context)) {
            Value value = parser.getValue(key, context);

            if (parser.isClamped(key)) // data clamping
                return new TextElement(key, textPane.getStyle(ColorizerStyles.clampedVariable));

            return new TextElement(key, value instanceof RandomVariable ? randomStyle : valueStyle);
        }
        return new TextElement(key, constantStyle);
    }
}
