package lphy.phylospec.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lphy.core.model.BasicFunction;
import lphy.core.model.GenerativeDistribution;
import lphy.core.model.GeneratorUtils;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.spi.Extension;
import lphy.core.spi.LPhyExtension;
import lphy.core.spi.LoaderManager;
import org.phylospec.components.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.*;

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
        library.setGenerators(buildGenerators(extensions));

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

    /**
     * Mirrors {@code lphystudio.app.docgenerator.GenerateDocs}'s own type-collection logic (its
     * "Types" section of {@code generateMarkdown()}, which writes one {@code docs/types/<name>.md}
     * page per distinct registered {@code Class}, named by {@code Class::getSimpleName()} and
     * deduped only by that literal name): one entry per distinct {@code Class} here too, named the
     * same way, deduped the same way - no translation, no merging, no collapsing.
     * <p>
     * This is deliberate, not an oversight: <b>LPhy does not have a "Vector" data type</b>.
     * Vectorisation ({@code lphy.core.vectorization} - the {@code replicates} argument, or passing
     * a vector of elements where a generator expects a scalar) is a language *mechanism* that
     * produces a genuine array value - e.g. vectorising a {@code Double} generator gives the real
     * LPhy data type {@code Double[]}, vectorising a {@code Vector<String>}-shaped one gives
     * {@code String[][]}. The array class itself (e.g. {@code Double[].class}) - not some generic
     * container - <i>is</i> the LPhy data type, exactly as {@code GenerateDocs} already treats it
     * (a real {@code docs/types/Double[].md} page, never a generic "Vector" page). So the resulting
     * names here are exactly {@code docs/types/*.md}'s file names (e.g. "Boolean[]", "Object",
     * "Number", "TimeTree[]") - only the container differs: a phylospec {@link Type} JSON object
     * here, a markdown page there.
     */
    private static List<Type> buildTypes(List<LPhyExtension> extensions) {
        TreeSet<Class<?>> types = new TreeSet<>(Comparator.comparing(Class::getName));
        for (LPhyExtension extension : extensions) {
            types.addAll(extension.getTypes());
        }

        Map<String, Type> typesByName = new LinkedHashMap<>();
        for (Class<?> c : types) {
            String name = c.getSimpleName();
            if (typesByName.containsKey(name)) continue;
            Type type = new Type();
            type.setName(name);
            type.setNamespace("lphy.types");
            type.setDescription("");
            // Every entry here is already a concrete, fully-instantiated LPhy class, not a generic
            // template like PhyloSpec's own Vector<T>/Map<K,V> - omit (Type is NON_NULL).
            type.setTypeParameters(null);
            // Omit (Type is NON_NULL) rather than emit an empty list.
            type.setTypeProperties(null);
            typesByName.put(name, type);
        }
        return new ArrayList<>(typesByName.values());
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
        String name = (info == null || info.phylospec().isEmpty())
                ? GeneratorUtils.getGeneratorName(c) : info.phylospec();
        String namespace = namespaceFor(info, isDistribution);
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

    private static String namespaceFor(GeneratorInfo info, boolean isDistribution) {
        String base = isDistribution ? "lphy.distributions" : "lphy.functions";
        if (info == null || info.category() == null
                || "NONE".equals(info.category().name()) || "ALL".equals(info.category().name())) {
            return base;
        }
        return base + "." + info.category().name().toLowerCase();
    }
}
