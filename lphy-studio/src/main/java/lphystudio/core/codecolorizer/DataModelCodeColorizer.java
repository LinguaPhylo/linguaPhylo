package lphystudio.core.codecolorizer;


import lphy.core.logger.LoggerUtils;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.parser.LPhyParserAction;
import lphy.core.parser.LPhyParserDictionary;
import lphy.core.parser.antlr.LPhyBaseListener;
import lphy.core.parser.antlr.LPhyBaseVisitor;
import lphy.core.parser.antlr.LPhyParser.*;
import lphy.core.spi.LoaderManager;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.antlr.v4.runtime.tree.ParseTree;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

/**
 * Implement the listener for the parse tree colouring LPhy code.
 * TODO merge common code with {@link LineCodeColorizer}
 */
public class DataModelCodeColorizer extends LPhyBaseListener implements CodeColorizer,LPhyParserAction {

    // CURRENT MODEL STATE

    private JTextPane textPane;
    static int argumentNameSize = 10;

    Style randomVarStyle;
    Style literalStyle;
    Style argumentNameStyle;
    Style functionStyle;
    Style genDistStyle;
    Style punctuationStyle;
    Style valueStyle;
    Style keywordStyle;
    Style clampedStyle;

    LPhyParserDictionary parser;

    LPhyParserDictionary.Context context = LPhyParserDictionary.Context.model;

    // the indent within a block
    protected String indent = "  ";

    public DataModelCodeColorizer(LPhyParserDictionary parser, JTextPane pane) {

        this.parser = parser;
        textPane = pane;

        ColorizerStyles.addStyles(pane);

        keywordStyle = textPane.getStyle(ColorizerStyles.keyword);
        functionStyle = textPane.getStyle(ColorizerStyles.function);
        genDistStyle = textPane.getStyle(ColorizerStyles.distribution);
        randomVarStyle = textPane.getStyle(ColorizerStyles.randomVariable);
        valueStyle = textPane.getStyle(ColorizerStyles.value);
        argumentNameStyle = textPane.getStyle(ColorizerStyles.argumentName);
        literalStyle = textPane.getStyle(ColorizerStyles.constant);
        punctuationStyle = textPane.getStyle(ColorizerStyles.punctuation);
        clampedStyle = pane.getStyle(ColorizerStyles.clampedVariable);
    }

    @Override
    public Style getStyle(ElementType elementType) {
        switch (elementType) {
            case value -> {return valueStyle;}
            case keyword -> {return keywordStyle;}
            case randomVariable -> {return randomVarStyle;}
            case literal -> {return literalStyle;}
            case argumentName -> {return argumentNameStyle;}
            case function -> {return functionStyle;}
            case distibution -> {return genDistStyle;}
            case punctuation -> {return punctuationStyle;}
            case clampedVar -> {return clampedStyle;}
        }
        return punctuationStyle;
    }

    public class DataModelASTVisitor extends LPhyBaseVisitor<Object> {

        public DataModelASTVisitor() {
        }

        public void addTextElement(TextElement element) {
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
        public Object visitDatablock(DatablockContext ctx) {

            context = LPhyParserDictionary.Context.data;

            TextElement element = new TextElement(ctx.getChild(0).getText() + " {\n", keywordStyle);

            addTextElement(element);
            Object children = visitChildren(ctx);

            element = new TextElement("}\n", keywordStyle);

            addTextElement(element);
            return children;
        }

        @Override
        public Object visitModelblock(ModelblockContext ctx) {

            context = LPhyParserDictionary.Context.model;

            TextElement element = new TextElement(ctx.getChild(0).getText() + " {\n", keywordStyle);

            addTextElement(element);
            Object children = visitChildren(ctx);

            element = new TextElement("}\n", keywordStyle);

            addTextElement(element);
            return children;
        }

        @Override
        public Object visitMapFunction(MapFunctionContext ctx) {
            TextElement element = new TextElement("{", textPane.getStyle("punctuationStyle"));
            element.add((TextElement)visit(ctx.getChild(1)));
            element.add(new TextElement("}", textPane.getStyle("punctuationStyle")));
            return element;
        }

        @Override
        public Object visitLiteral(LiteralContext ctx) {

            return new TextElement(ctx.getText(), literalStyle);
        }

        @Override
        public Object visitDeterm_relation(Determ_relationContext ctx) {

            TextElement element = new TextElement(indent, punctuationStyle);

            lphystudio.core.codecolorizer.Var var = (lphystudio.core.codecolorizer.Var)visit(ctx.getChild(0));
            element.add(var.getTextElement(parser, context));

            element.add(" = ", punctuationStyle);

            TextElement expr = (TextElement) visit(ctx.getChild(2));

            element.add(expr);
            element.add(";\n", punctuationStyle);

            addTextElement(element);
            return element;
        }

        @Override
        public Object visitStoch_relation(Stoch_relationContext ctx) {

            TextElement varText = new TextElement(indent, punctuationStyle);

            ParseTree childContext = ctx.getChild(0);
            String key = childContext.getText();
            if (parser.hasValue(key, context) && parser.isObserved(key)) {
                // data clamping
                varText.add(key, textPane.getStyle(ColorizerStyles.clampedVariable));
            } else
                varText.add(key, randomVarStyle);

            varText.add(" " + ctx.getChild(1).getText() + " ", punctuationStyle);

            addTextElement(varText);

            TextElement distributionElement = (TextElement) visit(ctx.getChild(2));

            addTextElement(distributionElement);

            return ctx.getText();
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

            return new lphystudio.core.codecolorizer.Var(DataModelCodeColorizer.this, id, rangeList);
        }

        @Override
        public Object visitExpression(ExpressionContext ctx) {

            if (ctx.getChildCount() == 1) {

                ParseTree childContext = ctx.getChild(0);

                // if this is a map just return the map Value
                if (childContext.getText().startsWith("{")) {
                    return visit(childContext);
                }

                String key = childContext.getText();
                if (parser.hasValue(key, context)) {
                    Value value = parser.getValue(key, context);
                    return new TextElement(key, value instanceof RandomVariable ? textPane.getStyle("randomVarStyle") : textPane.getStyle("valueStyle") );
                }
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
                return new TextElement((String) exp, literalStyle);
            }

            if (exp == null) {
                return new TextElement("null", literalStyle);
                //throw new RuntimeException("exp is null for expression context: " + ctx.getText() + " child count = " + ctx.getChildCount());
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
                e.add((TextElement) visit(ctx2));
            }
            e.add(")", punctuationStyle);


            return e;
        }

        @Override
        public Object visitMethodCall(MethodCallContext ctx) {

            //            lphystudio.core.codecolorizer.Var var = (Var)visit(ctx.getChild(0));
            //            TextElement e = var.getTextElement(parser, context);
            //            String methodName = ctx.children.get(2).getText();

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

    private TextElement getIDElement(String key) {
        if (parser.hasValue(key, context)) {
            Value value = parser.getValue(key, context);

            if (parser.isObserved(key)) // data clamping
                return new TextElement(key, textPane.getStyle(ColorizerStyles.clampedVariable));

            return new TextElement(key, value instanceof RandomVariable ? randomVarStyle : valueStyle);
        }
        return new TextElement(key, literalStyle);
    }

    public Object parse(String CASentence) {

        LoggerUtils.log.fine("Parsing " + CASentence + " in code colouriser");

        // Traverse parse tree
        AbstractParseTreeVisitor visitor = new DataModelASTVisitor();

        // containing either or both a data and model block;
        return LPhyParserAction.parse(CASentence, visitor);
    }

}
