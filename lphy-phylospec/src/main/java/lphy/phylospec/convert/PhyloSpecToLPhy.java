package lphy.phylospec.convert;

import lphy.core.logger.LoggerUtils;
import lphy.core.model.GenerativeDistribution;
import lphy.core.model.Generator;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.parser.ObservationUtils;
import lphy.core.parser.ParserUtils;
import lphy.core.parser.REPL;
import lphy.core.parser.argument.Argument;
import lphy.core.parser.argument.ArgumentUtils;
import lphy.core.parser.function.ExpressionNode1Arg;
import lphy.core.parser.function.ExpressionNode2Args;
import lphy.core.parser.function.MethodCall;
import lphy.core.parser.graphicalmodel.ArrayCreator;
import lphy.core.parser.graphicalmodel.GraphicalModel;
import lphy.core.parser.graphicalmodel.ValueCreator;
import lphy.core.vectorization.IID;
import lphy.core.vectorization.VectorizedDistribution;
import lphy.core.vectorization.VectorizedFunction;
import lphy.phylospec.convert.PhyloSpecNames.IIDShorthand;
import lphy.phylospec.convert.PhyloSpecNames.LPhyGenerator;
import lphy.phylospec.convert.PhyloSpecNames.MethodCallEntry;
import org.phylospec.ast.*;
import org.phylospec.components.ComponentResolver;
import org.phylospec.lexer.TokenType;
import org.phylospec.tiling.errors.TileApplicationError;
import org.phylospec.typeresolver.VariableResolver;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Converts a type-checked PhyloSpec AST into an LPhy model held in a {@link REPL}: each statement
 * ({@code ~}, {@code =}, {@code observed as}) becomes a named LPhy value, and each expression becomes
 * an LPhy {@link Value} or an unsampled {@link GenerativeDistribution}. See CLASS_DESIGN3.md §3.
 * <p>
 * Every failure is a {@link TileApplicationError} attached to the innermost AST node that failed.
 * @author Walter Xie
 */
public class PhyloSpecToLPhy implements AstVisitor<Void, Object, Void> {

    private static final String NOT_SUPPORTED = "Not supported by LPhy yet";
    private static final String TAXON = "taxon";
    private static final String TAXA = "taxa";

    private final REPL dict;                             // the model being built
    private final VariableResolver variableResolver;     // from the front end
    private final ComponentResolver componentResolver;   // the instance TypeResolver used (§3.5)
    private final PhyloSpecNames names;
    private final Map<Stmt.Draw, Stmt.ObservedAs> observedDraws = new IdentityHashMap<>();
    // statements being converted, to report a cyclic definition instead of overflowing the stack
    private final Set<Stmt> inProgress = Collections.newSetFromMap(new IdentityHashMap<>());
    private final AstPrinter printer = new AstPrinter();

    public PhyloSpecToLPhy(REPL dict, VariableResolver variableResolver,
                           ComponentResolver componentResolver, PhyloSpecNames names) {
        this.dict = dict;
        this.variableResolver = variableResolver;
        this.componentResolver = componentResolver;
        this.names = names;
    }

    /** Converts all top-level statements, in source order, into dict. */
    public void convert(List<Stmt> statements) {
        for (Stmt stmt : statements) {
            if (stmt instanceof Stmt.ObservedAs observedAs && observedAs.stmt instanceof Stmt.Draw draw)
                observedDraws.put(draw, observedAs);
        }
        for (Stmt stmt : statements) {
            // an import has already been applied by the front end
            if (!(stmt instanceof Stmt.Import)) eval(stmt);
        }
    }

    //*** §3.1 evaluation helpers ***//

    private Object eval(AstNode node) {
        try {
            return node instanceof Expr e ? e.accept(this) : ((Stmt) node).accept(this);
        } catch (TileApplicationError e) {
            throw e;                                    // already attributed to an inner node
        } catch (RuntimeException e) {
            throw new TileApplicationError(node, "Creating the LPhy objects did not work.", e.getMessage());
        }
    }

    private Value<?> evalValue(Expr expr) {
        Object result = eval(expr);
        if (result instanceof Value<?> value) return value;
        throw new TileApplicationError(expr, "Expected a value here, but got a distribution.",
                "Draw from the distribution with `~` and use the variable instead.");
    }

    private GenerativeDistribution<?> evalDistribution(Expr expr) {
        Object result = eval(expr);
        if (result instanceof GenerativeDistribution<?> distribution) return distribution;
        throw new TileApplicationError(expr, "Expected a distribution here, but got a value.", null);
    }

    private Map<String, Value> evalArguments(Map<String, Expr> bound) {
        Map<String, Value> values = new LinkedHashMap<>();
        bound.forEach((name, expr) -> values.put(name, evalValue(expr)));
        return values;
    }

    //*** §3.2 statements ***//

    private boolean isConverted(String name) {
        return dict.hasValue(name, GraphicalModel.Context.model) || dict.hasValue(name, GraphicalModel.Context.data);
    }

    @Override
    public Void visitDraw(Stmt.Draw stmt) {
        if (isConverted(stmt.name)) return null;
        Stmt.ObservedAs observedAs = observedDraws.get(stmt);
        if (observedAs != null) return visitObservedAsStmt(observedAs);   // never create it unobserved

        GenerativeDistribution<?> distribution = evalDistribution(stmt.expression);
        RandomVariable<?> variable = distribution.sample(stmt.name);
        dict.put(stmt.name, variable, GraphicalModel.Context.model);
        return null;
    }

    @Override
    public Void visitAssignment(Stmt.Assignment stmt) {
        if (isConverted(stmt.name)) return null;
        Value<?> value = evalValue(stmt.expression);
        if (!value.isAnonymous())
            throw new TileApplicationError(stmt, "Aliasing a variable is not supported.",
                    "Use `" + value.getId() + "` directly instead of `" + stmt.name + "`.");
        value.setId(stmt.name);
        dict.put(stmt.name, value, GraphicalModel.Context.model);
        return null;
    }

    /** LPhy's data clamping: the observed value goes in the data dictionary, its random variable in the model. */
    @Override
    public Void visitObservedAsStmt(Stmt.ObservedAs stmt) {
        if (!(stmt.stmt instanceof Stmt.Draw draw)) throw unsupported(stmt);
        if (isConverted(draw.name)) return null;

        GenerativeDistribution<?> distribution = evalDistribution(draw.expression);
        Value<?> observed = evalValue(stmt.observedAs);
        if (!observed.isAnonymous())
            throw new TileApplicationError(stmt.observedAs, "Observe an expression, not a variable.",
                    "Write the expression that defines `" + observed.getId() + "` after `observed as`.");
        observed.setId(draw.name);

        Object value = observed.value();
        RandomVariable<?> variable;
        if (distribution instanceof VectorizedDistribution<?> vectorized && value.getClass().isArray()) {
            variable = ObservationUtils.setObservationsToVectorizedRandomVariable((Object[]) value, draw.name, vectorized);
        } else if (distribution instanceof IID<?> iid && value.getClass().isArray()) {
            variable = ObservationUtils.setObservationsToVectorizedRandomVariable((Object[]) value, draw.name, iid);
        } else {
            variable = new RandomVariable(draw.name, value, distribution);
            variable.setObserved(true);
        }
        dict.put(draw.name, observed, GraphicalModel.Context.data);
        dict.put(draw.name, variable, GraphicalModel.Context.model);
        return null;
    }

    //*** §3.3 variables ***//

    private Value<?> lookup(String name) {
        Value<?> value = dict.getValue(name, GraphicalModel.Context.model);
        return value != null ? value : dict.getValue(name, GraphicalModel.Context.data);
    }

    @Override
    public Object visitVariable(Expr.Variable expr) {
        Value<?> value = lookup(expr.variableName);
        if (value != null) return value;

        Stmt definition = variableResolver.resolveVariable(expr);
        if (definition == null) throw unsupported(expr);   // an index variable (for i in ...)
        if (definition instanceof Stmt.Draw draw && observedDraws.containsKey(draw))
            definition = observedDraws.get(draw);

        if (!inProgress.add(definition))
            throw new TileApplicationError(expr, "`" + expr.variableName + "` is defined in terms of itself.", null);
        try {
            eval(definition);
        } finally {
            inProgress.remove(definition);
        }
        value = lookup(expr.variableName);
        if (value == null)
            throw new TileApplicationError(expr, "`" + expr.variableName + "` was not created.", null);
        return value;
    }

    //*** §3.4 literals ***//

    @Override
    public Object visitLiteral(Expr.Literal expr) {
        if (expr.unit != Unit.IMPLICIT) throw unsupported(expr);
        return ValueCreator.createValue(expr.value, null);
    }

    //*** §3.5 calls ***//

    @Override
    public Object visitCall(Expr.Call call) {
        String name = call.functionName;
        if ("IID".equals(name)) {
            Map<String, Expr> bound = bindArguments(call);
            return iid(call, bound.get("base"), bound.get("num"));
        }
        // LPhy has no single-taxon generator: taxon(...) is only valid inside an array of taxa (§3.9)
        if (TAXON.equals(name)) throw unsupported(call);

        IIDShorthand shorthand = names.iidShorthand(name);
        if (shorthand != null) return iidShorthand(call, shorthand);

        Function<?, ?> mathFunction = names.mathFunction(name);
        if (mathFunction != null) return mathFunction(call, mathFunction);

        MethodCallEntry methodCall = names.methodCall(name);
        if (methodCall != null) return methodCall(call, methodCall);

        if (!names.generators(name).isEmpty())
            return construct(call, name, evalArguments(bindArguments(call)));

        throw unsupported(call);   // a PhyloSpec generator with no LPhy counterpart yet
    }

    /** Binds the call's arguments against PhyloSpec's own overloads, which may omit argument names. */
    private Map<String, Expr> bindArguments(Expr.Call call) {
        List<org.phylospec.components.Generator> overloads = componentResolver.resolveGenerator(call.functionName);
        for (org.phylospec.components.Generator overload : overloads == null
                ? List.<org.phylospec.components.Generator>of() : overloads) {
            List<Expr.Call.Parameter> params = overload.getArguments().stream()
                    .map(a -> new Expr.Call.Parameter(a.getName(), Boolean.TRUE.equals(a.getRequired())))
                    .toList();
            try {
                Map<String, Expr> bound = new LinkedHashMap<>();
                call.resolveArgumentNames(params).forEach((name, arg) -> bound.put(name, unwrap(arg)));
                return bound;
            } catch (ArgumentResolutionError e) {
                // try the next overload
            }
        }
        throw new TileApplicationError(call, "No overload of " + call.functionName + " matches", null);
    }

    private Expr unwrap(Expr.Argument argument) {
        if (argument instanceof Expr.AssignedArgument assigned) return assigned.expression;
        throw unsupported(argument);
    }

    //*** §3.6 generator calls ***//

    private record Match(Generator generator, Map<String, Value> arguments) {}

    /** Builds a generator the way LPhy's parser does for a hand-written call, via {@link ParserUtils}. */
    private Object construct(AstNode node, String phylospecName, Map<String, Value> arguments) {
        List<Match> matches = new ArrayList<>();
        List<String> tried = new ArrayList<>();
        for (LPhyGenerator g : names.generators(phylospecName)) {
            Map<String, Value> renamed = rename(arguments, g);
            if (renamed == null) continue;   // no constructor takes one of the arguments
            tried.add(g.lphyName());
            // ParserUtils canonicalises deprecated keys in place, so give each call its own map
            List<Generator> found = g.kind() == PhyloSpecNames.Kind.DISTRIBUTION
                    ? ParserUtils.getMatchingGenerativeDistributions(g.lphyName(), new LinkedHashMap<>(renamed))
                    : ParserUtils.getMatchingFunctions(g.lphyName(), new LinkedHashMap<>(renamed));
            for (Generator generator : found) {
                if (generator != null && acceptsTypes(generator, renamed))
                    matches.add(new Match(generator, renamed));
            }
        }

        if (matches.isEmpty())
            throw new TileApplicationError(node, "No LPhy generator matches " + phylospecName + arguments.keySet(),
                    "Tried LPhy generators " + tried + ".");
        if (matches.size() > 1)
            LoggerUtils.log.warning("Found " + matches.size() + " matches for " + phylospecName + ". Picking first one!");

        Match match = matches.getFirst();
        // must be done so that Values all know their outputs
        match.arguments().forEach((lphyName, value) -> match.generator().setInput(lphyName, value));
        if (match.generator() instanceof GenerativeDistribution<?> distribution)
            return distribution;   // the statement samples it (§3.2)
        return match.generator().generate();
    }

    // PhyloSpec argument names -> LPhy ones; null if g has no constructor taking one of them
    private static Map<String, Value> rename(Map<String, Value> arguments, LPhyGenerator g) {
        Map<String, Value> renamed = new LinkedHashMap<>();
        for (Map.Entry<String, Value> entry : arguments.entrySet()) {
            String lphyName = IID.REPLICATES_PARAM_NAME.equals(entry.getKey())
                    ? entry.getKey() : g.argumentRename().get(entry.getKey());
            if (lphyName == null) return null;
            renamed.put(lphyName, entry.getValue());
        }
        return renamed;
    }

    // ParserUtils matches names only: drop a direct match whose parameter types reject the values
    private static boolean acceptsTypes(Generator generator, Map<String, Value> arguments) {
        if (generator instanceof IID || generator instanceof VectorizedDistribution
                || generator instanceof VectorizedFunction)
            return true;   // ParserUtils has already type-checked these against the component type
        for (Constructor<?> constructor : generator.getClass().getConstructors()) {
            List<Argument> parameters = ArgumentUtils.getArguments(constructor);
            boolean accepts = arguments.entrySet().stream().allMatch(entry -> parameters.stream().anyMatch(p ->
                    p.name.equals(entry.getKey()) && (entry.getValue().value() == null
                            || p.type.isAssignableFrom(entry.getValue().value().getClass()))));
            if (accepts) return true;
        }
        return false;
    }

    /** IID(base, num): LPhy writes it as a replicates argument on the base distribution's own call. */
    private Object iid(Expr.Call call, Expr base, Expr num) {
        if (!(base instanceof Expr.Call baseCall))
            throw new TileApplicationError(base, "IID base has no LPhy equivalent.", "Use a distribution call as the base.");
        Map<String, Value> arguments = evalArguments(bindArguments(baseCall));
        arguments.put(IID.REPLICATES_PARAM_NAME, evalValue(num));
        return construct(call, baseCall.functionName, arguments);
    }

    /** e.g. DiscreteGammaInv(shape, numCategories, numSites) -> IID(DiscreteGamma(shape, numCategories), numSites). */
    private Object iidShorthand(Expr.Call call, IIDShorthand shorthand) {
        Map<String, Expr> bound = bindArguments(call);
        shorthand.defaultOnlyArguments().forEach((argument, defaultValue) -> {
            Expr expr = bound.remove(argument);
            if (expr != null && !isLiteral(expr, defaultValue))
                throw new TileApplicationError(expr, NOT_SUPPORTED,
                        "LPhy only supports " + argument + " = " + defaultValue + " here.");
        });
        Expr num = bound.remove(shorthand.replicatesArgument());
        Map<String, Value> arguments = evalArguments(bound);
        arguments.put(IID.REPLICATES_PARAM_NAME, evalValue(num));
        return construct(call, shorthand.baseName(), arguments);
    }

    private static boolean isLiteral(Expr expr, Object value) {
        if (!(expr instanceof Expr.Literal literal)) return false;
        if (literal.value instanceof Number n && value instanceof Number v)
            return n.doubleValue() == v.doubleValue();
        return Objects.equals(literal.value, value);
    }

    //*** §3.7 operators and math functions ***//

    // The expression text is what CanonicalCodeBuilder prints, so it must be LPhy syntax. AstPrinter
    // renders S-expressions such as `(> s 1.0)`, so the text is built from the evaluated operands.
    @Override
    public Object visitBinary(Expr.Binary expr) {
        BiFunction<?, ?, ?> factory = names.binaryOperator(expr.operator);
        if (factory == null) throw unsupported(expr);
        Value<?> left = evalValue(expr.left);
        Value<?> right = evalValue(expr.right);
        String text = operandText(left) + " " + TokenType.getLexeme(expr.operator) + " " + operandText(right);
        return new ExpressionNode2Args(text, factory, left, right).apply();
    }

    @Override
    public Object visitUnary(Expr.Unary expr) {
        Function<?, ?> factory = names.unaryOperator(expr.operator);
        if (factory == null) throw unsupported(expr);
        Value<?> operand = evalValue(expr.right);
        return new ExpressionNode1Arg(TokenType.getLexeme(expr.operator) + operandText(operand), factory, operand).apply();
    }

    private Object mathFunction(Expr.Call call, Function<?, ?> factory) {
        Map<String, Expr> bound = bindArguments(call);
        if (bound.size() != 1) throw unsupported(call);   // e.g. log(x, base): LPhy's log is natural only
        Value<?> operand = evalValue(bound.values().iterator().next());
        String text = call.functionName + "(" + operandText(operand) + ")";
        return new ExpressionNode1Arg(text, factory, operand).apply();
    }

    // a named value prints its id; anything else its LPhy code, in brackets if it is a binary operation
    private static String operandText(Value<?> value) {
        if (!value.isAnonymous()) return value.getId();
        String code = value.codeString();
        return value.getGenerator() instanceof ExpressionNode2Args ? "(" + code + ")" : code;
    }

    //*** §3.8 method calls ***//

    private Object methodCall(Expr.Call call, MethodCallEntry entry) {
        Map<String, Expr> bound = bindArguments(call);
        Value<?> receiver = evalValue(bound.get(entry.receiverArgument()));
        Value<?>[] arguments = entry.otherArguments().stream()
                .map(name -> evalValue(bound.get(name))).toArray(Value<?>[]::new);
        try {
            return new MethodCall(entry.lphyMethod(), receiver, arguments).apply();
        } catch (NoSuchMethodException e) {
            throw new TileApplicationError(call, "LPhy has no method " + entry.lphyMethod() + " on "
                    + receiver.value().getClass().getSimpleName() + ".", null);
        }
    }

    //*** §3.9 arrays ***//

    @Override
    public Object visitArray(Expr.Array expr) {
        long taxonCalls = expr.elements.stream().filter(PhyloSpecToLPhy::isTaxonCall).count();
        if (taxonCalls > 0) {
            if (taxonCalls < expr.elements.size()) throw unsupported(expr);
            return taxa(expr);
        }
        Value[] values = expr.elements.stream().map(this::evalValue).toArray(Value[]::new);
        return ArrayCreator.createArrayValue(values);
    }

    private static boolean isTaxonCall(Expr expr) {
        return expr instanceof Expr.Call call && TAXON.equals(call.functionName);
    }

    /** [taxon(name=..., species=..., age=...), ...] -> taxa(names=[...], species=[...], ages=[...]). */
    private Object taxa(Expr.Array expr) {
        List<Map<String, Expr>> taxonArguments = expr.elements.stream()
                .map(e -> bindArguments((Expr.Call) e)).toList();

        Map<String, Value> arguments = new LinkedHashMap<>();
        arguments.put("names", column(taxonArguments, "name", null));

        long withSpecies = taxonArguments.stream().filter(a -> a.containsKey("species")).count();
        if (withSpecies == taxonArguments.size()) {
            arguments.put("species", column(taxonArguments, "species", null));
        } else if (withSpecies > 0) {
            throw new TileApplicationError(expr, NOT_SUPPORTED,
                    "LPhy needs a species for every taxon, or for none of them.");
        }

        if (taxonArguments.stream().anyMatch(a -> a.containsKey("age")))
            arguments.put("ages", column(taxonArguments, "age", 0.0));   // PhyloSpec's default age

        // only CreateTaxa, among LPhy's `taxa` generators, takes `names`
        return construct(expr, TAXA, arguments);
    }

    // one argument of every taxon, as an LPhy array; a missing value takes the default
    private Value<?> column(List<Map<String, Expr>> taxonArguments, String argument, Double defaultValue) {
        Value[] values = new Value[taxonArguments.size()];
        for (int i = 0; i < values.length; i++) {
            Expr expr = taxonArguments.get(i).get(argument);
            if (expr == null) {
                values[i] = ValueCreator.createValue(defaultValue, null);
            } else if (defaultValue != null && expr instanceof Expr.Literal literal && literal.value instanceof Number n) {
                values[i] = ValueCreator.createValue(n.doubleValue(), null);   // ages are Double[] in LPhy
            } else {
                values[i] = evalValue(expr);
            }
        }
        return ArrayCreator.createArrayValue(values);
    }

    //*** §3.10 unsupported forms ***//

    private TileApplicationError unsupported(AstNode node) {
        String text = node instanceof Expr e ? e.accept(printer) : node.getClass().getSimpleName();
        return new TileApplicationError(node, NOT_SUPPORTED, "`" + text + "` has no LPhy equivalent yet.");
    }

    @Override
    public Object visitGrouping(Expr.Grouping expr) {
        return eval(expr.expression);   // RemoveGroupings normally removes these first
    }

    @Override
    public Void visitDecoratedStmt(Stmt.Decorated stmt) {
        throw unsupported(stmt);
    }

    @Override
    public Void visitImport(Stmt.Import stmt) {
        throw unsupported(stmt);
    }

    @Override
    public Void visitIndexedStmt(Stmt.Indexed indexed) {
        throw unsupported(indexed);
    }

    @Override
    public Void visitObservedBetweenStmt(Stmt.ObservedBetween observedBetween) {
        throw unsupported(observedBetween);
    }

    @Override
    public Object visitStringTemplate(Expr.StringTemplate expr) {
        throw unsupported(expr);
    }

    @Override
    public Object visitTemplateVariable(Expr.TemplateVariable expr) {
        throw unsupported(expr);
    }

    @Override
    public Object visitOptionalTemplateVariable(Expr.OptionalTemplateVariable expr) {
        throw unsupported(expr);
    }

    @Override
    public Object visitAssignedArgument(Expr.AssignedArgument expr) {
        throw unsupported(expr);   // arguments are unwrapped by bindArguments
    }

    @Override
    public Object visitDrawnArgument(Expr.DrawnArgument expr) {
        throw unsupported(expr);
    }

    @Override
    public Object visitIndex(Expr.Index expr) {
        throw unsupported(expr);
    }

    @Override
    public Object visitRange(Expr.Range range) {
        throw unsupported(range);
    }

    @Override
    public Void visitAtomicType(AstType.Atomic expr) {
        throw new UnsupportedOperationException("types are not converted");
    }

    @Override
    public Void visitGenericType(AstType.Generic expr) {
        throw new UnsupportedOperationException("types are not converted");
    }
}
