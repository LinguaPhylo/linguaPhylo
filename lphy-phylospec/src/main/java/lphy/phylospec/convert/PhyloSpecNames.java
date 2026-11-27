package lphy.phylospec.convert;

import lphy.core.model.GeneratorUtils;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.parser.function.ExpressionNode1Arg;
import lphy.core.parser.function.ExpressionNode2Args;
import lphy.core.spi.LoaderManager;
import org.phylospec.lexer.TokenType;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * The name index used by the converter ({@link PhyloSpecToLPhy}): maps PhyloSpec generator and argument
 * names to LPhy ones, read from the {@code phylospec} attributes of {@link GeneratorInfo} and
 * {@link ParameterInfo}, plus the hand-maintained method-call, math-function, operator and
 * IID-shorthand tables. See CLASS_DESIGN3.md §2.
 * <p>
 * Immutable; build it once with {@link #load()}, which fails loudly on any naming conflict (§2.3).
 * @author Walter Xie
 */
public final class PhyloSpecNames {

    public enum Kind { DISTRIBUTION, FUNCTION }

    /** One LPhy generator name grouped under a PhyloSpec name. */
    public record LPhyGenerator(String lphyName, Kind kind,
                                Map<String, String> argumentRename) {}   // PhyloSpec arg -> LPhy arg

    /** One curated method-call entry, e.g. rootAge(tree) -> tree.rootAge(). */
    public record MethodCallEntry(String lphyMethod, String receiverArgument,
                                  List<String> otherArguments) {}

    /** A PhyloSpec generator that is shorthand for IID(base(...), n), e.g.
     *  DiscreteGammaInv(shape, numCategories, numSites) -> IID(DiscreteGamma(shape, numCategories), numSites). */
    public record IIDShorthand(String baseName, String replicatesArgument,
                               Map<String, Object> defaultOnlyArguments) {}   // argument -> the only value allowed

    /**
     * Java method name -> LPhy script name, for the operators bound to a symbol rather than to a
     * function-call name matching the method (e.g. {@code a + b}, not {@code a.plus(b)}). The ~30 unary
     * math functions (abs, sqrt, log, ...) need no entry: their script name is the method name.
     * Mined from the operator switch in {@code lphy.core.parser.LPhyListenerImpl}.
     */
    public static final Map<String, String> EXPRESSION_OPERATOR_SCRIPT_NAMES = Map.ofEntries(
            Map.entry("not", "!"),
            Map.entry("plus", "+"), Map.entry("minus", "-"), Map.entry("times", "*"), Map.entry("divide", "/"),
            Map.entry("pow", "**"), Map.entry("mod", "%"),
            Map.entry("and", "&&"), Map.entry("or", "||"),
            Map.entry("le", "<="), Map.entry("less", "<"), Map.entry("ge", ">="), Map.entry("greater", ">"),
            Map.entry("ne", "!="), Map.entry("equals", "=="),
            Map.entry("bitwiseand", "&"), Map.entry("bitwiseor", "|")
    );

    /**
     * Curated PhyloSpec functions that are LPhy method calls (§2.2). Only entries whose PhyloSpec
     * arguments are exactly the receiver plus the method's own arguments, in order, qualify.
     */
    private static final Map<String, MethodCallEntry> METHOD_CALLS = Map.of(
            "rootAge", new MethodCallEntry("rootAge", "tree", List.of()),
            "numBranches", new MethodCallEntry("branchCount", "tree", List.of())
    );

    /** PhyloSpec generators that are an IID of another PhyloSpec generator (§2.2). */
    private static final Map<String, IIDShorthand> IID_SHORTHANDS = Map.of(
            "DiscreteGammaInv", new IIDShorthand("DiscreteGamma", "numSites",
                    Map.of("invariantProportion", 0))
    );

    /** PhyloSpec names handled by the converter itself, which must never resolve to a generator. */
    static final Set<String> RESERVED_NAMES = Set.of("IID", "taxon");

    private final Map<String, List<LPhyGenerator>> generators;
    private final Map<String, Function<?, ?>> mathFunctions;
    private final Map<TokenType, BiFunction<?, ?, ?>> binaryOperators;
    private final Map<TokenType, Function<?, ?>> unaryOperators;

    private PhyloSpecNames(Map<String, List<LPhyGenerator>> generators,
                           Map<String, Function<?, ?>> mathFunctions,
                           Map<TokenType, BiFunction<?, ?, ?>> binaryOperators,
                           Map<TokenType, Function<?, ?>> unaryOperators) {
        this.generators = generators;
        this.mathFunctions = mathFunctions;
        this.binaryOperators = binaryOperators;
        this.unaryOperators = unaryOperators;
    }

    /**
     * Builds the index from every generator class LPhy has loaded, and checks it.
     * @throws IllegalStateException on a naming conflict (§2.3)
     */
    public static PhyloSpecNames load() {
        return load(new ArrayList<>(LoaderManager.getAllGenerativeDistributionClasses()),
                new ArrayList<>(LoaderManager.getAllFunctionsClasses()));
    }

    /** Builds the index from the given classes only; used by {@link #load()} and by tests. */
    static PhyloSpecNames load(Collection<? extends Class<?>> distributionClasses,
                               Collection<? extends Class<?>> functionClasses) {
        // PhyloSpec name -> LPhy name -> generator, in a stable order
        Map<String, Map<String, LPhyGenerator>> byPhyloSpecName = new TreeMap<>();
        // LPhy name -> the PhyloSpec name it resolved to, and the class that set it (check 1)
        Map<String, String> phylospecNameOfLPhyName = new HashMap<>();
        Map<String, Class<?>> firstClassOfLPhyName = new HashMap<>();

        for (Kind kind : Kind.values()) {
            Collection<? extends Class<?>> classes =
                    kind == Kind.DISTRIBUTION ? distributionClasses : functionClasses;
            for (Class<?> c : classes) {
                String lphyName = GeneratorUtils.getGeneratorName(c);
                if (lphyName == null) continue;
                String phylospecName = phylospecName(c);

                // check 1: one PhyloSpec name per LPhy name
                String previous = phylospecNameOfLPhyName.putIfAbsent(lphyName, phylospecName);
                if (previous != null && !previous.equals(phylospecName))
                    throw new IllegalStateException("LPhy generator '" + lphyName + "' resolves to two PhyloSpec names: '"
                            + previous + "' (" + firstClassOfLPhyName.get(lphyName).getName() + ") and '"
                            + phylospecName + "' (" + c.getName() + ")");
                firstClassOfLPhyName.putIfAbsent(lphyName, c);

                LPhyGenerator generator = byPhyloSpecName
                        .computeIfAbsent(phylospecName, k -> new LinkedHashMap<>())
                        .computeIfAbsent(lphyName, k -> new LPhyGenerator(lphyName, kind, new TreeMap<>()));
                addArgumentRenames(generator, c);
            }
        }

        Map<String, List<LPhyGenerator>> generators = new TreeMap<>();
        byPhyloSpecName.forEach((name, byLPhyName) -> generators.put(name, byLPhyName.values().stream()
                .map(g -> new LPhyGenerator(g.lphyName(), g.kind(), Collections.unmodifiableMap(g.argumentRename())))
                .toList()));

        PhyloSpecNames names = new PhyloSpecNames(Collections.unmodifiableMap(generators),
                buildMathFunctions(), buildBinaryOperators(), buildUnaryOperators());
        names.checkTablesAreDisjoint();
        return names;
    }

    // check 2: within one LPhy name, a PhyloSpec argument maps to the same LPhy argument everywhere
    private static void addArgumentRenames(LPhyGenerator generator, Class<?> c) {
        for (Constructor<?> constructor : c.getConstructors()) {
            for (ParameterInfo parameter : GeneratorUtils.getParameterInfo(constructor)) {
                String phylospecArg = phylospecName(parameter);
                String previous = generator.argumentRename().putIfAbsent(phylospecArg, parameter.name());
                if (previous != null && !previous.equals(parameter.name()))
                    throw new IllegalStateException("PhyloSpec argument '" + phylospecArg + "' of LPhy generator '"
                            + generator.lphyName() + "' maps to two LPhy arguments: '" + previous + "' and '"
                            + parameter.name() + "' (in " + c.getName() + ")");
            }
        }
    }

    // check 3: a name is a generator, a math function, a method call or an IID shorthand, never two
    private void checkTablesAreDisjoint() {
        Map<String, String> tableOfName = new HashMap<>();
        Map<String, Set<String>> tables = new LinkedHashMap<>();
        tables.put("generator", generators.keySet());
        tables.put("math function", mathFunctions.keySet());
        tables.put("method call", METHOD_CALLS.keySet());
        tables.put("IID shorthand", IID_SHORTHANDS.keySet());
        tables.put("reserved name", RESERVED_NAMES);
        tables.forEach((table, tableNames) -> {
            for (String name : tableNames) {
                String previous = tableOfName.putIfAbsent(name, table);
                if (previous != null)
                    throw new IllegalStateException("PhyloSpec name '" + name + "' is both a " + previous
                            + " and a " + table);
            }
        });
    }

    //*** the naming rule: the PhyloSpec name of an LPhy generator or parameter ***//

    /** {@code @GeneratorInfo.phylospec()} if set, otherwise the LPhy generator name. */
    public static String phylospecName(Class<?> generatorClass) {
        GeneratorInfo info = GeneratorUtils.getGeneratorInfo(generatorClass);
        return (info == null || info.phylospec().isEmpty())
                ? GeneratorUtils.getGeneratorName(generatorClass) : info.phylospec();
    }

    /** {@code @ParameterInfo.phylospec()} if set, otherwise the LPhy parameter name. */
    public static String phylospecName(ParameterInfo parameter) {
        return parameter.phylospec().isEmpty() ? parameter.name() : parameter.phylospec();
    }

    //*** lookups ***//

    /** @return one entry per LPhy generator name under this PhyloSpec name; empty if none. */
    public List<LPhyGenerator> generators(String phylospecName) {
        return generators.getOrDefault(phylospecName, List.of());
    }

    /** @return LPhy's factory for this PhyloSpec math function (e.g. sqrt), or null if none. */
    public Function<?, ?> mathFunction(String phylospecName) {
        return mathFunctions.get(phylospecName);
    }

    /** @return the curated method call for this PhyloSpec function, or null if none. */
    public MethodCallEntry methodCall(String phylospecName) {
        return METHOD_CALLS.get(phylospecName);
    }

    /** @return the IID shorthand for this PhyloSpec generator, or null if none. */
    public IIDShorthand iidShorthand(String phylospecName) {
        return IID_SHORTHANDS.get(phylospecName);
    }

    /** @return LPhy's factory for this PhyloSpec binary operator, or null if LPhy has none. */
    public BiFunction<?, ?, ?> binaryOperator(TokenType operator) {
        return binaryOperators.get(operator);
    }

    /** @return LPhy's factory for this PhyloSpec unary operator, or null if LPhy has none. */
    public Function<?, ?> unaryOperator(TokenType operator) {
        return unaryOperators.get(operator);
    }

    //*** math-function and operator tables ***//

    // the public static Function factories on ExpressionNode1Arg named like a function call (sqrt(), exp(), ...)
    private static Map<String, Function<?, ?>> buildMathFunctions() {
        Map<String, Function<?, ?>> functions = new TreeMap<>();
        for (Method method : staticFactories(ExpressionNode1Arg.class, Function.class)) {
            if (!EXPRESSION_OPERATOR_SCRIPT_NAMES.containsKey(method.getName()))
                functions.put(method.getName(), (Function<?, ?>) invoke(method));
        }
        return Collections.unmodifiableMap(functions);
    }

    // a PhyloSpec operator token maps to the LPhy factory whose script name is the token's lexeme
    private static Map<TokenType, BiFunction<?, ?, ?>> buildBinaryOperators() {
        Map<TokenType, BiFunction<?, ?, ?>> operators = new EnumMap<>(TokenType.class);
        for (TokenType token : List.of(TokenType.PLUS, TokenType.MINUS, TokenType.STAR, TokenType.SLASH,
                TokenType.LESS, TokenType.LESS_EQUAL, TokenType.GREATER, TokenType.GREATER_EQUAL,
                TokenType.EQUAL_EQUAL, TokenType.BANG_EQUAL)) {
            Method method = operatorFactory(ExpressionNode2Args.class, BiFunction.class, TokenType.getLexeme(token));
            if (method != null) operators.put(token, (BiFunction<?, ?, ?>) invoke(method));
        }
        return Collections.unmodifiableMap(operators);
    }

    // LPhy has no unary minus factory, so only `!` maps
    private static Map<TokenType, Function<?, ?>> buildUnaryOperators() {
        Map<TokenType, Function<?, ?>> operators = new EnumMap<>(TokenType.class);
        for (TokenType token : List.of(TokenType.BANG, TokenType.MINUS)) {
            Method method = operatorFactory(ExpressionNode1Arg.class, Function.class, TokenType.getLexeme(token));
            if (method != null) operators.put(token, (Function<?, ?>) invoke(method));
        }
        return Collections.unmodifiableMap(operators);
    }

    private static Method operatorFactory(Class<?> wrapperClass, Class<?> factoryType, String scriptName) {
        for (Method method : staticFactories(wrapperClass, factoryType)) {
            if (scriptName.equals(EXPRESSION_OPERATOR_SCRIPT_NAMES.get(method.getName()))) return method;
        }
        return null;
    }

    private static List<Method> staticFactories(Class<?> wrapperClass, Class<?> factoryType) {
        List<Method> methods = new ArrayList<>();
        for (Method method : wrapperClass.getDeclaredMethods()) {
            if (Modifier.isStatic(method.getModifiers()) && Modifier.isPublic(method.getModifiers())
                    && method.getParameterCount() == 0 && method.getReturnType() == factoryType)
                methods.add(method);
        }
        methods.sort(Comparator.comparing(Method::getName));
        return methods;
    }

    private static Object invoke(Method factory) {
        try {
            return factory.invoke(null);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Cannot create LPhy operator " + factory, e);
        }
    }
}
