package james.parser;




import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ParseTree;

import james.Coalescent;
import james.core.ErrorModel;
import james.core.JCPhyloCTMC;
import james.core.PhyloBrownian;
import james.core.PhyloCTMC;
import james.core.distributions.Dirichlet;
import james.core.distributions.DiscretizedGamma;
import james.core.distributions.Exp;
import james.core.distributions.Gamma;
import james.core.distributions.LogNormal;
import james.core.distributions.Normal;
import james.core.functions.BinaryCTMC;
import james.core.functions.GTR;
import james.core.functions.HKY;
import james.core.functions.JukesCantor;
import james.core.functions.K80;
import james.core.functions.Newick;
import james.graphicalModel.DeterministicFunction;
import james.graphicalModel.Func;
import james.graphicalModel.GenerativeDistribution;
import james.graphicalModel.GraphicalModelNode;
import james.graphicalModel.RandomVariable;
import james.graphicalModel.Value;
import james.graphicalModel.types.DoubleValue;
import james.graphicalModel.types.IntegerValue;
import james.parser.SimulatorParser.*;

public class SimulatorListenerImpl extends SimulatorBaseListener {
	
    // CURRENT MODEL STATE
    private SortedMap<String, Value<?>> dictionary;

    // PARSER STATE
    static Map<String, Class<?>> genDistDictionary;
    static Map<String, Class<?>> functionDictionary;
    static Set<String> bivarOperators;

	
	static public void initNameMap() {
		if (genDistDictionary == null) {
			genDistDictionary = new TreeMap<>();
			functionDictionary = new TreeMap<>();
			
	        Class<?>[] genClasses = {Normal.class, LogNormal.class, Exp.class, Coalescent.class, JCPhyloCTMC.class,
	                PhyloCTMC.class, PhyloBrownian.class, Dirichlet.class, Gamma.class, DiscretizedGamma.class,
	                ErrorModel.class};
	
	        for (Class<?> genClass : genClasses) {
	            genDistDictionary.put(genClass.getSimpleName(), genClass);
	        }
	
	        Class<?>[] functionClasses = {james.core.functions.Exp.class, JukesCantor.class, K80.class, HKY.class, GTR.class,
	                BinaryCTMC.class, Newick.class};
	
	        for (Class<?> functionClass : functionClasses) {
	            functionDictionary.put(Func.getFunctionName(functionClass), functionClass);
	        }
		}
		System.out.println(Arrays.toString(genDistDictionary.keySet().toArray()));
		System.out.println(Arrays.toString(functionDictionary.keySet().toArray()));
		
	}

//	private static void initMap(Class baseClass, Map<String, String> mapNameToClass) {
//		List<String> classes = PackageManager.find(baseClass, "jags");
//		for (String _class : classes) {
//			try {
//				Class _impl = Class.forName(_class);
//					if (!Modifier.isAbstract(_impl.getModifiers())) {
//					Constructor<?> ctor;
//					Object t = null;
//					try {
//						ctor = _impl.getConstructor();
//						t = ctor.newInstance();
//					} catch (NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException e) {
//						// ignore
//					}
//					if (t != null) {
//						String name =
//								t instanceof JFunction ?
//								((JFunction)t).getJAGSName() :
//								((JAGSDistribution)t).getName();
//						mapNameToClass.put(name, _class);
//						if (t instanceof JFunction) {
//							name = ((JFunction) t).getJAGSAlias();
//							if (name != null) {
//								mapNameToClass.put(name, _class);
//							}
//						}
//					}
//				}
//			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
//				e.printStackTrace();
//			}
//		}		
//	}

//	static JFunction fun0 = new JFunction() {		
//		@Override
//		public int getDimension() {return 1;}
//		
//		@Override
//		public double getArrayValue(int dim) {return 0;}
//		
//		@Override
//		public double getArrayValue() {return 0;}
//		
//		@Override
//		public int getDimensionCount() {return 1;}
//		
//		@Override
//		public int getDimension(int dim) {return 1;}
//	};
	
	public SimulatorListenerImpl(SortedMap<String, Value<?>> dictionary) {
		this.dictionary = dictionary;
	}
	
	// we want to return JFunction and JFunction[] -- so make it a visitor of Object and cast to expected type
	public class SimulatorASTVisitor extends SimulatorBaseVisitor<Object> {
//		List<Distribution> distributions = new ArrayList<>();
		
		//Map<String, Integer> iteratorValue = new HashMap<>();
		//Map<String, Integer> iteratorDimension = new HashMap<>();
		
		public SimulatorASTVisitor() {
			initNameMap();
			
			bivarOperators = new HashSet<>();
			for (String s : new String[]{"+","-","*","/","**","&&","||","<=","<",">=",">","%",":","^","!=","==","&","|","<<",">>",">>>"}) {
				bivarOperators.add(s);
			}
//			
//			univarDistirbutions = new HashSet<>();
//			bivarDistirbutions = new HashSet<>();
//			trivarDistirbutions = new HashSet<>();
//			for (String s : new String[]{"dchisq" ,"dexp" ,"dpois" ,"dgeom","ddirich"}){
//				univarDistirbutions.add(s);
//			}
//			for (String s : new String[]{"dnorm" ,"dlnorm" ,"dbeta" ,"dnchisq" ,"dnt" ,"dbinom" ,"dnbinom" ,"dnbinom_mu" ,"dcauchy" ,"df" ,"dgamma" ,"dunif" ,"dweibull" ,"dlogis" ,"dsignrank"}){
//				bivarDistirbutions.add(s);
//			}
//			for (String s : new String[]{"dnbeta" ,"dnf" ,"dhyper" ,"dwilcox"}){
//				trivarDistirbutions.add(s);			
//			}

		}
		
		

				
		@Override
		public Value visitConstant(SimulatorParser.ConstantContext ctx) {
			String text = ctx.getText();
			double d = 0;
			try {
				d = Double.parseDouble(text);
				String id = nextID("DoubleValue");
				Value<Double> v = new DoubleValue(id, d);
				return v;
			} catch (NumberFormatException e) {
				try {
					d = Long.parseLong(text);
					String id = nextID("IntegerValue");
					// TODO: should be a LongValue?
					Value<Integer> v = new IntegerValue(id, (int) d);
					return v;
				} catch (NumberFormatException e2) {
					int i = Boolean.parseBoolean(text) ? 1 : 0;
					String id = nextID("IntegerValue");
					Value<Integer> v = new IntegerValue(id, i);
					return v;
				}
			}
		}
	
		private String nextID(String id) {
			int k = 0;
			while (dictionary.containsKey(id + k)) {
				k++;
			}
			return id + k;
		}




		@Override
		public Value visitDeterm_relation(SimulatorParser.Determ_relationContext ctx) {
			// TODO: why not Func -- Func has no apply()?
			Object expr = visit(ctx.getChild(2));
			String id = ctx.children.get(0).getText();
			if (expr instanceof DeterministicFunction) {
				DeterministicFunction f = (DeterministicFunction) expr;
				Value value = f.apply();
				dictionary.put(id, value);
				return value;
	 		} else if (expr instanceof Value) {
				Value value = (Value) expr;
				dictionary.put(id, value);
				return value;
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
//			System.out.println(2);
			GenerativeDistribution genDist = (GenerativeDistribution) visit(ctx.getChild(2));
			String id = ctx.getChild(0).getText();
//			JFunction f;JFunction
//			if (id.indexOf('[') == -1) {
//				f = (JFunction) doc.pluginmap.get(id);
//			} else {
//				id = ctx.getChild(0).getChild(0).getText() + '[';
//				for (int i = 2; i < ctx.getChild(0).getChildCount() -1; i++) {
//					id += (int) ((JFunction) visit(ctx.getChild(0).getChild(i))).getArrayValue();
//					if (i < ctx.getChild(0).getChildCount() -2) {
//						id += ',';
//					}
//				}
//				id += ']';
//				f = (JFunction) visit(ctx.getChild(0));
//			}
//			
//			Distribution distribution = new Distribution(distr, f);
//			distribution.setID("logP." + id);
//			
//			distributions.add(distribution);
//			doc.registerPlugin(distribution);
//			
//			return distribution;

	        RandomVariable var = genDist.sample(id);
	        dictionary.put(var.getId(), var);
	        return var;
		}
		
		@Override
		protected Object aggregateResult(Object aggregate, Object nextResult) {
			if (nextResult != null) {
				return nextResult;
			}
			return aggregate;
		}
		
		@Override
		public Object visitVar(VarContext ctx) {
			String id = ctx.getChild(0).getText();
//			JFunction var = (JFunction) doc.pluginmap.get(id);
//			if (ctx.getChildCount() == 1) {
//				// variable not indexed
//				return var;
//			}
//			JFunction index = (JFunction) visit(ctx.getChild(2));
//			JFunction element = new Index(var, index);
//			return element;
			return id;
		}
		
		
		@Override
		public Object visitExpression(SimulatorParser.ExpressionContext ctx) {
			if (ctx.getChildCount() == 1) {
				String key = ctx.getChild(0).getText();
				if (dictionary.containsKey(key)) {
					return dictionary.get(key);
				}
//				if (iteratorValue.containsKey(key)) {
//					final int ivalue = iteratorValue.get(key);
//					return new JFunction() {						
//						@Override
//						public int getDimension() {return 1;}
//						
//						@Override
//						public double getArrayValue(int dim) {
//							return ivalue;
//						}
//						
//						@Override
//						public double getArrayValue() {
//							return ivalue;
//						}
//						
//						@Override
//						public int getDimensionCount() {return 1;}
//						
//						@Override
//						public int getDimension(int dim) {return 1;}
//					};
//				}
//				return visit(ctx.getChild(0));
//			}
				ExpressionNode transform = null;
			if (ctx.getChildCount() >= 2) {
				String s = ctx.getChild(1).getText();
				if (bivarOperators.contains(s)) {
					Value f1 = (Value) visit(ctx.getChild(0));
					Value f2 = (Value) visit(ctx.getChild(ctx.getChildCount() - 1));


					switch (s) {
					case "+": transform = new ExpressionNode(ctx.getText(), ExpressionNode.plus(), f1, f2); break;
					case "-": transform = new ExpressionNode(ctx.getText(), ExpressionNode.minus(), f1,f2); break;
					case "*": transform = new ExpressionNode(ctx.getText(), ExpressionNode.times(), f1,f2); break;
					case "/": transform = new ExpressionNode(ctx.getText(), ExpressionNode.divide(), f1,f2); break;
					case "**": transform = new ExpressionNode(ctx.getText(), ExpressionNode.pow(), f1,f2); break;
					case "&&": transform = new ExpressionNode(ctx.getText(), ExpressionNode.and(), f1,f2); break;
					case "||": transform = new ExpressionNode(ctx.getText(), ExpressionNode.or(), f1,f2); break;
					case "<=": transform = new ExpressionNode(ctx.getText(), ExpressionNode.le(), f1,f2); break;
					case "<": 
						switch (ctx.getChildCount()) {
						case 3:
							transform = new ExpressionNode(ctx.getText(), ExpressionNode.less(), f1,f2); break;
						case 4:
//							transform = new ExpressionNode(ctx.getText(), ExpressionNode.leftShift(), f1,f2); break;
						} 
						break;
					case ">=": transform = new ExpressionNode(ctx.getText(), ExpressionNode.ge(), f1,f2); break;
					case ">":
						switch (ctx.getChildCount()) {
						case 3:
							transform = new ExpressionNode(ctx.getText(), ExpressionNode.greater(), f1,f2); break;
						case 4:
//							transform = new ExpressionNode(ctx.getText(), ExpressionNode.rightShift(), f1,f2); break;
						case 5:
//							transform = new ExpressionNode(ctx.getText(), ExpressionNode.zeroFillRightShift(), f1,f2); break;
						} 
						break;
					case "!=": transform = new ExpressionNode(ctx.getText(), ExpressionNode.ne(), f1,f2); break;
					case "==": transform = new ExpressionNode(ctx.getText(), ExpressionNode.equals(), f1,f2); break;
					case "%": transform = new ExpressionNode(ctx.getText(), ExpressionNode.mod(), f1,f2); break;

					case "&": transform = new ExpressionNode(ctx.getText(), ExpressionNode.bitwiseand(), f1,f2); break;
					case "|": transform = new ExpressionNode(ctx.getText(), ExpressionNode.bitwiseor(), f1,f2); break;
//					case "^": transform = new ExpressionNode(ctx.getText(), ExpressionNode.bitwiseXOr(), f1,f2); break;
//					case "<<": transform = new ExpressionNode(ctx.getText(), ExpressionNode.leftShift(), f1,f2); break;
//					case ">>": transform = new ExpressionNode(ctx.getText(), ExpressionNode.rightShift(), f1,f2); break;
//					case ">>>": transform = new ExpressionNode(ctx.getText(), ExpressionNode.zeroFillRightShift(), f1,f2); break;
//					case ":": transform = new ExpressionNode(ctx.getText(), ExpressionNode.range(), f1,f2); break;
					}
					return transform; 
				}
//				} else if (s.equals("!")) {
//					JFunction f1 = (JFunction) visit(ctx.getChild(2));
//					transform = new Not(f1);
//				} else if (s.equals("~")) {
//					JFunction f1 = (JFunction) visit(ctx.getChild(2));
//					transform = new Complement(f1);
//				} else if (s.equals("[")) {
//					JFunction var = (JFunction) visit(ctx.getChild(0));
//					JFunction f1 = (JFunction) visit(ctx.getChild(2));
//					transform = new Index(var, f1);
//				}
			}
			}
			return super.visitExpression(ctx);
		}
		
		
		@Override
		public Object visitNamed_expression(Named_expressionContext ctx) {
			String name = ctx.getChild(0).getText();
			Value value = (Value) visit(ctx.getChild(2));
			// TODO: do we really need a new object here?
			Value v = new Value(name, value.value());
			return v;
		}
		
		@Override
		public Object visitDistribution(SimulatorParser.DistributionContext ctx) {
			super.visitDistribution(ctx);
			
			String name = ctx.getChild(0).getText();
			GenerativeDistribution distr = null;
			
			Value [] f = (Value[]) visit(ctx.getChild(2));
			
			
			if (genDistDictionary.containsKey(name)) {
				Class class_ = genDistDictionary.get(name);
				Constructor ctor = null;
				try {
				switch (f.length) {
				case 0: 
					ctor = class_.getConstructor();
					distr = (GenerativeDistribution<?>) ctor.newInstance();
					break;
				case 1: 
					ctor = class_.getConstructor(Value.class); 
					distr = (GenerativeDistribution) ctor.newInstance(f[0]);
					break;
				case 2: 
					ctor = class_.getConstructor(Value.class, Value.class); 
					distr = (GenerativeDistribution) ctor.newInstance(f[0], f[1]);
					break;
				case 3: 
					ctor = class_.getConstructor(Value.class, Value.class, Value.class); 
					distr = (GenerativeDistribution) ctor.newInstance(f[0], f[1], f[2]);
					break;
				}
				} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
					
				}
				return distr;
			}
//			
//			
//			/*
//			if (univarDistirbutions.contains(name)) {
//				switch (name) {
//				//case "dchisq": distr = new Chisq(f1); break;
//				case "dexp": distr = new Exponential(f[0]); break;
//				case "ddirich": distr = new Dirichlet(f[0]); break;
//				//case "dpois": distr = new Pois(f1); break;
//				//case "dgeom": distr = new Geom(f1); break;
//				}
//				
//			} else if (bivarDistirbutions.contains(name)) {
//				switch (name) {
//				case "dnorm": distr = new Normal(f[0],f[1]); break;
//				case "dlnorm": distr = new LogNormal(f[0],f[1]); break;
//				case "dbeta": distr = new Beta(f[0],f[1]); break;
//				//case "dnchisq": distr = new Nchisq(f1,f2); break;
//				//case "dnt": distr = new Nt(f1,f2); break;
//				//case "dbinom": distr = new Binom(f1,f2); break;
//				//case "dnbinom": distr = new Nbinom(f1,f2); break;
//				//case "dnbinom_mu": distr = new Nbinom_mu(f1,f2); break;
//				//case "dcauchy": distr = new Cauchy(f1,f2); break;
//				//case "df": distr = new F(f1,f2); break;
//				//case "dgamma": distr = new Gamma(f1,f2); break;
//				//case "dunif": distr = new Unif(f1,f2); break;
//				//case "dweibull": distr = new Weibull(f1,f2); break;
//				//case "dlogis": distr = new Logis(f1,f2); break;
//				//case "dsignrank": distr = new Signrank(f1,f2); break;
//				}
//			} else if (trivarDistirbutions.contains(name)) {
//				switch (name) {				
//					//case "dnbeta": distr = new Nbeta(f1,f2,f3); break;
//					//case "dnf": distr = new Nf(f1,f2,f3); break;
//					//case "dhyper": distr = new Hyper(f1,f2,f3); break;
//					//case "dwilcox": distr = new Wilcox(f1,f2,f3); break;
//				}
//				
//			} else {
//				throw new IllegalArgumentException("Unknown distributions. Choose one of " +
//						Arrays.toString(univarDistirbutions.toArray()) + 
//						Arrays.toString(bivarDistirbutions.toArray()) + 
//						Arrays.toString(trivarDistirbutions.toArray()) 
//						);
//			}
//			*/
//			if (distr == null) {
//				throw new IllegalArgumentException("Distributions not implemented yet. "
//						+ "Choose one of " + Arrays.toString(mapDistrToClass.keySet().toArray(new String[]{})));
//			}
			return distr; 
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
			List<Value> list = new ArrayList<>();
			for (int i = 0; i < ctx.getChildCount(); i+= 2) {
				list.add((Value) visit(ctx.getChild(i)));
			}
			return list.toArray(new Value[] {});
		}
		
		@Override
		public Value visitMethodCall(SimulatorParser.MethodCallContext ctx) {
//			Transform transform = null;
			String functionName = ctx.children.get(0).getText();
			ParseTree ctx2 = ctx.getChild(2);
			Value [] f= (Value []) visit(ctx2);
			DeterministicFunction func = null;
			if (functionDictionary.containsKey(functionName)) {
				Class class_ = functionDictionary.get(functionName);
				Constructor ctor = null;
				try {
				switch (f.length) {
				case 0: 
					ctor = class_.getConstructor();
					func = (DeterministicFunction<?>) ctor.newInstance();
					break;
				case 1: 
					ctor = class_.getConstructor(Value.class); 
					func = (DeterministicFunction) ctor.newInstance(f[0]);
					break;
				case 2: 
					ctor = class_.getConstructor(Value.class, Value.class); 
					func = (DeterministicFunction) ctor.newInstance(f[0], f[1]);
					break;
				case 3: 
					ctor = class_.getConstructor(Value.class, Value.class, Value.class); 
					func = (DeterministicFunction) ctor.newInstance(f[0], f[1], f[2]);
					break;
				}
				} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
					
				}
			}
			return func.apply();

//			
//			if (functionName.equals("c")) {
//				JFunction [] f= (JFunction []) visit(ctx.getChild(2));				
//				Concat c = new Concat(f);
//				return c;
//			}
//			
//			// process expression_list
//			JFunction [] f =  (JFunction[]) visit(ctx.getChild(2));
//			if (mapNameToClass.containsKey(functionName)) {
//				String className = mapNameToClass.get(functionName);
//				Constructor ctor = null;
//				try {
//				switch (f.length) {
//				case 0: 
//					ctor = Class.forName(className).getConstructor();
//					transform = (Transform) ctor.newInstance();
//					break;
//				case 1: 
//					ctor = Class.forName(className).getConstructor(JFunction.class); 
//					transform = (Transform) ctor.newInstance(f[0]);
//					break;
//				case 2: 
//					ctor = Class.forName(className).getConstructor(JFunction.class, JFunction.class); 
//					transform = (Transform) ctor.newInstance(f[0], f[1]);
//					break;
//				case 3: 
//					ctor = Class.forName(className).getConstructor(JFunction.class, JFunction.class, JFunction.class); 
//					transform = (Transform) ctor.newInstance(f[0], f[1], f[2]);
//					break;
//				}
//				} catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
//					// ignore
//				}			return null;
//				if (transform != null) {
//					return transform;
//				}
//			}
//			switch (functionName) {
//				case "length": transform = new Length(f[0]);break;
//				case "dim": transform = new Dim(f[0]);break;
//			/*
//				// Univariable functions
//
//				case "sort": transform = new Sort(f[0]);break;
//				case "rank": transform = new Rank(f[0]);break;
//				case "order": transform = new Order(f[0]);break;
//				case "inverse": transform = new Inverse(f[0]);break;
//				case "t": transform = new Transpose(f[0]);break;
//				
//				case "abs": transform = new Abs(f[0]);break;
//				case "cos": transform = new Cos(f[0]);break;
//				case "sin": transform = new Sin(f[0]);break;
//				case "tan": transform = new Tan(f[0]);break;
//				case "arccos": 
//				case "acos": transform = new Acos(f[0]);break;
//				case "arcsin": 
//				case "asin": transform = new Asin(f[0]);break;
//				case "arctan": 
//				case "atan": transform = new Atan(f[0]);break;
//				case "sinh": transform = new Sinh(f[0]);break;
//				case "cosh": transform = new Cosh(f[0]);break;
//				case "tanh": transform = new Tanh(f[0]);break;
//				case "arcsinh": 
//				case "asinh": transform = new Asinh(f[0]);break;
//				case "arccosh": 
//				case "acosh": transform = new Acosh(f[0]);break;
//				case "arctanh": 
//				case "atanh": transform = new Atanh(f[0]);break;
//
//				case "cbrt": transform = new Cbrt(f[0]);break;
//				case "cloglog": transform = new CLogLog(f[0]);break;
//				case "sqrt": transform = new Sqrt(f[0]);break;
//				case "exp": transform = new Exp(f[0]);break;
//				case "expm1": transform = new Expm1(f[0]);break;
//				case "log": transform = new jags.functions.Log(f[0]);break;
//				case "log10": transform = new Log10(f[0]);break;
//				case "log1p": transform = new Log1p(f[0]);break;
//				case "logdet": transform = new LogDet(f[0]);break;
//				case "loggamm": transform = new LogGamma(f[0]);break;
//				case "logit": transform = new Logit(f[0]);break;
//				case "logfact": transform = new LogFact(f[0]);break;
//				case "probit": transform = new Probit(f[0]);break;
//				case "ceil": transform = new Ceil(f[0]);break;
//				case "trunc":
//				case "floor": transform = new Floor(f[0]);break;
//				case "round": transform = new Round(f[0]);break;
//				case "signum": transform = new Signum(f[0]);break;
//				case "step": transform = new Step(f[0]);break;
//				case "mean": transform = new Mean(f[0]);break;
//				case "sd": transform = new StdDev(f[0]);break;
//				
//				// Bivariable functions
//				case "hypot": transform = new Hypot(f[0], f[1]);break;
//				case "atan2": transform = new Atan2(f[0], f[1]);break;
//				case "pow": transform = new Pow(f[0], f[1]);break;
//				case "rep": transform = new Rep(f[0], f[1]);break;
//				caObjectse "prod":
//				case "%*%": transform = new MatrixMult(f[0], f[1]);break;
//				case "equals": transform = new Eq(f[0], f[1]);break;
//				
//				case "ifelse": transform = new IfElse(f[0], f[1], f[2]);break;
//				case "interp.lin": transform = new InterpLin(f[0], f[1], f[2]);break;
//				*/
//				case "inprod": transform = new Times(f[0], f[1]); break;
//				case "prod":f.apply()
//				case "%*%": transform = new MatrixMult(f[0], f[1]);break;
//
//				case "min": transform = new Min(f);break;
//				case "max": transform = new Max(f);break;
//				case "sum": transform = new Sum(f);break;
//
//				default:
//					throw new IllegalArgumentException("Unknown function : " + functionName);
//			}
//			
//			return transform;
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
        	    if ( e instanceof NoViableAltException ) {
        	    	NoViableAltException nvae = (NoViableAltException)e;
        	    	System.out.println(nvae.getLocalizedMessage());
//              msg = "X no viable alt; token="+nvae.token+
//                 " (decision="+nvae.decisionNumber+
//                 " state "+nvae.stateNumber+")"+
//                 " decision=<<"+nvae.grammarDecisionDescription+">>";
           }
           else {
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
	
}
