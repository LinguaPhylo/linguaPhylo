package lphy.parser;


import lphy.core.LPhyParser;
import lphy.core.functions.DoubleArray;
import lphy.core.functions.IntegerArray;
import lphy.core.functions.Range;
import lphy.evolution.DataFrame;
import lphy.graphicalModel.*;
import lphy.graphicalModel.types.*;
import lphy.parser.SimulatorParser.Expression_listContext;
import lphy.parser.SimulatorParser.Named_expressionContext;
import lphy.parser.SimulatorParser.Unnamed_expression_listContext;
import lphy.parser.SimulatorParser.VarContext;
import lphy.utils.LoggerUtils;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Level;

public class SimulatorListenerImpl extends AbstractBaseListener {

    LPhyParser.Context context;

    public SimulatorListenerImpl(LPhyParser parser, LPhyParser.Context context) {
        this.parser = parser;
        this.context = context;

    }

    private void put(String id, Value val) {
        switch (context) {
            case data: parser.getDataDictionary().put(id, val); break;
            case model: default:
                parser.getModelDictionary().put(id, val);
        }
    }

    private Value<?> get(String id) {
        return parser.getValue(id, context);
    }

    private boolean containsKey(String id) {
        return parser.hasValue(id,context);
    }

    // we want to return JFunction and JFunction[] -- so make it a visitor of Object and cast to expected type
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

        public Object visitRange_list(SimulatorParser.Range_listContext ctx) {

            System.out.println("Visiting a range list:" + ctx.getText());

            List<Integer> range = new ArrayList<>();
            for (int i = 0; i < ctx.getChildCount(); i++) {
                Object o = visit(ctx.getChild(i));
                if (o instanceof Integer) {
                    range.add((Integer) o);
                } else if (o == null) {
                    // ignore commas
                } else {
                    throw new IllegalArgumentException("Expected integer value, but don't know how to handle " +
                            o == null ? "null" : o.getClass().getName());
                }
            }
            return range;
        }

        public Object visitRange_element(SimulatorParser.Range_elementContext ctx) {

            System.out.println("Visiting a range element:" + ctx.getText());

            Object o = visitChildren(ctx);
            if (o instanceof DoubleValue) {
                return new Integer((int) (double) ((DoubleValue) o).value());
            }
            if (o instanceof IntegerValue) {
                return ((IntegerValue) o).value();
            }
            throw new IllegalArgumentException("Expected integer value, but don't know how to handle " +
                    (o == null ? "null" : o.getClass().getName()));
        }

        @Override
        public Value visitConstant(SimulatorParser.ConstantContext ctx) {

            String text = ctx.getText();
            if (text.startsWith("\"")) {
                //String id = nextID("StringValue");
                StringValue v = new StringValue(null, stripQuotes(text));
                return v;
            }
            double d = 0;
            try {
                d = Long.parseLong(text);
                //String id = nextID("IntegerValue");
                // TODO: should be a LongValue?
                Value<Integer> v = new IntegerValue(null, (int) d);
                return v;
            } catch (NumberFormatException e) {
                try {
                    d = Double.parseDouble(text);
                    //String id = nextID("DoubleValue");
                    Value<Double> v = new DoubleValue(null, d);
                    return v;
                } catch (NumberFormatException e2) {
                    boolean bool = Boolean.parseBoolean(text);
                    //String id = nextID("IntegerValue");
                    Value<Boolean> v = new BooleanValue(null, bool);
                    return v;
                }
            }
        }

        private String stripQuotes(String stringWithQuotes) {
            if (stringWithQuotes.startsWith("\"") && stringWithQuotes.endsWith("\"")) {
                return stringWithQuotes.substring(1, stringWithQuotes.length() - 1);
            } else throw new RuntimeException();
        }

        private String nextID(String id) {
            int k = 0;
            while (parser.hasValue(id + k, context)) {
                k++;
            }
            return id + k;
        }

        @Override
        public Value visitDeterm_relation(SimulatorParser.Determ_relationContext ctx) {
            // TODO: why not Func -- Func has no apply()?
            LoggerUtils.log.fine(" visitDeterm_relation");

            Object expr = visit(ctx.getChild(2));
            Object o = visit(ctx.children.get(0));
            String id = o instanceof String ? (String) o : ((RangedVar) o).id;

            LoggerUtils.log.fine("   id = " + id);
            if (expr instanceof DeterministicFunction) {
                DeterministicFunction f = (DeterministicFunction) expr;
                Value value = f.apply();
                value.setFunction(f);
                value.setId(id);
                if (o instanceof RangedVar) {

                    if (parser.hasValue(id, context)) {
                        Value v = parser.getValue(id, context);
                        List<Integer> range = ((RangedVar) o).range;
                        for (int i = 0; i < range.size(); i++) {
                            int index = range.get(i);
                            // TODO: figure out  what to do here?
                        }
                    }

                } else {
                    put(id, value);
                    LoggerUtils.log.fine("   adding value " + value + " to the dictionary");
                }
                return value;
            } else if (expr instanceof Value) {
                Value value = (Value) expr;
                value.setId(id);

                if (o instanceof RangedVar) {

                    if (containsKey(id)) {
                        Value v = get(id);
                        List<Integer> range = ((RangedVar) o).range;
                        for (int i = 0; i < range.size(); i++) {
                            int index = range.get(i);
                            // TODO: figure out  what to do here?
                        }
                    }

                } else {
                    put(id, value);
                    LoggerUtils.log.fine("   adding value " + value + " to the dictionary");
                }
                return value;
            } else {
                LoggerUtils.log.severe("in visitDeterm_relation() expecting a function or a value!");

            }
            return null;
//			if (id.indexOf('[') >= 0) {
//				id = ctx.getChild(0).getChild(0).getText();
//				JFunction range = (JFunction) visit(ctx.getChild(0).getChild(2));
//				Variable c = null;
//				if (doc.pluginmap.containsKey(id)) {
//					c = (Variable) doc.pluginmap.get(id);
//					c.setValue(range, f);
//				} else {
//					throw new IllegalArgumentException("Variable " + id + " should have been declared before using [] notation");
//					//c = new Variable(id, f, dimensions);
//					//c.setValue(range, f);
//				}
//				return c;
//			}
//
//			Variable c = new Variable(f);
//			c.setID(id);
//			doc.registerPlugin(c);
//			System.out.println(c);			
//			return c;<?>
        }

        @Override
        public Value visitStoch_relation(SimulatorParser.Stoch_relationContext ctx) {

            if (context == LPhyParser.Context.data) {
                throw new RuntimeException("Generative distributions are not allowed in the data block!");
            }

            GenerativeDistribution genDist = (GenerativeDistribution) visit(ctx.getChild(2));
            String id = ctx.getChild(0).getText();

            RandomVariable var = genDist.sample(id);
            put(var.getId(), var);
            return var;
        }

        @Override
        protected Object aggregateResult(Object aggregate, Object nextResult) {
            if (nextResult != null) {
                return nextResult;
            }
            return aggregate;
        }

        class RangedVar {
            String id;
            List<Integer> range;

            RangedVar(String id, List<Integer> range) {
                this.id = id;
                this.range = range;
            }
        }

        @Override
        public Object visitVar(VarContext ctx) {

            String id = ctx.getChild(0).getText();
            if (ctx.getChildCount() > 1) {
                // variable of the form NAME '[' range ']'
                Object o = visit(ctx.getChild(2));
                if (o instanceof List) {
                    List<Integer> range = (List<Integer>) o;
                    return new RangedVar(id, range);
                } else {
                    throw new IllegalArgumentException("Expected list of integer values, but don't know how to handle " +
                            o == null ? "null" : o.getClass().getName());
                }
            }

            LoggerUtils.log.log(Level.FINE, "  visitVar: " + id);

            return id;
        }


        @Override
        public Object visitExpression(SimulatorParser.ExpressionContext ctx) {

            // Deals with single token expressions -- probably an id referring to previously defined constant or variable
            if (ctx.getChildCount() == 1) {
                String key = ctx.getChild(0).getText();
                if (containsKey(key)) {
                    return get(key);
                }
            }
            ExpressionNode expression = null;
            if (ctx.getChildCount() >= 2) {
                String s = ctx.getChild(1).getText();
                if (bivarOperators.contains(s)) {
                    Value f1 = new ValueOrFunction(visit(ctx.getChild(0))).getValue();

                    Value f2 = new ValueOrFunction(visit(ctx.getChild(ctx.getChildCount() - 1))).getValue();

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
                } else if (s.equals("[")) {
                    Value[] var = (Value[]) visit(ctx.getChild(1));
                    if (var[0].value() instanceof Double[]) {
                        Double[][] value = new Double[var.length][];
                        for (int i = 0; i < value.length; i++) {
                            value[i] = (Double[]) var[i].value();
                        }
                        DoubleArray2DValue v = new DoubleArray2DValue(null, value);
                        return v;
                    } else if (var[0].value() instanceof Double) {

                        if (allConstants(var)) {
                            Double[] value = new Double[var.length];
                            for (int i = 0; i < value.length; i++) {
                                value[i] = (Double) var[i].value();
                            }
                            DoubleArrayValue v = new DoubleArrayValue(null, value);
                            return v;
                        } else {
                            DoubleArray doubleArray = new DoubleArray(var);
                            return doubleArray.apply();
                        }
                    }
                    if (var[0].value() instanceof Integer[]) {
                        Integer[][] value = new Integer[var.length][];
                        for (int i = 0; i < value.length; i++) {
                            value[i] = (Integer[]) var[i].value();
                        }
                        IntegerArray2DValue v = new IntegerArray2DValue(null, value);
                        return v;
                    } else if (var[0].value() instanceof Integer) {
                        if (allConstants(var)) {
                            Integer[] value = new Integer[var.length];
                            for (int i = 0; i < value.length; i++) {
                                value[i] = (Integer) var[i].value();
                            }
                            IntegerArrayValue v = new IntegerArrayValue(null, value);
                            return v;
                        } else {
                            IntegerArray intArray = new IntegerArray(var);
                            return intArray.apply();
                        }
                    }
                    if (var[0].value() instanceof String[]) {
                        String[][] value = new String[var.length][];
                        for (int i = 0; i < value.length; i++) {
                            value[i] = (String[]) var[i].value();
                        }
                        StringArray2DValue v = new StringArray2DValue(null, value);
                        return v;
                    } else if (var[0].value() instanceof String) {
                        if (allConstants(var)) {
                            String[] value = new String[var.length];
                            for (int i = 0; i < value.length; i++) {
                                value[i] = (String) var[i].value();
                            }
                            StringArrayValue v = new StringArrayValue(null, value);
                            return v;
                        } else {
                            IntegerArray intArray = new IntegerArray(var);
                            return intArray.apply();
                        }
                    } else {
                        throw new RuntimeException("Don't know how to handle 3D matrices");
                    }
//				}
                }
            }
            return super.visitExpression(ctx);
        }

        class ValueOrFunction {

            Object obj;

            public ValueOrFunction(Object obj) {
                this.obj = obj;
                if (!(obj instanceof Value) && !(obj instanceof DeterministicFunction)) throw new RuntimeException();
            }

            Value getValue() {
                if (obj instanceof Value) return (Value) obj;
                if (obj instanceof DeterministicFunction) {
                    DeterministicFunction func = (DeterministicFunction) obj;
                    Value val = func.apply();
                    val.setFunction(func);
                    return val;
                }
                throw new RuntimeException();
            }
        }

        class NamedValue {
            public NamedValue(String name, Value value) {
                this.name = name;
                this.value = value;
            }

            String name;
            Value value;
        }

        @Override
        public Object visitNamed_expression(Named_expressionContext ctx) {
            String name = ctx.getChild(0).getText();
            Object obj = visit(ctx.getChild(2));

            LoggerUtils.log.log(Level.FINE, " Visiting named expression:");
            LoggerUtils.log.log(Level.FINE, "   name: " + name + " child2: " + obj);

            if (obj instanceof DeterministicFunction) {
                Value value = ((DeterministicFunction) obj).apply();
                value.setFunction(((DeterministicFunction) obj));
                NamedValue v = new NamedValue(name, value);
                return v;
            }

            if (obj instanceof Value) {
                Value value = (Value) obj;
                NamedValue v = new NamedValue(name, value);
                return v;
            }
            return obj;
        }

        @Override
        public Object visitDistribution(SimulatorParser.DistributionContext ctx) {
            // super.visitDistribution(ctx);

            String name = ctx.getChild(0).getText();
            NamedValue[] f = (NamedValue[]) visit(ctx.getChild(2));
            Map<String, Value> arguments = new HashMap<>();
            for (NamedValue v : f) {
                arguments.put(v.name, v.value);
            }

            Set<Class<?>> genDistClasses = genDistDictionary.get(name);

            if (genDistClasses == null)
                throw new RuntimeException("Found no implementation for generative distribution " + name);

            for (Class genDistClass : genDistClasses) {
                try {
                    List<Object> initargs = new ArrayList<>();
                    Constructor constructor = getConstructorByArguments(arguments, genDistClass, initargs);

                    if (constructor != null) {
                        GenerativeDistribution dist = (GenerativeDistribution) constructor.newInstance(initargs.toArray());
                        for (String parameterName : arguments.keySet()) {
                            Value value = arguments.get(parameterName);

                            dist.setInput(parameterName, value);
                        }
                        return dist;
                    }
                } catch (InstantiationException e) {
                    e.printStackTrace();
                    throw new RuntimeException("Parsing generative distribution " + name + " failed.");
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    throw new RuntimeException("Parsing generative distribution " + name + " failed.");
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                    throw new RuntimeException("Parsing generative distribution " + name + " failed.");
                }
            }
            throw new RuntimeException("Parser exception: no constructor found for " + name);


//	        Class genDistClass = genDistDictionary.get(name);
//	        if (genDistClass == null) {
//	            throw new RuntimeException("Parsing error: Unrecognised generative distribution: " + name);
//	        }
//
//	        try {
//	            List<Object> initargs = new ArrayList<>();
//	            Constructor constructor = getConstructorByArguments(arguments, genDistClass, initargs);
//	            if (constructor == null) {
//	            	constructor = getConstructorByArguments(arguments, genDistClass, initargs);
//	                throw new RuntimeException("Parser error: no constructor found for generative distribution " + name + " with arguments " + arguments);
//	            }
//
//	            GenerativeDistribution dist = (GenerativeDistribution) constructor.newInstance(initargs.toArray());
//	            for (String parameterName : arguments.keySet()) {
//	                Value value = arguments.get(parameterName);
//	                dist.setInput(parameterName, arguments.get(parameterName));
//	            }
//	            return dist;
//	        } catch (InstantiationException | IllegalAccessException | InvocationTargetException  e) {
//	            e.printStackTrace();
//	            throw new RuntimeException("Parsing generative distribution " + name + " failed. " + e.getMessage());
//	        }

        }


//		@Override // for_loop: counter relations 
//		public Object visitFor_loop(SimulatorParser.For_loopContext ctx) {
//			ParseTree counter = ctx.getChild(0);
//			// counter: FOR '(' NAME IN range_element ')'
//			String name = counter.getChild(2).getText();
//			JFunction range = (JFunction) visit(counter.getChild(4));
//			iteratorDimension.remove(range.getDimension());
//			for (int i = 0; i < range.getDimension(); i++) {
//				int value = (int) range.getArrayValue(i);
//				iteratorValue.put(name, value);
//				visit(ctx.getChild(1));
//			}
//			iteratorValue.remove(name);
//			iteratorDimension.remove(name);
//			return null;
//		}			return null;


        @Override
        public Object visitExpression_list(Expression_listContext ctx) {
            List<NamedValue> list = new ArrayList<>();
            for (int i = 0; i < ctx.getChildCount(); i += 2) {
                list.add((NamedValue) visit(ctx.getChild(i)));
            }
            return list.toArray(new NamedValue[]{});
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
                } else throw new RuntimeException("Found a non-value, non-function in unnamed expression list");
            }
            return list.toArray(new Value[]{});
        }

        @Override
        public Object visitMethodCall(SimulatorParser.MethodCallContext ctx) {

            String functionName = ctx.children.get(0).getText();
            ParseTree ctx2 = ctx.getChild(2);
            Value[] f1 = null;
            Object argumentObject = null;
            NamedValue[] namedValues = null;
            if (ctx2.getText().equals(")")) {
                f1 = new Value[]{};
            } else {
                argumentObject = visit(ctx2);
                if (argumentObject instanceof Value[]) {
                    f1 = (Value[]) argumentObject;
                } else if (argumentObject instanceof NamedValue[]) {
                    namedValues = (NamedValue[]) argumentObject;
                    f1 = new Value[namedValues.length];
                    for (int i = 0; i < namedValues.length; i++) {
                        f1[i] = namedValues[i].value;
                    }
                }
            }

            if (univarfunctions.contains(functionName)) {
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

            Set<Class<?>> functionClasses = functionDictionary.get(functionName);

            if (functionClasses == null)
                throw new RuntimeException("Found no implementation for function " + functionName);

            Map<String, Value> arguments = new HashMap<>();
            if (namedValues != null) {
                for (NamedValue v : namedValues) {
                    arguments.put(v.name, v.value);
                }
            }

            for (Class functionClass : functionClasses) {
                try {
                    List<Object> initargs = new ArrayList<>();
                    Constructor constructor = null;
                    if (namedValues != null) {
                        constructor = getConstructorByArguments(arguments, functionClass, initargs);
                    } else {
                        constructor = getConstructorByArguments(f1, functionClass, initargs);
                    }

                    if (constructor != null) {
                        DeterministicFunction f = (DeterministicFunction) constructor.newInstance(initargs.toArray());

                        if (namedValues != null) {
                            for (String parameterName : arguments.keySet()) {
                                Value value = arguments.get(parameterName);

                                f.setInput(parameterName, value);
                            }
                        } else if (f1.length > 0) {
                            f.setInput(f.getParamName(0), f1[0]);
                        } // else f1 empty and cannot setInput
                        Value val = f.apply();
                        return val;
                    }
                } catch (InstantiationException e) {
                    e.printStackTrace();
                    throw new RuntimeException("Parsing generative distribution " + functionName + " failed.");
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    throw new RuntimeException("Parsing generative distribution " + functionName + " failed.");
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                    throw new RuntimeException("Parsing generative distribution " + functionName + " failed.");
                }
            }
            throw new RuntimeException("Parser exception: no constructor found for " + functionName);

        }
    }

//    private Map<String, Value<?>> dictionary() {
//        switch (context) {
//            case data: return parser.getDataDictionary();
//
//        }
//    }

    private boolean allConstants(Value[] var) {
        for (Value v : var) {
            if (!v.isConstant()) return false;
        }
        return true;
    }

    private Constructor getConstructorByArguments(Map<String, Value> arguments, Class generatorClass, List<Object> initargs) {
        for (Constructor constructor : generatorClass.getConstructors()) {
            List<ParameterInfo> pInfo = Generator.getParameterInfo(constructor);

            if (arguments.size() == 1 && pInfo.size() == 1) {
                initargs.add(arguments.values().iterator().next());
                return constructor;
            }

            if (match(arguments, pInfo)) {
                for (int i = 0; i < pInfo.size(); i++) {
                    Value arg = arguments.get(pInfo.get(i).name());
                    if (arg != null) {
                        initargs.add(arg);
                    } else if (!pInfo.get(i).optional()) {
                        throw new RuntimeException("Required argument " + pInfo.get(i).name() + " not found!");
                    } else {
                        initargs.add(null);
                    }
                }
                return constructor;
            }
        }
        return null;
    }

    private Constructor getConstructorByArguments(Value[] values, Class generatorClass, List<Object> initargs) {
        for (Constructor constructor : generatorClass.getConstructors()) {
            List<ParameterInfo> pInfo = Generator.getParameterInfo(constructor);

            if (values.length == 1 && pInfo.size() == 1) {
                initargs.add(values[0]);
                return constructor;
            }
            if (values.length == 0 && pInfo.size() == 1) {
                initargs.add(null);
                return constructor;
            }
        }
        return null;
    }


    /**
     * A match occurs if the required parameters are in the argument map and the remaining arguments in the map match names of optional arguments.
     *
     * @param arguments
     * @param pInfo
     * @return
     */
    private boolean match(Map<String, Value> arguments, List<ParameterInfo> pInfo) {

        Set<String> requiredArguments = new TreeSet<>();
        Set<String> optionalArguments = new TreeSet<>();
        for (ParameterInfo pinfo : pInfo) {
            if (pinfo.optional()) {
                optionalArguments.add(pinfo.name());
            } else {
                requiredArguments.add(pinfo.name());
            }
        }

        if (!arguments.keySet().containsAll(requiredArguments)) {
            return false;
        }
        Set<String> allArguments = optionalArguments;
        allArguments.addAll(requiredArguments);
        return allArguments.containsAll(arguments.keySet());
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


    public static void main(String[] args) throws IOException {
        if (args.length == 1) {
            SimulatorListenerImpl parser = new SimulatorListenerImpl(new REPL(), LPhyParser.Context.model);
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
