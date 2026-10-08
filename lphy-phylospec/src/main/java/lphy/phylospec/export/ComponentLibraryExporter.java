package lphy.phylospec.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lphy.core.model.BasicFunction;
import lphy.core.model.GenerativeDistribution;
import lphy.core.model.GeneratorUtils;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.MethodInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.model.annotation.TypeInfo;
import lphy.core.parser.argument.ArgumentValue;
import lphy.core.parser.function.ExpressionNode1Arg;
import lphy.core.parser.function.ExpressionNode2Args;
import lphy.core.spi.Extension;
import lphy.core.spi.LPhyExtension;
import lphy.core.spi.LoaderManager;
import lphy.phylospec.convert.PhyloSpecNames;
import org.phylospec.components.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Reflects over every LPhy generator ({@link GenerativeDistribution}, {@link BasicFunction})
 * and data type declared by LPhy core and the standard library, and writes them out as a
 * component library JSON file in the same format as phylospec-core-component-library.json.
 * <p>
 * Usage: {@code mvn -pl lphy-phylospec -am exec:exec -Dexporter.args=path/to/output.json}
 */
public class ComponentLibraryExporter {

    // relative to this module's own basedir (exec:java's working directory), not the repo root
    private static final String DEFAULT_OUTPUT =
            "src/main/resources/phylospec-lphy-component-library.json";

    // Written alongside DEFAULT_OUTPUT, in the same directory - see buildMethodCalls().
    private static final String METHOD_CALLS_OUTPUT_FILENAME = "lphy-method-calls.json";

    /**
     * The extensions this "LPhy" component library actually covers: core plus the standard
     * library, matching this module's own {@code pom.xml} dependency on {@code lphy-base} (which
     * pulls in {@code lphy}). Resolved explicitly by class name via
     * {@link lphy.core.spi.LPhyCoreLoader#getExtensionMap(List)} - the same pattern
     * {@code lphystudio.app.docgenerator.GenerateDocs} uses for its {@code BASIC_EXT_NAMES} -
     * rather than reading {@link LoaderManager}'s process-wide, {@code ServiceLoader}-merged
     * registry directly. That registry picks up <b>every</b> {@link Extension} visible on the
     * classpath: if this module ever gained an extra dependency that also registers one (a test
     * dependency, or a third-party extension pulled in transitively), the exported "LPhy" library
     * would silently and incorrectly include that extension's types/generators too, with nothing
     * in the code enforcing the "LPhy" scope the library's own {@code name} field claims.
     */
    private static final List<String> LPHY_EXTENSION_CLASS_NAMES =
            List.of("lphy.core.spi.LPhyCoreImpl", "lphy.base.spi.LPhyBaseImpl");

    public static void main(String[] args) throws IOException {
        // args[0] can be null (not just absent/empty) here: a self-closing <exporter.args/> pom
        // property with no override resolves to a null Maven property, which exec-maven-plugin
        // still passes through as one array element rather than omitting it.
        String outputPath = (args.length > 0 && args[0] != null && !args[0].isBlank())
                ? args[0] : DEFAULT_OUTPUT;

        Map<String, Extension> extensionMap = LoaderManager.getLphyCoreLoader()
                .getExtensionMap(LPHY_EXTENSION_CLASS_NAMES);
        if (extensionMap.isEmpty()) {
            throw new IllegalStateException("Cannot find the extensions " + LPHY_EXTENSION_CLASS_NAMES
                    + " on the classpath - is lphy-phylospec missing its lphy-base dependency?");
        }
        List<LPhyExtension> extensions = new ArrayList<>();
        for (Extension extension : extensionMap.values()) {
            if (extension instanceof LPhyExtension lPhyExtension) extensions.add(lPhyExtension);
        }

        ComponentLibrary library = new ComponentLibrary();
        library.setName("LPhy");
        library.setVersion("0.1.0");
        library.setDescription("Generators and data types available in LinguaPhylo (LPhy), exported for PhyloSpec.");
        library.setAuthors(List.of("LinguaPhylo team"));
        library.setLicense("LGPL-3.0");
        library.setTypes(buildTypes(extensions));
        List<Generator> generators = buildGenerators(extensions);
        generators.addAll(buildExpressionOperatorGenerators());
        generators.sort(Comparator.comparing(Generator::getName).thenComparing(Generator::getNamespace));
        library.setGenerators(generators);

        ComponentLibrarySchema schema = new ComponentLibrarySchema();
        schema.setComponentLibrary(library);

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        File outFile = new File(outputPath);
        File parent = outFile.getParentFile();
        if (parent != null) parent.mkdirs();
        mapper.writeValue(outFile, schema);

        System.out.println("Wrote " + library.getTypes().size() + " types and "
                + library.getGenerators().size() + " generators to " + outFile.getAbsolutePath());

        List<Map<String, Object>> methodCalls = buildMethodCalls();
        Map<String, Object> methodCallLibrary = new LinkedHashMap<>();
        methodCallLibrary.put("name", "LPhy method calls");
        methodCallLibrary.put("description",
                "LPhy's method call: an instance method invoked via dot-call syntax "
                        + "(e.g. tree.rootAge()), exposed to scripts wherever a public method carries "
                        + "@MethodInfo (lphy.core.model.annotation.MethodInfo). Not a third generator kind "
                        + "alongside GenerativeDistribution/DeterministicFunction above - MethodCall "
                        + "(lphy.core.parser.function.MethodCall) itself extends DeterministicFunction; it is "
                        + "LPhy's own special-case deterministic function, built dynamically over an "
                        + "arbitrary annotated method via reflection rather than declared as its own "
                        + "constructor-based generator class. PhyloSpec has no equivalent construct (no "
                        + "dot-call syntax at all), so this is its own file rather than part of the "
                        + "PhyloSpec-schema-shaped component library above.");
        methodCallLibrary.put("methodCalls", methodCalls);

        File methodCallsFile = new File(parent != null ? parent : new File("."), METHOD_CALLS_OUTPUT_FILENAME);
        mapper.writeValue(methodCallsFile, methodCallLibrary);
        System.out.println("Wrote " + methodCalls.size() + " method-call signatures to "
                + methodCallsFile.getAbsolutePath());
    }

    /**
     * One entry per distinct return type across every exported generator: the {@code T} inside a
     * {@code DeterministicFunction<T>}'s {@code apply()}, which returns {@code Value<T>} (see
     * {@code MapFunction}), or a {@code GenerativeDistribution<T>}'s {@code sample()}, which
     * returns {@code RandomVariable<T>} (see {@code lphy.base.distribution.Normal}) - resolved via
     * {@link GeneratorUtils#getReturnType}, the exact same call {@link #buildGenerators(Class,
     * boolean)} makes to derive each generator's own {@code generatedType}, so the two stay
     * self-consistent by construction. Iterates the same {@code extension.getDistributions()} /
     * {@code extension.getFunctions()} class lists {@link #buildGenerators(List)} does, rather than
     * delegating to {@link LPhyExtension#getTypes()}: that pre-existing set is scoped for a
     * different consumer (lphy-studio's type browser) and mixes in constructor *parameter* types
     * too (via {@code NarrativeUtils.getParameterTypes}) - a type that only ever appears as an
     * argument, never as something a generator actually produces, doesn't belong in a catalog of
     * LPhy's data types (e.g. {@code NChar} is only ever a parameter to {@code nchar()}; no
     * generator returns it).
     * <p>
     * Deduped by {@code Class::getSimpleName()}, one entry per distinct name (first one seen wins) -
     * this part mirrors {@code lphystudio.app.docgenerator.GenerateDocs}'s own type-collection logic
     * (its "Types" section of {@code generateMarkdown()}, one {@code docs/types/<name>.md} page per
     * distinct registered {@code Class}). LPhy does not have a "Vector" data type: vectorisation
     * ({@code lphy.core.vectorization} - the {@code replicates} argument, or passing a vector of
     * elements where a generator expects a scalar) is a language *mechanism* that produces a genuine
     * array value - e.g. vectorising a {@code Double} generator gives the real LPhy data type
     * {@code Double[]}. The array class itself (e.g. {@code Double[].class}) - not some generic
     * container - <i>is</i> the LPhy data type, exactly as {@code GenerateDocs} already treats it.
     * <p>
     * {@code namespace} is the type's own Java package (e.g. {@code java.util} for {@code Map},
     * {@code lphy.base.evolution.tree} for {@code TimeTree}) rather than the blanket constant
     * {@code "lphy.types"} this used to emit for every entry regardless of origin - this also
     * correctly reflects that a JDK class like {@code java.util.Map} isn't "an LPhy type" the way
     * {@code TimeTree} is, it's just the class LPhy happens to represent a map value with.
     * <p>
     * Almost every entry here is a concrete, fully-instantiated class either way - true even for
     * the JDK scalar classes LPhy uses directly as its own value types ({@code Double}, {@code
     * String}, {@code Boolean}, {@code Object}: none of these declare their own generic type
     * parameters). The one exception is a genuinely generic JDK container like {@code java.util.Map}
     * (declares {@code <K, V>} on the class itself) reaching here as the raw-erased {@code T} of some
     * generator's {@code Value<T>} (see {@link GeneratorUtils#getClass}, e.g. {@code MapFunction}'s
     * {@code Value<Map<String, Object>>}) - so {@code typeParameters} is populated reflectively from
     * {@code Class::getTypeParameters()} rather than hardcoded {@code null}, exactly mirroring how
     * PhyloSpec's own {@code Map} type declares {@code typeParameters: ["K", "V"]}.
     */
    private static List<Type> buildTypes(List<LPhyExtension> extensions) {
        TreeSet<Class<?>> returnTypes = new TreeSet<>(Comparator.comparing(Class::getName));
        for (LPhyExtension extension : extensions) {
            for (Class<GenerativeDistribution> c : LoaderManager.getAllClassesOfType(
                    extension.getDistributions(), GenerativeDistribution.class)) {
                returnTypes.add(GeneratorUtils.getReturnType(c));
            }
            for (Class<BasicFunction> c : LoaderManager.getAllClassesOfType(
                    extension.getFunctions(), BasicFunction.class)) {
                returnTypes.add(GeneratorUtils.getReturnType(c));
            }
        }
        returnTypes.addAll(expressionOperatorReturnTypes());

        Map<String, Type> typesByName = new LinkedHashMap<>();
        for (Class<?> c : returnTypes) {
            String name = c.getSimpleName();
            if (typesByName.containsKey(name)) continue;
            Type type = new Type();
            type.setName(name);
            type.setNamespace(c.getPackageName());
            // e.g. BModelSet's @TypeInfo(description = "The selected model set for bModelTest.").
            // Not every LPhy type class carries one (only about half of them do today) - falls
            // back to "" same as before when absent, rather than leaving this unpopulated only
            // for classes lphy-base happens to annotate.
            TypeInfo typeInfo = c.getAnnotation(TypeInfo.class);
            type.setDescription(typeInfo != null ? typeInfo.description() : "");
            java.lang.reflect.TypeVariable<?>[] classTypeParams = c.getTypeParameters();
            type.setTypeParameters(classTypeParams.length == 0 ? null
                    : Arrays.stream(classTypeParams)
                            .map(java.lang.reflect.TypeVariable::getName)
                            .collect(java.util.stream.Collectors.toList()));
            // Omit (Type is NON_NULL) rather than emit an empty list.
            type.setTypeProperties(null);
            typesByName.put(name, type);
        }
        return new ArrayList<>(typesByName.values());
    }

    /**
     * LPhy's "easy" operators - ~30 unary math functions (abs, sqrt, log, ...) and 16 binary
     * operators (+, -, *, /, <=, ==, ...) plus unary `!` - are architecturally invisible to Rule 1
     * (the {@code apply()}/{@code sample()} + {@code declareFunctions()} machinery above): each one
     * is a {@code public static Function}/{@code BiFunction} factory method on one of these two
     * generic wrapper classes (see their own javadoc), not a dedicated class with its own
     * {@code apply()}. There's no {@code @GeneratorInfo} per operator either - the sole
     * {@code @GeneratorInfo} present, on {@code ExpressionNode2Args#getParams()}, is a structural
     * placeholder ({@code name="expression"}) for the whole wrapper, not a real operator name. The
     * only place the operator-name <-> implementation binding exists at all is a hardcoded
     * {@code switch} in the hand-written parser listener, {@code lphy.core.parser.LPhyListenerImpl}.
     */
    private static final List<Class<?>> EXPRESSION_NODE_WRAPPER_CLASSES =
            List.of(ExpressionNode1Arg.class, ExpressionNode2Args.class);

    /**
     * Every public static {@code Function}/{@code BiFunction} factory method across both wrapper
     * classes - the reflectable source of truth for "what operators exist and what are their
     * arg/return types", even though *names* need {@link PhyloSpecNames#EXPRESSION_OPERATOR_SCRIPT_NAMES} for
     * the symbol-bound half of them.
     */
    private static List<Method> expressionOperatorMethods() {
        List<Method> methods = new ArrayList<>();
        for (Class<?> wrapperClass : EXPRESSION_NODE_WRAPPER_CLASSES) {
            for (Method method : wrapperClass.getDeclaredMethods()) {
                if (!Modifier.isStatic(method.getModifiers()) || !Modifier.isPublic(method.getModifiers())) continue;
                if (!(method.getGenericReturnType() instanceof ParameterizedType pt)) continue;
                Class<?> rawType = (Class<?>) pt.getRawType();
                if (rawType == Function.class || rawType == BiFunction.class) {
                    methods.add(method);
                }
            }
        }
        return methods;
    }

    // Every Function<A,R>/BiFunction<A,B,R> across both wrapper classes uses plain, non-nested
    // JDK classes (Number, Double, Integer, Boolean, Object - verified against every declaration)
    // - a direct cast suffices, no need for GeneratorUtils#getClass's fuller generic-unwrapping.
    private static Class<?> functionalTypeArgClass(java.lang.reflect.Type type) {
        return type instanceof Class<?> c ? c : Object.class;
    }

    private static Set<Class<?>> expressionOperatorReturnTypes() {
        Set<Class<?>> types = new LinkedHashSet<>();
        for (Method method : expressionOperatorMethods()) {
            ParameterizedType pt = (ParameterizedType) method.getGenericReturnType();
            java.lang.reflect.Type[] typeArgs = pt.getActualTypeArguments();
            types.add(functionalTypeArgClass(typeArgs[typeArgs.length - 1]));
        }
        return types;
    }

    private static List<Generator> buildExpressionOperatorGenerators() {
        List<Generator> generators = new ArrayList<>();
        for (Method method : expressionOperatorMethods()) {
            ParameterizedType pt = (ParameterizedType) method.getGenericReturnType();
            java.lang.reflect.Type[] typeArgs = pt.getActualTypeArguments();
            boolean binary = typeArgs.length == 3;
            Class<?> returnClass = functionalTypeArgClass(typeArgs[typeArgs.length - 1]);

            Generator generator = new Generator();
            generator.setName(PhyloSpecNames.EXPRESSION_OPERATOR_SCRIPT_NAMES.getOrDefault(method.getName(), method.getName()));
            generator.setDescription("");
            generator.setNamespace(method.getDeclaringClass().getPackageName());
            generator.setGeneratedType(returnClass.getSimpleName());
            // These ~48 operators all share just two implementing classes (unlike every other
            // generator, one class each) - flagged explicitly so a JSON consumer isn't left
            // wondering why e.g. abs/sqrt/log all report the identical namespace.
            generator.setAdditionalProperty("implementedVia", method.getDeclaringClass().getSimpleName());

            List<Argument> arguments = new ArrayList<>();
            String[] argNames = binary ? new String[]{"a", "b"} : new String[]{"x"};
            for (int i = 0; i < argNames.length; i++) {
                Argument argument = new Argument();
                argument.setName(argNames[i]);
                argument.setType(functionalTypeArgClass(typeArgs[i]).getSimpleName());
                argument.setRequired(true);
                argument.setDescription("");
                arguments.add(argument);
            }
            generator.setArguments(arguments);
            // Omit (Generator is NON_NULL) rather than emit an empty list.
            generator.setTypeParameters(null);
            generator.setExamples(null);
            generator.setConstraints(null);
            generators.add(generator);
        }
        return generators;
    }

    private static List<Generator> buildGenerators(List<LPhyExtension> extensions) {
        List<Generator> generators = new ArrayList<>();

        for (LPhyExtension extension : extensions) {
            for (Class<GenerativeDistribution> c : LoaderManager.getAllClassesOfType(
                    extension.getDistributions(), GenerativeDistribution.class)) {
                generators.addAll(buildGenerators(c, true));
            }
            for (Class<BasicFunction> c : LoaderManager.getAllClassesOfType(
                    extension.getFunctions(), BasicFunction.class)) {
                generators.addAll(buildGenerators(c, false));
            }
        }
        // Iterating extension-by-extension (rather than one merged, name-sorted dictionary as
        // before) means insertion order now depends on extension/map iteration order, not just
        // generator name - sort explicitly so the output stays alphabetized and deterministic
        // regardless of that.
        generators.sort(Comparator.comparing(Generator::getName).thenComparing(Generator::getNamespace));
        return generators;
    }

    /**
     * One entry per public constructor, so a generator with multiple constructor overloads
     * (e.g. Yule with/without a root age) becomes multiple entries sharing a name, matching
     * how the PhyloSpec component library models overloading.
     */
    private static List<Generator> buildGenerators(Class<?> c, boolean isDistribution) {
        GeneratorInfo info = GeneratorUtils.getGeneratorInfo(c);
        // `name` is always the LPhy generator name, as written in an LPhy script; the PhyloSpec
        // name, if different, goes only into the separate `phylospec` property below
        String name = GeneratorUtils.getGeneratorName(c);
        // The implementing class's own Java package (e.g. lphy.base.distribution for Normal,
        // lphy.core.parser.function for MapFunction) rather than a synthetic category-derived
        // string (the old "lphy.distributions.prior" scheme) - real, verifiable provenance instead
        // of a name only meaningful within this exporter.
        String namespace = c.getPackageName();
        String returnType = GeneratorUtils.getReturnType(c).getSimpleName();
        String generatedType = isDistribution ? "Distribution<" + returnType + ">" : returnType;

        List<Generator> generators = new ArrayList<>();
        for (Constructor<?> constructor : c.getConstructors()) {
            Generator generator = new Generator();
            generator.setName(name);
            generator.setDescription(info == null ? "" : info.description());
            generator.setNamespace(namespace);
            generator.setGeneratedType(generatedType);
            generator.setArguments(buildArguments(constructor));
            // Omit (Generator is NON_NULL) rather than emit an empty list.
            generator.setTypeParameters(null);
            // Omit (Generator is NON_NULL) rather than emit an empty list.
            generator.setExamples(null);
            // Omit (Generator is NON_NULL) rather than emit an empty list.
            generator.setConstraints(null);
            // explicit @GeneratorInfo.phylospec() annotation value, if the class declared one
            // (e.g. JukesCantor's apply() sets phylospec = "jc69") - kept alongside `name`
            // ("jukesCantor"), never folded into it: `name` is the LPhy name, `phylospec` the
            // PhyloSpec name.
            if (info != null && !info.phylospec().isEmpty()) {
                generator.setAdditionalProperty("phylospec", info.phylospec());
            }
            generators.add(generator);
        }
        return generators;
    }

    private static List<Argument> buildArguments(Constructor<?> constructor) {
        // MapFunction(ArgumentValue... argumentValues) is the one LPhy generator built entirely
        // around dynamically-named arguments (LPhy's map literal, e.g. `{a=1, b=2, c="three"}`,
        // desugars to exactly this constructor call) - there's no fixed parameter list to
        // reflect over (hence no @ParameterInfo annotations, and an empty `paramInfos` below),
        // and the PhyloSpec Argument schema itself has no "variadic, dynamically-named argument"
        // concept to map it onto. Represented as the closest fit within that schema: one
        // synthetic argument whose type spells out the name/value pairing directly as
        // Map<String, Object> (String because a name is always a bare identifier; Object because
        // a value can be anything, including another map, per `n = {d=1, e="two", f=m}` above).
        if (isDynamicMapVarargsConstructor(constructor)) {
            return List.of(buildDynamicMapArgument());
        }

        List<ParameterInfo> paramInfos = GeneratorUtils.getParameterInfo(constructor);
        java.lang.reflect.Type[] genericParamTypes = constructor.getGenericParameterTypes();

        List<Argument> arguments = new ArrayList<>();
        for (int i = 0; i < paramInfos.size(); i++) {
            ParameterInfo pInfo = paramInfos.get(i);
            Argument argument = new Argument();
            // the LPhy parameter name; the PhyloSpec name goes only into `phylospec` below
            argument.setName(pInfo.name());
            argument.setDescription(pInfo.description());
            argument.setRequired(!pInfo.optional());
            Class<?> paramType = i < genericParamTypes.length
                    ? GeneratorUtils.getClass(genericParamTypes[i]) : Object.class;
            argument.setType(paramType.getSimpleName());
            // explicit @ParameterInfo.phylospec() annotation value, if this parameter declared
            // one (e.g. HKY's freq parameter sets phylospec = "baseFrequencies", which differs
            // from its LPhy name "freq") - see note on Generator above.
            if (!pInfo.phylospec().isEmpty()) {
                argument.setAdditionalProperty("phylospec", pInfo.phylospec());
            }
            arguments.add(argument);
        }
        return arguments;
    }

    /**
     * True only for {@code MapFunction}'s {@code ArgumentValue... argumentValues} constructor
     * (currently the sole user of this pattern in LPhy): a single varargs parameter of
     * {@link ArgumentValue}, carrying an arbitrary, caller-chosen number of dynamically-named
     * name/value pairs rather than a fixed parameter list.
     */
    private static boolean isDynamicMapVarargsConstructor(Constructor<?> constructor) {
        return constructor.isVarArgs()
                && constructor.getParameterCount() == 1
                && constructor.getParameterTypes()[0].getComponentType() == ArgumentValue.class;
    }

    private static Argument buildDynamicMapArgument() {
        Argument argument = new Argument();
        // "*" rather than a fabricated identifier like "entries": no such name exists in the real
        // API - `map(a=1, b=2)` and `map(x=1, y=2)` are both valid calls to the same constructor,
        // so any single literal name here would misrepresent it as one fixed, named parameter.
        // "*" is the conventional "any key" placeholder (as in JSON Schema's own
        // patternProperties). Only fields the PhyloSpec Argument schema itself actually defines
        // are set here - no invented additionalProperties flag, since nothing in PhyloSpec would
        // read one.
        argument.setName("*");
        argument.setType("Map<String, Object>");
        argument.setRequired(false);
        argument.setDescription(
                "Any number of name=value pairs (e.g. {a=1, b=2, c=\"three\"}); each name becomes "
                        + "a String key and each value (of any type, including another map) becomes "
                        + "the corresponding value in the resulting map.");
        return argument;
    }

    /**
     * LPhy's third generator-<em>discovery</em> mechanism, alongside constructor-based generators
     * ({@link #buildGenerators}) and symbol-bound operators ({@link #buildExpressionOperatorGenerators}):
     * an instance method invoked via dot-call syntax on a value, e.g. {@code tree.rootAge()}. This is
     * not a third semantic kind alongside {@code GenerativeDistribution}/{@code DeterministicFunction}
     * (every method call, like every operator, is always deterministic - {@link MethodCall} itself
     * {@code extends DeterministicFunction}); it is one more way LPhy exposes a deterministic function
     * to scripts, alongside a dedicated constructor-based class and a symbol-bound operator method.
     * {@link MethodCall} (in {@code lphy-core}) is the runtime dispatcher: given a receiver value and a
     * method name, it resolves {@code c = value.value().getClass(); method = c.getMethod(methodName,
     * paramTypes)} - ordinary Java method resolution against the value's actual <em>runtime</em> class -
     * then requires the resolved {@link Method} to itself carry {@code @MethodInfo}, with no fallback to
     * an ancestor's annotation (see that class's own "TODO should we check super classes here?" -
     * there isn't one; ​an override that changes behavior without re-declaring {@code @MethodInfo}
     * is silently uncallable from an LPhy script, even though the same-named method still works
     * fine as ordinary Java on the un-overriding declaring class).
     * <p>
     * Unlike the other two discovery mechanisms, there is no existing registry of "every class that
     * might declare one of these" to iterate ({@link LPhyExtension} only tracks distribution/
     * function classes, not arbitrary value types like {@code TimeTree} or {@code Alignment}) - so
     * this scans the runtime classpath itself ({@link #findLphyClasses}) rather than relying on a
     * hand-maintained list, so a future {@code @MethodInfo} method anywhere under the {@code lphy.}
     * package prefix is picked up automatically the next time this exporter runs.
     * <p>
     * Grouped by (method name, return type, ordered argument types): from an LPhy script's point of
     * view, "the same method call" is exactly this signature, regardless of which class in a
     * hierarchy actually provides it for a given value - e.g. {@code Alignment}'s own default
     * {@code taxa()} and {@code AbstractAlignment}'s override of it collapse into one row listing
     * both declaring classes, rather than being reported as two unrelated methods. Scans
     * {@link Class#getDeclaredMethods()} (not {@code getMethods()}) on every candidate class: this
     * mirrors {@code MethodCall}'s own resolution exactly - only a class that directly carries its
     * own {@code @MethodInfo}-annotated declaration is credited with "implementing" the call, so an
     * inherited-but-not-overridden method is correctly attributed to its one real declaring class,
     * and a subclass whose override dropped the annotation is correctly left out (it can't actually
     * be called there).
     */
    private static List<Map<String, Object>> buildMethodCalls() {
        Map<String, Map<String, Object>> groups = new LinkedHashMap<>();

        for (Class<?> c : findLphyClasses()) {
            for (Method m : c.getDeclaredMethods()) {
                if (!Modifier.isPublic(m.getModifiers())) continue;
                MethodInfo info = m.getAnnotation(MethodInfo.class);
                if (info == null) continue;

                String returnType = m.getReturnType().getSimpleName();
                String[] paramTypes = Arrays.stream(m.getParameterTypes())
                        .map(Class::getSimpleName).toArray(String[]::new);
                // Signature, not just name: e.g. two unrelated one-arg methods that happen to
                // share a name but differ in return/argument type must stay separate rows.
                String key = m.getName() + "(" + String.join(",", paramTypes) + "):" + returnType;

                Map<String, Object> group = groups.computeIfAbsent(key, k -> {
                    Map<String, Object> g = new LinkedHashMap<>();
                    g.put("name", m.getName());
                    g.put("returnType", returnType);
                    List<Map<String, String>> args = new ArrayList<>();
                    for (String paramType : paramTypes) {
                        Map<String, String> arg = new LinkedHashMap<>();
                        // LPhy method-call arguments are always positional, never named - see
                        // MethodCall#argParamName ("arg" + i), which is the only name they ever
                        // get even internally. No @ParameterInfo-equivalent exists for them
                        // (unlike constructor arguments), so there's no real name to reflect.
                        arg.put("type", paramType);
                        args.add(arg);
                    }
                    g.put("arguments", args);
                    g.put("classes", new ArrayList<Map<String, String>>());
                    return g;
                });

                @SuppressWarnings("unchecked")
                List<Map<String, String>> classEntries = (List<Map<String, String>>) group.get("classes");
                Map<String, String> classEntry = new LinkedHashMap<>();
                classEntry.put("class", c.getSimpleName());
                classEntry.put("namespace", c.getPackageName());
                classEntry.put("description", info.description());
                classEntries.add(classEntry);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>(groups.values());
        result.sort(Comparator.comparing(g -> ((String) g.get("name")).toLowerCase()));
        return result;
    }

    /**
     * Every loadable class on the runtime classpath under the {@code lphy.} package prefix. Two
     * independent axes of "which classpath, exactly":
     * <p>
     * 1. <b>Where the roots come from.</b> {@link #classpathRootsFromProperty} (the
     * {@code java.class.path} system property) covers a real forked JVM - {@code exec:exec}, or
     * plain {@code java -cp}. It does <em>not</em> cover this module's own README-documented
     * {@code mvn exec:java}: that goal runs {@link #main} in-process under an isolated
     * {@link java.net.URLClassLoader} built from the resolved dependency graph, without ever
     * touching {@code java.class.path} - confirmed by running it: an earlier version of this scan
     * using only that property wrote 0 method-call signatures. {@link #classpathRootsFromClassLoaders}
     * covers that case instead, by reading {@code getURLs()} off every {@code URLClassLoader} found
     * walking up this class's own loader chain plus the current thread's context loader. Both are
     * always tried and merged (deduped): cheap, and safe against future exec-plugin version changes
     * shifting which one is actually in play.
     * <p>
     * 2. <b>What shape each root is.</b> Checked as both a directory of {@code .class} files and a
     * JAR of them (see {@link #collectClassesFromDirectory}/{@link #collectClassesFromJar}), because
     * {@code lphy}/{@code lphy-base}'s shape depends on how this module was invoked: run standalone
     * ({@code cd lphy-phylospec; mvn exec:java}), Maven resolves them as ordinary dependencies from
     * the local repository - JAR files; run as part of a combined multi-module reactor build
     * ({@code mvn -pl lphy-phylospec -am ...}), Maven substitutes each sibling module's own
     * {@code target/classes} output directory instead.
     * <p>
     * Both axes fail silently (empty result, not an error) if their assumption is wrong, which is
     * why every combination is handled rather than picking one. The exec-maven-plugin config in this
     * module's {@code pom.xml} runs {@link #main} in plain classpath mode (not module-path), so
     * there's no JPMS export/opens boundary to fight regardless. Dependency JARs unrelated to LPhy
     * (jackson, phylospec-core, ...) are cheap to open and skip: only entries under the
     * {@code lphy.} package prefix are ever loaded. Nested ({@code $}) classes are skipped: every
     * current {@code @MethodInfo} declaration lives on a top-level class (verified against the whole
     * source tree), and skipping them avoids the extra risk of loading synthetic/anonymous classes
     * never meant to be loaded standalone. A class that fails to load (e.g. depends on an optional
     * dependency not on this classpath) is skipped rather than failing the whole export - this is a
     * best-effort documentation scan, not a correctness-critical path.
     */
    private static List<Class<?>> findLphyClasses() {
        List<Class<?>> classes = new ArrayList<>();
        ClassLoader loader = ComponentLibraryExporter.class.getClassLoader();
        Set<File> roots = new LinkedHashSet<>();
        for (File root : classpathRootsFromProperty()) roots.add(root);
        for (File root : classpathRootsFromClassLoaders()) roots.add(root);
        for (File root : roots) {
            if (root.isDirectory()) {
                collectClassesFromDirectory(root, root, loader, classes);
            } else if (root.isFile() && root.getName().endsWith(".jar")) {
                collectClassesFromJar(root, loader, classes);
            }
        }
        return classes;
    }

    // Covers `exec:exec` (forks a real `java -cp ...` process) and any plain `java -cp` invocation:
    // both set this system property to the real launch classpath.
    private static List<File> classpathRootsFromProperty() {
        String classpath = System.getProperty("java.class.path");
        if (classpath == null) return List.of();
        List<File> roots = new ArrayList<>();
        for (String entry : classpath.split(File.pathSeparator)) {
            roots.add(new File(entry));
        }
        return roots;
    }

    // Covers `exec:java` (the default `mvn exec:java`, and this module's own README-documented
    // invocation): it runs the main class in-process under an isolated URLClassLoader built from
    // the resolved dependency graph, WITHOUT touching `java.class.path` - so that property alone
    // silently misses lphy/lphy-base entirely in that mode (verified: an earlier version of this
    // scan using only java.class.path wrote 0 method-call signatures under a real `mvn exec:java`
    // run). Walks up the classloader delegation chain collecting every URLClassLoader's URLs, since
    // exec:java's isolated loader (or a plugin-version-dependent ancestor of it) is where the real
    // dependency classpath actually lives.
    private static List<File> classpathRootsFromClassLoaders() {
        List<File> roots = new ArrayList<>();
        Set<ClassLoader> seen = new LinkedHashSet<>();
        for (ClassLoader l = ComponentLibraryExporter.class.getClassLoader(); l != null; l = l.getParent()) {
            if (!seen.add(l)) break; // cycle guard, shouldn't happen but no reason to trust it won't
            if (l instanceof java.net.URLClassLoader urlLoader) {
                for (java.net.URL url : urlLoader.getURLs()) {
                    try {
                        roots.add(new File(url.toURI()));
                    } catch (java.net.URISyntaxException ignored) {
                        // not a file:// URL (e.g. a remote repository proxy) - not scannable, skip
                    }
                }
            }
        }
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        if (contextLoader instanceof java.net.URLClassLoader urlLoader && seen.add(contextLoader)) {
            for (java.net.URL url : urlLoader.getURLs()) {
                try {
                    roots.add(new File(url.toURI()));
                } catch (java.net.URISyntaxException ignored) {
                    // as above
                }
            }
        }
        return roots;
    }

    // Package-relative path (directory-walk relative path, or JAR entry name) -> loadable class
    // name, or null to skip: shared by both collectClassesFrom* methods so the ".class" stripping,
    // "/"-or-File.separator normalization, "lphy." scoping, and $-nested-class exclusion stay
    // identical regardless of which classpath shape found the entry.
    private static String candidateClassName(String relativePath) {
        if (!relativePath.endsWith(".class") || relativePath.contains("$")) return null;
        String className = relativePath.substring(0, relativePath.length() - ".class".length())
                .replace('/', '.').replace(File.separatorChar, '.');
        return className.startsWith("lphy.") ? className : null;
    }

    private static void loadIfLphyClass(String relativePath, ClassLoader loader, List<Class<?>> out) {
        String className = candidateClassName(relativePath);
        if (className == null) return;
        try {
            out.add(Class.forName(className, false, loader));
        } catch (Throwable ignored) {
            // package-info-like or unloadable class - not a real class to scan, skip it
        }
    }

    private static void collectClassesFromDirectory(File root, File dir, ClassLoader loader, List<Class<?>> out) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) {
                collectClassesFromDirectory(root, f, loader, out);
            } else {
                loadIfLphyClass(root.toPath().relativize(f.toPath()).toString(), loader, out);
            }
        }
    }

    private static void collectClassesFromJar(File jarFile, ClassLoader loader, List<Class<?>> out) {
        try (java.util.jar.JarFile jar = new java.util.jar.JarFile(jarFile)) {
            Enumeration<java.util.jar.JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                loadIfLphyClass(entries.nextElement().getName(), loader, out);
            }
        } catch (IOException ignored) {
            // not a readable JAR (or not actually one despite the .jar extension) - skip it
        }
    }
}
