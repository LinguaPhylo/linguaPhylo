package lphy.core.parser;

import lphy.core.exception.SimulatorParsingException;
import lphy.core.logger.LoggerUtils;
import lphy.core.model.*;
import lphy.core.model.datatype.*;
import lphy.core.parser.antlr.LPhyBaseListener;
import lphy.core.parser.antlr.LPhyBaseVisitor;
import lphy.core.parser.antlr.LPhyParser.*;
import lphy.core.parser.argument.ArgumentValue;
import lphy.core.parser.function.ExpressionNode1Arg;
import lphy.core.parser.function.ExpressionNode2Args;
import lphy.core.parser.function.MapFunction;
import lphy.core.parser.function.MethodCall;
import lphy.core.parser.graphicalmodel.ArrayCreator;
import lphy.core.spi.LoaderManager;
import lphy.core.vectorization.IID;
import lphy.core.vectorization.VectorizedDistribution;
import lphy.core.vectorization.array.*;
import lphy.core.vectorization.operation.Range;
import lphy.core.vectorization.operation.RangeList;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static lphy.core.parser.Var.getIndexedValue;

/**
 * Implement the listener for LPhy parse tree.
 */
public class LPhyListenerImpl extends LPhyBaseListener implements LPhyParserAction {

//    static {
//        ParserLoader.getInstance();
//    }

    // default to parse as the model block.
    LPhyParserDictionary.Context context = LPhyParserDictionary.Context.model;

    // the parser object that stores all parsed values.
    LPhyParserDictionary parserDictionary;

    public LPhyListenerImpl(LPhyParserDictionary parserDictionary) {
        this.parserDictionary = parserDictionary;
    }

    public Object parse(String CASentence) {
        LPhyASTVisitor visitor = new LPhyASTVisitor();
        return LPhyParserAction.parse(CASentence, visitor);
    }

    // for studio console cmd
//    public Object parse(String CASentence, LPhyParserDictionary.Context context) {
//        if (CASentence.matches("data\\s*\\{") || CASentence.matches("model\\s*\\{")) {
//            throw new IllegalArgumentException("Do not input data or model keywords into the studio console !" +
//                    "\n" + CASentence);
//        }
//        // set context
//        this.context = context;
//        // no data and model blocks
//        return this.parse(CASentence);
//    }


    public LPhyParserDictionary getParserDictionary() {
        return parserDictionary;
    }

    public void clear() {
        this.parserDictionary.clear();
        // back to default
        context = LPhyParserDictionary.Context.model;
    }

    /**
     * Puts the given value in the parser dictionary under the given id, based on the current context.
     *
     * @param id
     * @param val
     */
    private void put(String id, Value val) {
        parserDictionary.put(id, val, context);
//        switch (context) {
//            case data:
//                parserDictionary.getDataDictionary().put(id, val);
//                parserDictionary.getDataValues().add(val);
//                break;
//            case model:
//            default:
//                parserDictionary.getModelDictionary().put(id, val);
//                parserDictionary.getModelValues().add(val);
//        }
    }

    private Value<?> get(String id) {
        return parserDictionary.getValue(id, context);
    }

    private boolean containsKey(String id) {
        return parserDictionary.hasValue(id, context);
    }

    // we want to return JFunction and JFunction[] -- so make it a visitor of Object and cast to expected type
    public class LPhyASTVisitor extends LPhyBaseVisitor<Object> {

        public LPhyASTVisitor() {
        }

        @Override
        public Object visitDatablock(DatablockContext ctx) {
            // set context to data
            context = LPhyParserDictionary.Context.data;
            return super.visitDatablock(ctx);
        }

        @Override
        public Object visitModelblock(ModelblockContext ctx) {
            // set context to model
            context = LPhyParserDictionary.Context.model;
            return super.visitModelblock(ctx);
        }

        @Override
        public Object visitFree_lines(Free_linesContext ctx) {
            // no data/model keywords, set context to model
            context = LPhyParserDictionary.Context.model;
            return super.visitFree_lines(ctx);
        }

        /**
         * parse the string with double quotes first,
         * if not, then go to {@link #visitFloatingPointLiteral(FloatingPointLiteralContext)},
         * {@link #visitIntegerLiteral(IntegerLiteralContext)},
         * and {@link #visitBooleanLiteral(BooleanLiteralContext)}, according to grammar.
         * @param ctx the Literals
         * @return  a LPhy {@link Value}
         */
        @Override
        public Value visitLiteral(LiteralContext ctx) {
            String stringWithQuotes = ctx.getText();
            // only support ", not ', see grammar
            if (stringWithQuotes.startsWith("\"")) {
                if (stringWithQuotes.startsWith("\"") && stringWithQuotes.endsWith("\"")) {
                    String text = stringWithQuotes.substring(1, stringWithQuotes.length() - 1);
                    return new StringValue(null, text);
                } else
                    throw new RuntimeException("Attempted to strip quotes, " +
                            "but the string was not quoted by double quotes !");
            }
            // not currently allowed by grammar
//            if (text.startsWith("'") && text.endsWith("'") && text.length() == 3) {
//                return new CharacterValue(null, text.charAt(1));
//            }

            // go to other visitor if not string with quotes
            return (Value) super.visitLiteral(ctx);
        }

        @Override
        public Object visitFloatingPointLiteral(FloatingPointLiteralContext ctx) {
            String text = ctx.getText();
//            try {
            double d = Double.parseDouble(text);
            return new DoubleValue(null, d);
//            } catch (NumberFormatException e) {
            //TODO better Exception?
//            }
        }

        @Override
        public Object visitIntegerLiteral(IntegerLiteralContext ctx) {
            String text = ctx.getText();
//            try {
//                long aLong = Long.parseLong(text);
            // TODO: should be a LongValue?
            int i = Integer.parseInt(text);
            return new IntegerValue(null, i); // (int) aLong
//            } catch (NumberFormatException e) {
            //TODO better Exception?
//            }
        }

        @Override
        public Object visitBooleanLiteral(BooleanLiteralContext ctx) {
            String text = ctx.getText();
            boolean bool = Boolean.parseBoolean(text);
            return new BooleanValue(null, bool);
        }

        /**
         * @param ctx
         * @return a RangeList function.
         */
        @Override
        public Object visitRange_list(Range_listContext ctx) {

            List<GraphicalModelNode> nodes = new ArrayList<>();

            for (int i = 0; i < ctx.getChildCount(); i++) {
                Object o = visit(ctx.getChild(i));
                if (o instanceof IntegerValue || o instanceof IntegerArrayValue || o instanceof Range) {
                    nodes.add((GraphicalModelNode) o);
                } else if (o instanceof DeterministicFunction) {
                    DeterministicFunction f = (DeterministicFunction)o;
                    if (f.value() instanceof Integer || f.value() instanceof Integer[]) {
                        nodes.add((GraphicalModelNode)o);
                    } else {
                        LoggerUtils.log.severe("Expected function returning Integer or Integer[]: " + o);
                        throw new SimulatorParsingException("Expected function returning Integer or Integer[]:  " +
                                o == null ? "null" : o.getClass().getName(), ctx);

                    }
                } else if (o == null) {
                    // ignore commas
                } else {
                    LoggerUtils.log.severe("Expected Integer value, or Range, in range list, but found: " + o);
                    throw new SimulatorParsingException("Expected Integer value, or Range, in range list, but don't know how to handle " +
                            o == null ? "null" : o.getClass().getName(), ctx);
                }
            }
            return new RangeList(nodes.toArray(new GraphicalModelNode[0]));
        }

//        /**
//         * @param ctx
//         * @return RangeElement
//         */
//        public RangeElement visitRange_element(Range_elementContext ctx) {
//
//            Object o = visitChildren(ctx);
//
//            if (o instanceof RangeElement) {
//                return (RangeElement) o;
//            }
//
//            LoggerUtils.log.severe("Expected Integer value, or Range, in range element, but found: " + o);
//
//            throw new SimulatorParsingException("Expected integer value, or range, but don't know how to handle " +
//                    (o == null ? "null" : o.getClass().getName()), ctx);
//        }

        @Override
        public Value visitDeterm_relation(Determ_relationContext ctx) {
            // TODO: why not Func -- Func has no apply()?
            LoggerUtils.log.fine(" visitDeterm_relation");

            Object expr = visit(ctx.getChild(2));
            Var var = (Var)visit(ctx.children.get(0));
            String id = var.id;

            if (expr instanceof DeterministicFunction) {
                DeterministicFunction f = (DeterministicFunction) expr;
                Value value = f.apply();
                var.assign(value, f, context);
                return value;
            } else if (expr instanceof Value) {
                Value value = (Value) expr;
                var.assign(value, null, context);
                return value;
            } else {
                LoggerUtils.log.severe("in visitDeterm_relation() expecting a function or a value! " + expr);

            }
            return null;
        }

        /**
         * @param ctx
         * @return a RandomVariable generated by a Generative Distribution
         */
        public Value visitStoch_relation(Stoch_relationContext ctx) {

            if (context == LPhyParserDictionary.Context.data) {
                throw new SimulatorParsingException("Generative distributions are not allowed in the data block! Use model block for Generative distributions. ", ctx);
            }

            GenerativeDistribution genDist = (GenerativeDistribution) visit(ctx.getChild(2));

            Var var = (Var)visit(ctx.getChild(0));
            RandomVariable variable = null;

            if (DataClampingUtils.isDataClamping(var, parserDictionary)) {
                // data clamping
                Object valueVal = Objects.requireNonNull(parserDictionary.getDataDictionary().get(var.getId())).value();

                // when the generator is VectorizedDistribution,
                // data clamping requires to wrap the list of component RandomVariable into VectorizedRandomVariable,
                // so that the equation and narrative can be generated properly
                if (genDist instanceof VectorizedDistribution<?> vectDist && valueVal.getClass().isArray()) {
                    variable = DataClampingUtils.clampDataToVectorizedRandomVariable(
                            (Object[]) valueVal, var.getId(), vectDist);
                } else if (genDist instanceof IID<?> iid && valueVal.getClass().isArray()) {
                    variable = DataClampingUtils.clampDataToVectorizedRandomVariable(
                            (Object[]) valueVal, var.getId(), iid);
                } else {
                    // singe var
                    variable = new RandomVariable(var.getId(), valueVal, genDist);
                    variable.setClamped(true);
                }
                LoggerUtils.log.info("Data clamping: the value of " + var.getId() +
                        " in the 'model' block is replaced by the value of " + var.getId() + " in the 'data' block .");

            } else {
                variable = genDist.sample(var.getId());
            }

            if (variable != null && !var.isRangedVar()) {
                put(variable.getId(), variable);
                return variable;
            } else {
                throw new SimulatorParsingException("Data clamping requires to data as array object !", ctx);
//                // if already exists
//                if (get(rv.id) != null) {
//                    addElementToVariable(rv.id, rv.range, variable);
//                } else {
//                    RandomVariable parentVariable = new RandomVariable(rv.id, )
//                }
            }
        }

        @Override
        protected Object aggregateResult(Object aggregate, Object nextResult) {
            if (nextResult != null) {
                return nextResult;
            }
            return aggregate;
        }

        /**
         * @param ctx the VarContext
         * @return var
         */
        @Override
        public Var visitVar(VarContext ctx) {

            String id = ctx.getChild(0).getText();
            if (ctx.getChildCount() > 1) {
                // variable of the form NAME '[' range ']'
                Object o = visit(ctx.getChild(2));
                if (o instanceof RangeList) {
                    return new Var(id, (RangeList) o, parserDictionary);
                } else {
                    throw new SimulatorParsingException("Expected variable id, or id and range list", ctx);
                }
            }


            return new Var(id, parserDictionary);
        }

        /** TODO 2d-array indexing? #365
         * @param ctx
         * @return a Slice or ElementsAt function, e.g. x[0]
         */
        private Object visitIndexRange(ExpressionContext ctx) {

            Object child = visit(ctx.getChild(0));

            Value array = new ValueOrFunction(child, ctx).getValue();

            if (!array.value().getClass().isArray()) {
                throw new SimulatorParsingException("Expected value " + array + " to be an array.", ctx);
            }

            RangeList rangeList = (RangeList) visit(ctx.getChild(2));

            return getIndexedValue(array, rangeList);
        }

        @Override
        public Object visitExpression(ExpressionContext ctx) {

            // Deals with single token expressions -- either an id or a map expression
            if (ctx.getChildCount() == 1) {
                ParseTree childContext = ctx.getChild(0);

                // if this is a map just return the map Value
                if (childContext.getText().startsWith("{")) {
                    Object obj = visit(childContext);

                    return obj;
                }

                String key = childContext.getText();
                if (containsKey(key)) {
                    return get(key);
                }
//                else {
//                    return new PlaceHolder(key);
//                }
            }
            ExpressionNode expression = null;
            if (ctx.getChildCount() >= 2) {
                String s = ctx.getChild(1).getText();

                // getChild(1) to parse the array index, e.g. x[0]
                if (s.equals("[")) {
                    return visitIndexRange(ctx);
                }

                if (LoaderManager.getBivarOperators().contains(s)) {

                    Value f1 = new ValueOrFunction(visit(ctx.getChild(0)), ctx).getValue();

                    Value f2 = new ValueOrFunction(visit(ctx.getChild(ctx.getChildCount() - 1)), ctx).getValue();

                    switch (s) {
                        case "+":
                            expression = new ExpressionNode2Args(ctx.getText(), ExpressionNode2Args.plus(), f1, f2);
                            break;
                        case "-":
                            expression = new ExpressionNode2Args(ctx.getText(), ExpressionNode2Args.minus(), f1, f2);
                            break;
                        case "*":
                            expression = new ExpressionNode2Args(ctx.getText(), ExpressionNode2Args.times(), f1, f2);
                            break;
                        case "/":
                            expression = new ExpressionNode2Args(ctx.getText(), ExpressionNode2Args.divide(), f1, f2);
                            break;
                        case "**":
                            expression = new ExpressionNode2Args(ctx.getText(), ExpressionNode2Args.pow(), f1, f2);
                            break;
                        case "&&":
                            expression = new ExpressionNode2Args(ctx.getText(), ExpressionNode2Args.and(), f1, f2);
                            break;
                        case "||":
                            expression = new ExpressionNode2Args(ctx.getText(), ExpressionNode2Args.or(), f1, f2);
                            break;
                        case "<=":
                            expression = new ExpressionNode2Args(ctx.getText(), ExpressionNode2Args.le(), f1, f2);
                            break;
                        case "<":
                            switch (ctx.getChildCount()) {
                                case 3:
                                    expression = new ExpressionNode2Args(ctx.getText(), ExpressionNode2Args.less(), f1, f2);
                                    break;
                                case 4:
//							transform = new ExpressionNode(ctx.getText(), ExpressionNode.leftShift(), f1,f2); break;
                            }
                            break;
                        case ">=":
                            expression = new ExpressionNode2Args(ctx.getText(), ExpressionNode2Args.ge(), f1, f2);
                            break;
                        case ">":
                            switch (ctx.getChildCount()) {
                                case 3:
                                    expression = new ExpressionNode2Args(ctx.getText(), ExpressionNode2Args.greater(), f1, f2);
                                    break;
                                case 4:
//							transform = new ExpressionNode(ctx.getText(), ExpressionNode.rightShift(), f1,f2); break;
                                case 5:
//							transform = new ExpressionNode(ctx.getText(), ExpressionNode.zeroFillRightShift(), f1,f2); break;
                            }
                            break;
                        case "!=":
                            expression = new ExpressionNode2Args(ctx.getText(), ExpressionNode2Args.ne(), f1, f2);
                            break;
                        case "==":
                            expression = new ExpressionNode2Args(ctx.getText(), ExpressionNode2Args.equals(), f1, f2);
                            break;
                        case "%":
                            expression = new ExpressionNode2Args(ctx.getText(), ExpressionNode2Args.mod(), f1, f2);
                            break;
                        case "&":
                            expression = new ExpressionNode2Args(ctx.getText(), ExpressionNode2Args.bitwiseand(), f1, f2);
                            break;
                        case "|":
                            expression = new ExpressionNode2Args(ctx.getText(), ExpressionNode2Args.bitwiseor(), f1, f2);
                            break;
//					case "^": transform = new ExpressionNode(ctx.getText(), ExpressionNode.bitwiseXOr(), f1,f2); break;
//					case "<<": transform = new ExpressionNode(ctx.getText(), ExpressionNode.leftShift(), f1,f2); break;
//					case ">>": transform = new ExpressionNode(ctx.getText(), ExpressionNode.rightShift(), f1,f2); break;
//					case ">>>": transform = new ExpressionNode(ctx.getText(), ExpressionNode.zeroFillRightShift(), f1,f2); break;
                        case ":":
                            return new Range(f1, f2);
                    }
                    return expression;
                }
                s = ctx.getChild(0).getText();

                if (s.equals("!")) {
                    Value f1 = (Value) visit(ctx.getChild(2));
                    expression = new ExpressionNode1Arg(ctx.getText(), ExpressionNode1Arg.not(), f1);
                    return expression;
                }
                // parsing array moves to visitArray_expression
            }

            return super.visitExpression(ctx);
        }

        class ValueOrFunction {

            Object obj;
            ParserRuleContext ctx;

            public ValueOrFunction(Object obj, ParserRuleContext ctx) {
                this.obj = obj;
                this.ctx = ctx;
                if (!(obj instanceof Value) && !(obj instanceof DeterministicFunction)) {
                    throw new SimulatorParsingException("Expected value or function but got " + obj + (obj != null ? (" of class " + obj.getClass().getName()) : ""), ctx);
                }
                if (context == LPhyParserDictionary.Context.data) {
                    parserDictionary.getDataValues().add(getValue());
                } else {
                    parserDictionary.getModelValues().add(getValue());
                }
            }

            Value getValue() {
                if (obj instanceof Value) return (Value) obj;
                if (obj instanceof DeterministicFunction) {
                    DeterministicFunction func = (DeterministicFunction) obj;
                    Value val = func.apply();
                    val.setFunction(func);
                    return val;
                }
                throw new SimulatorParsingException("Expected value or function but got " + obj + (obj != null ? (" of class " + obj.getClass().getName()) : ""), ctx);
            }
        }

        @Override
        public Object visitNamed_expression(Named_expressionContext ctx) {
            String name = ctx.getChild(0).getText();
            Object obj = visit(ctx.getChild(2));

            if (obj instanceof DeterministicFunction) {
                Value value = ((DeterministicFunction) obj).apply();
                value.setFunction(((DeterministicFunction) obj));
                ArgumentValue v = new ArgumentValue(name, value, parserDictionary, context);
                return v;
            }

            if (obj instanceof Value) {
                Value value = (Value) obj;
                ArgumentValue v = new ArgumentValue(name, value, parserDictionary, context);
                return v;
            }
            return obj;
        }

        /**
         * @param ctx
         * @return A generative distribution object if a match can be found.
         */
        public Object visitDistribution(DistributionContext ctx) {

            String name = ctx.getChild(0).getText();
            ArgumentValue[] f = (ArgumentValue[]) visit(ctx.getChild(2));
            Map<String, Value> arguments = new LinkedHashMap<>(); // cannot use HashMap<>(); issue 423

            for (int i = 0; i < f.length; i++) {
                ArgumentValue v = f[i];
                if (v != null) {
                    arguments.put(v.getName(), v.getValue());
                } else {
                    throw new SimulatorParsingException("Argument " + i + " unexpectedly null", ctx);
                }
            }

            Generator generator;
            List<Generator> matches = ParserUtils.getMatchingGenerativeDistributions(name, arguments);
            switch (matches.size()) {
                case 0:
                    throw new SimulatorParsingException("No generative distribution named " + name + " found matching arguments " + arguments, ctx);
                case 1:
                default:
                    if (matches.size() > 1) {
                        LoggerUtils.log.warning("Found " + matches.size() + " matches for " + name + ". Picking first one!");
                    }
                    generator = matches.get(0);
                    // must be done so that Values all know their outputs
                    for (Map.Entry<String, Value> entry : arguments.entrySet()) {
                        generator.setInput(entry.getKey(), entry.getValue());
                    }
                    return generator;
            }

        }

        /**
         * @param ctx
         * @return an array of ArgumentValue objects
         */
        public Object visitExpression_list(Expression_listContext ctx) {
            List<ArgumentValue> list = new ArrayList<>();
            for (int i = 0; i < ctx.getChildCount(); i += 2) {
                list.add((ArgumentValue) visit(ctx.getChild(i)));
            }
            return list.toArray(new ArgumentValue[]{});
        }

        @Override
        public Object visitUnnamed_expression_list(Unnamed_expression_listContext ctx) {
            List<Value> list = new ArrayList<>();
            for (int i = 0; i < ctx.getChildCount(); i += 2) {

                Object obj = visit(ctx.getChild(i));

                if (obj instanceof DeterministicFunction) {
                    Value value = ((DeterministicFunction) obj).apply();
                    value.setFunction(((DeterministicFunction) obj));
                    list.add(value);
                } else if (obj instanceof Value) {
                    Value value = (Value) obj;
                    list.add(value);
                } else if (obj == null) {
                    list.add(null);
                } else throw new SimulatorParsingException("Found a non-value, non-function in unnamed expression list: " + obj, ctx);
            }
            return list.toArray(new Value[]{});
        }

        /**
         * @param ctx the array, e.g. [1,2,3]
         * @return an array object depending on its type
         */
        @Override
        public Object visitArray_construction(Array_constructionContext ctx) {
            if (ctx.getChildCount() >= 2) {
                String s = ctx.getChild(0).getText();
                if (s.equals("[")) {
                    // get unnamed expression list
                    Value[] var = (Value[]) visit(ctx.getChild(1));

                    Class<?> type = ArrayCreator.getType(var);

                    Value res;
                    // if all values null assume double array
                    if (type == Double.class) {
                        // The commented code below is the incorrect way to handle var[i] == null.
                        // it is against the principle to create new Value, which could lose
                        // their generator. Handle it in DoubleArray apply()

//                        if (allConstants(var)) {
//                            Double[] value = new Double[var.length];
//                            for (int i = 0; i < value.length; i++) {
//                                if (var[i] != null) value[i] = (Double) var[i].value();
//                            }
//                            return new DoubleArrayValue(null, value);
//                        } else {
                        DoubleArray doubleArray = new DoubleArray(var);
                        res = doubleArray.apply();
//                        }
                    } else if (type == Double[].class) {
                        //TODO not sure how to do properly for 2d ?
                        DoubleArray2D doubleArray2D = new DoubleArray2D(var);
                        return doubleArray2D.apply();

                    } else if (type == Integer[].class) {
                        IntegerArray2D integerArray2D = new IntegerArray2D(var);
                        return integerArray2D.apply();

                    } else if (type == Integer.class) {
                        IntegerArray intArray = new IntegerArray(var);
                        res = intArray.apply();

                    } else if (type == Boolean[].class) {
                        BooleanArray2D booleanArray2D = new BooleanArray2D(var);
                        return booleanArray2D.apply();

                    } else if (type == Boolean.class) {
                        BooleanArray booleanArray = new BooleanArray(var);
                        res = booleanArray.apply();

                    } else if (type == String[].class) {
                        StringArray2D stringArray2D = new StringArray2D(var);
                        return stringArray2D.apply();

                    } else if (type == String.class) {
                        StringArray stringArray = new StringArray(var);
                        res = stringArray.apply();

                    } else if (type == Number[].class) {
                        NumberArray2D numberArray2D = new NumberArray2D(var);
                        return numberArray2D.apply();

                    } else if (type == Number.class) {
                        NumberArray numberArray = new NumberArray(var);
                        res = numberArray.apply();

                    } else if (type == Object[].class) {
                        ObjectArray2D objectArray2D = new ObjectArray2D(var);
                        return objectArray2D.apply();

                    } else {
                        // handle generic value array construction
                        ObjectArray objectArray = new ObjectArray(var);
                        res = objectArray.apply();
                    }

                    // It is necessary here to set generator to null, in order to avoid break constant array into pieces,
                    // because doubleArray function will display in the graphical model.
                    if (allConstants(var))
                        res.setFunction(null);
                    return res;
                }
                throw new IllegalArgumentException("[ ] are required ! " + ctx.getText());
            }
            return super.visitArray_construction(ctx);
        }

        /**
         * @param var
         * @return true if all values are null or constant.
         */
        private boolean allConstants(Value[] var) {
            for (Value v : var) {
                if (v != null && !v.isConstant()) return false;
            }
            return true;
        }


        /**
         * @param ctx
         * @return A map function of the name=value pairs contained in this map expression
         */
        @Override
        public Object visitMapFunction(MapFunctionContext ctx) {
            // handle special map function!
            ParseTree ctx1 = ctx.getChild(1);
            LoggerUtils.log.info("parsing a map expression: " + ctx1.getText());

            ArgumentValue[] argumentObject = (ArgumentValue[]) visit(ctx1);
            Generator generator = new MapFunction(argumentObject);
            return generator;
        }

        /**
         * @param ctx
         * @return a Value or an Expression.
         */
        @Override
        public Object visitMethodCall(MethodCallContext ctx) {

            Var var = (Var)visit(ctx.children.get(0));
            String methodName = ctx.children.get(2).getText();
            Value value = null;
            Object childObject;

            if (!var.isRangedVar()) {
                value = get(var.id);
            } else {
                value = getIndexedValue(get(var.id), var.rangeList).apply();
            }

            if (value == null) {
                throw new SimulatorParsingException("Value " + ctx.children.get(0).getText() + " not found for method call " + methodName, ctx);
            }

            ParseTree ctx2 = ctx.getChild(4);

            Value[] f1 = new Value[]{};
            Object argumentObject = null;
            ArgumentValue[] argumentValues = null;
            if (ctx2.getText().equals(")")) {
                f1 = new Value[]{};
            } else {
                argumentObject = visit(ctx2);
                if (argumentObject instanceof Value[]) {
                    f1 = (Value[]) argumentObject;
                } else if (argumentObject instanceof ArgumentValue[]) {
                    argumentValues = (ArgumentValue[]) argumentObject;
                    f1 = new Value[argumentValues.length];
                    for (int i = 0; i < argumentValues.length; i++) {
                        f1[i] = argumentValues[i].getValue();
                    }
                }
            }

            try {
                return new MethodCall(methodName, value, f1);
            } catch (NoSuchMethodException e) {
                LoggerUtils.log.severe("Method call " + methodName + " failed on object " + value.getId());
                throw new SimulatorParsingException(e.getMessage(), ctx);
            }
        }

        /**
         * @param ctx
         * @return a Value or an Expression.
         */
        @Override
        public Object visitFunction(FunctionContext ctx) {

            String functionName = ctx.children.get(0).getText();
            ParseTree ctx2 = ctx.getChild(2);

            Value[] f1 = null;
            Object argumentObject = null;
            ArgumentValue[] argumentValues = null;
            if (ctx2.getText().equals(")")) {
                f1 = new Value[]{};
            } else {
                argumentObject = visit(ctx2);
                if (argumentObject instanceof Value[]) {
                    f1 = (Value[]) argumentObject;
                } else if (argumentObject instanceof ArgumentValue[]) {
                    argumentValues = (ArgumentValue[]) argumentObject;
                    f1 = new Value[argumentValues.length];
                    for (int i = 0; i < argumentValues.length; i++) {
                        if (argumentValues[i] == null)
                            throw new SimulatorParsingException("Argument " + (i + 1) + " from " + functionName + " is null ! ", ctx);
                        f1[i] = argumentValues[i].getValue();
                    }
                }
            }

            if (LoaderManager.getUnivarfunctions().contains(functionName)) {
                ExpressionNode expression = null;
                switch (functionName) {
                    case "abs":
                        expression = new ExpressionNode1Arg(ctx.getText(), ExpressionNode1Arg.abs(), f1);
                        break;
                    case "acos":
                        expression = new ExpressionNode1Arg(ctx.getText(), ExpressionNode1Arg.acos(), f1);
                        break;
                    case "acosh":
                        expression = new ExpressionNode1Arg(ctx.getText(), ExpressionNode1Arg.acosh(), f1);
                        break;
                    case "asin":
                        expression = new ExpressionNode1Arg(ctx.getText(), ExpressionNode1Arg.asin(), f1);
                        break;
                    case "asinh":
                        expression = new ExpressionNode1Arg(ctx.getText(), ExpressionNode1Arg.asinh(), f1);
                        break;
                    case "atan":
                        expression = new ExpressionNode1Arg(ctx.getText(), ExpressionNode1Arg.atan(), f1);
                        break;
                    case "atanh":
                        expression = new ExpressionNode1Arg(ctx.getText(), ExpressionNode1Arg.atanh(), f1);
                        break;
                    case "cLogLog":
                        expression = new ExpressionNode1Arg(ctx.getText(), ExpressionNode1Arg.cLogLog(), f1);
                        break;
                    case "cbrt":
                        expression = new ExpressionNode1Arg(ctx.getText(), ExpressionNode1Arg.cbrt(), f1);
                        break;
                    case "ceil":
                        expression = new ExpressionNode1Arg(ctx.getText(), ExpressionNode1Arg.ceil(), f1);
                        break;
                    case "cos":
                        expression = new ExpressionNode1Arg(ctx.getText(), ExpressionNode1Arg.cos(), f1);
                        break;
                    case "cosh":
                        expression = new ExpressionNode1Arg(ctx.getText(), ExpressionNode1Arg.cosh(), f1);
                        break;
                    case "exp":
                        expression = new ExpressionNode1Arg(ctx.getText(), ExpressionNode1Arg.exp(), f1);
                        break;
                    case "expm1":
                        expression = new ExpressionNode1Arg(ctx.getText(), ExpressionNode1Arg.expm1(), f1);
                        break;
                    case "floor":
                        expression = new ExpressionNode1Arg(ctx.getText(), ExpressionNode1Arg.floor(), f1);
                        break;
                    case "log":
                        expression = new ExpressionNode1Arg(ctx.getText(), ExpressionNode1Arg.log(), f1);
                        break;
                    case "log10":
                        expression = new ExpressionNode1Arg(ctx.getText(), ExpressionNode1Arg.log10(), f1);
                        break;
                    case "log1p":
                        expression = new ExpressionNode1Arg(ctx.getText(), ExpressionNode1Arg.log1p(), f1);
                        break;
                    case "logFact":
                        expression = new ExpressionNode1Arg(ctx.getText(), ExpressionNode1Arg.logFact(), f1);
                        break;
                    case "logGamma":
                        expression = new ExpressionNode1Arg(ctx.getText(), ExpressionNode1Arg.logGamma(), f1);
                        break;
                    case "logit":
                        expression = new ExpressionNode1Arg(ctx.getText(), ExpressionNode1Arg.logit(), f1);
                        break;
                    case "phi":
                        expression = new ExpressionNode1Arg(ctx.getText(), ExpressionNode1Arg.phi(), f1);
                        break;
                    case "probit":
                        expression = new ExpressionNode1Arg(ctx.getText(), ExpressionNode1Arg.probit(), f1);
                        break;
                    case "round":
                        expression = new ExpressionNode1Arg(ctx.getText(), ExpressionNode1Arg.round(), f1);
                        break;
                    case "signum":
                        expression = new ExpressionNode1Arg(ctx.getText(), ExpressionNode1Arg.signum(), f1);
                        break;
                    case "sin":
                        expression = new ExpressionNode1Arg(ctx.getText(), ExpressionNode1Arg.sin(), f1);
                        break;
                    case "sinh":
                        expression = new ExpressionNode1Arg(ctx.getText(), ExpressionNode1Arg.sinh(), f1);
                        break;
                    case "sqrt":
                        expression = new ExpressionNode1Arg(ctx.getText(), ExpressionNode1Arg.sqrt(), f1);
                        break;
                    case "step":
                        expression = new ExpressionNode1Arg(ctx.getText(), ExpressionNode1Arg.step(), f1);
                        break;
                    case "tan":
                        expression = new ExpressionNode1Arg(ctx.getText(), ExpressionNode1Arg.tan(), f1);
                        break;
                    case "tanh":
                        expression = new ExpressionNode1Arg(ctx.getText(), ExpressionNode1Arg.tanh(), f1);
                        break;
                }
                return expression;
            }

            Set<Class<?>> functionClasses = LoaderManager.getFunctionClasses(functionName);

            if (functionClasses == null) {
                throw new SimulatorParsingException("Found no implementation for function with name " + functionName, ctx);
            }

            Map<String, Value> arguments = new LinkedHashMap<>(); // cannot use HashMap<>(); issue 423
            if (argumentValues != null) {
                for (ArgumentValue v : argumentValues) {
                    arguments.put(v.getName(), v.getValue());
                }
            }

            Generator generator;
            List<Generator> matches;
            if (argumentValues == null) {
                matches = ParserUtils.getMatchingFunctions(functionName, f1);
            } else {
                matches = ParserUtils.getMatchingFunctions(functionName, arguments);
            }
            switch (matches.size()) {
                case 0:
                    LoggerUtils.log.severe("Found no function for " + functionName + " matching arguments " + (argumentValues != null ? arguments : f1));
                    return null;
                case 1:
                default:
                    if (matches.size() > 1)
                        LoggerUtils.log.severe("Found " + matches.size() + " matches for " + functionName + ". Picking first one!");
                    generator = matches.get(0);
                    // must be done so that Values all know their outputs
                    for (Map.Entry<String, Value> entry : arguments.entrySet()) {
                        generator.setInput(entry.getKey(), entry.getValue());
                    }
                    return generator.generate();
            }

        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 1) {
            LPhyListenerImpl parser = new LPhyListenerImpl(new REPL());
            BufferedReader fin = new BufferedReader(new FileReader(args[0]));
            StringBuffer buf = new StringBuffer();
            String str = null;
            while (fin.ready()) {
                str = fin.readLine();
                buf.append(str);
                buf.append('\n');
            }
            fin.close();
            Object o = parser.parse(buf.toString());
            System.err.println("OK Done!");
        } else {
            throw new IllegalArgumentException("Expected 1 argument: a file name");
        }
    }

}
