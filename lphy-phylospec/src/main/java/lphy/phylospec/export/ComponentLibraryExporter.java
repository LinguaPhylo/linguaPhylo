package lphy.phylospec.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lphy.core.model.BasicFunction;
import lphy.core.model.GenerativeDistribution;
import lphy.core.model.GeneratorUtils;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.spi.LoaderManager;
import org.phylospec.components.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reflects over every LPhy generator ({@link GenerativeDistribution}, {@link BasicFunction})
 * and data type registered via {@link LoaderManager}, and writes them out as a component
 * library JSON file in the same format as phylospec-core-component-library.json.
 * <p>
 * Usage: {@code mvn -pl lphy-phylospec -am exec:exec -Dexporter.args=path/to/output.json}
 */
public class ComponentLibraryExporter {

    // relative to this module's own basedir (exec:java's working directory), not the repo root
    private static final String DEFAULT_OUTPUT =
            "src/main/resources/phylospec-lphy-component-library.json";

    public static void main(String[] args) throws IOException {
        // args[0] can be null (not just absent/empty) here: a self-closing <exporter.args/> pom
        // property with no override resolves to a null Maven property, which exec-maven-plugin
        // still passes through as one array element rather than omitting it.
        String outputPath = (args.length > 0 && args[0] != null && !args[0].isBlank())
                ? args[0] : DEFAULT_OUTPUT;

        ComponentLibrary library = new ComponentLibrary();
        library.setName("LPhy");
        library.setVersion("0.1.0");
        library.setDescription("Generators and data types available in LinguaPhylo (LPhy), exported for PhyloSpec.");
        library.setAuthors(List.of("LinguaPhylo team"));
        library.setLicense("LGPL-3.0");
        library.setTypes(buildTypes());
        library.setGenerators(buildGenerators());

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
    }

    private static List<Type> buildTypes() {
        List<Type> types = new ArrayList<>();
        for (Class<?> c : LoaderManager.getTypes()) {
            Type type = new Type();
            type.setName(phyloSpecTypeName(c));
            type.setNamespace("lphy.types");
            type.setDescription("");
            types.add(type);
        }
        return types;
    }

    private static List<Generator> buildGenerators() {
        List<Generator> generators = new ArrayList<>();

        for (Class<GenerativeDistribution> c : LoaderManager.getAllClassesOfType(
                LoaderManager.getGenDistDictionary(), GenerativeDistribution.class)) {
            generators.addAll(buildGenerators(c, true));
        }
        for (Class<BasicFunction> c : LoaderManager.getAllClassesOfType(
                LoaderManager.getFunctionDictionary(), BasicFunction.class)) {
            generators.addAll(buildGenerators(c, false));
        }
        return generators;
    }

    /**
     * One entry per public constructor, so a generator with multiple constructor overloads
     * (e.g. Yule with/without a root age) becomes multiple entries sharing a name, matching
     * how the PhyloSpec component library models overloading.
     */
    private static List<Generator> buildGenerators(Class<?> c, boolean isDistribution) {
        GeneratorInfo info = GeneratorUtils.getGeneratorInfo(c);
        String name = (info == null || info.phylospec().isEmpty())
                ? GeneratorUtils.getGeneratorName(c) : info.phylospec();
        String namespace = namespaceFor(info, isDistribution);
        String returnType = phyloSpecTypeName(GeneratorUtils.getReturnType(c));
        String generatedType = isDistribution ? "Distribution<" + returnType + ">" : returnType;

        List<Generator> generators = new ArrayList<>();
        for (Constructor<?> constructor : c.getConstructors()) {
            Generator generator = new Generator();
            generator.setName(name);
            generator.setDescription(info == null ? "" : info.description());
            generator.setNamespace(namespace);
            generator.setGeneratedType(generatedType);
            generator.setArguments(buildArguments(constructor));
            // explicit @GeneratorInfo.phylospec() annotation value, if the class declared one
            // (e.g. HKY's apply() sets phylospec = "hky") - kept alongside `name` rather than
            // folded into it, so it's visible whether a name came from an explicit annotation
            // or is just the LPhy name used as-is.
            if (info != null && !info.phylospec().isEmpty()) {
                generator.setAdditionalProperty("phylospec", info.phylospec());
            }
            generators.add(generator);
        }
        return generators;
    }

    private static List<Argument> buildArguments(Constructor<?> constructor) {
        List<ParameterInfo> paramInfos = GeneratorUtils.getParameterInfo(constructor);
        java.lang.reflect.Type[] genericParamTypes = constructor.getGenericParameterTypes();

        List<Argument> arguments = new ArrayList<>();
        for (int i = 0; i < paramInfos.size(); i++) {
            ParameterInfo pInfo = paramInfos.get(i);
            Argument argument = new Argument();
            argument.setName(pInfo.phylospec().isEmpty() ? pInfo.name() : pInfo.phylospec());
            argument.setDescription(pInfo.description());
            argument.setRequired(!pInfo.optional());
            Class<?> paramType = i < genericParamTypes.length
                    ? GeneratorUtils.getClass(genericParamTypes[i]) : Object.class;
            argument.setType(phyloSpecTypeName(paramType));
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

    private static String namespaceFor(GeneratorInfo info, boolean isDistribution) {
        String base = isDistribution ? "lphy.distributions" : "lphy.functions";
        if (info == null || info.category() == null
                || "NONE".equals(info.category().name()) || "ALL".equals(info.category().name())) {
            return base;
        }
        return base + "." + info.category().name().toLowerCase();
    }

    /**
     * Hand-maintained map from LPhy's Java classes to PhyloSpec type names. PhyloSpec's numeric
     * types form a refinement lattice (Real, NonNegativeReal, PositiveReal, Probability, ...)
     * that Java's plain Double/Integer/Number can't express - this only covers the unambiguous
     * base cases. Anything unmapped falls back to its simple class name and is logged to stderr
     * so it can be reviewed and added here (or handled via a future ParameterInfo bounds annotation).
     */
    private static final Map<Class<?>, String> JAVA_TO_PHYLOSPEC_TYPE = new LinkedHashMap<>();
    static {
        JAVA_TO_PHYLOSPEC_TYPE.put(Double.class, "Real");
        JAVA_TO_PHYLOSPEC_TYPE.put(Float.class, "Real");
        JAVA_TO_PHYLOSPEC_TYPE.put(Number.class, "Real");
        JAVA_TO_PHYLOSPEC_TYPE.put(Integer.class, "Integer");
        JAVA_TO_PHYLOSPEC_TYPE.put(Long.class, "Integer");
        JAVA_TO_PHYLOSPEC_TYPE.put(Boolean.class, "Boolean");
        JAVA_TO_PHYLOSPEC_TYPE.put(String.class, "String");
        JAVA_TO_PHYLOSPEC_TYPE.put(Object.class, "Any");
    }

    private static final java.util.Set<Class<?>> UNMAPPED_TYPES_LOGGED = new java.util.HashSet<>();

    private static String phyloSpecTypeName(Class<?> c) {
        if (c == null) return "Any";
        String mapped = JAVA_TO_PHYLOSPEC_TYPE.get(c);
        if (mapped != null) return mapped;
        if (c.isArray()) {
            return "Vector<" + phyloSpecTypeName(c.getComponentType()) + ">";
        }
        if (UNMAPPED_TYPES_LOGGED.add(c)) {
            System.err.println("[ComponentLibraryExporter] No PhyloSpec type mapping for "
                    + c.getName() + " - falling back to simple name '" + c.getSimpleName()
                    + "'; add an entry to JAVA_TO_PHYLOSPEC_TYPE if this is wrong.");
        }
        return c.getSimpleName();
    }
}
