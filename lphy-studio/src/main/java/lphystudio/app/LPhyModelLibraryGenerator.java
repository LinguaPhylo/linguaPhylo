package lphystudio.app;

import lphy.core.model.BasicFunction;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.GenerativeDistribution;
import lphy.core.model.Generator;
import lphy.core.model.GeneratorUtils;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.IOFunction;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.spi.Extension;
import lphy.core.spi.LPhyCoreLoader;
import lphy.core.spi.LPhyExtension;
import lphy.core.spi.LoaderManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * LPhy Model Library Generator - generates types and generators for the PhyloSpec schema format
 */
public class LPhyModelLibraryGenerator {
    private static final Logger logger = Logger.getLogger(LPhyModelLibraryGenerator.class.getName());

    private final Set<String> knownTypeNames = new HashSet<>();
    private final Map<String, JSONObject> typeDefinitions = new HashMap<>();
    private final Map<String, JSONObject> generatorDefinitions = new HashMap<>();

    // Track LPhy primitive types
    private static final Set<String> LPHY_PRIMITIVES = Set.of(
            "Integer", "Double", "Number", "Boolean", "String",
            "Integer[]", "Double[]", "Number[]", "Boolean[]", "String[]",
            "Integer[][]", "Double[][]", "Number[][]"
    );

    /**
     * Generate the complete model library schema
     */
    public String generateModelLibrary(List<String> extensionClassNames) throws Exception {
        JSONObject schema = new JSONObject();
        JSONObject modelLibrary = new JSONObject();

        // Basic metadata
        modelLibrary.put("name", "LPhy Core Library");
        modelLibrary.put("version", "1.0.0");
        modelLibrary.put("engine", "LPhy");
        modelLibrary.put("engineVersion", getVersion());
        modelLibrary.put("description", "Core model components for LinguaPhylo");

        // Scan for all components
        scanAllComponents(extensionClassNames);

        // Generate types and generators
        JSONArray types = new JSONArray();
        typeDefinitions.values().forEach(types::put);
        modelLibrary.put("types", types);

        JSONArray generators = new JSONArray();
        generatorDefinitions.values().forEach(generators::put);
        modelLibrary.put("generators", generators);

        schema.put("modelLibrary", modelLibrary);
        return schema.toString(2);
    }

    /**
     * Scan all components from extensions
     */
    private void scanAllComponents(List<String> extensionClassNames) throws Exception {
        LPhyCoreLoader loader = LoaderManager.getLphyCoreLoader();
        Map<String, Extension> extensionMap = loader.getExtensionMap(extensionClassNames);

        if (extensionMap.isEmpty()) {
            throw new IllegalArgumentException("Cannot find extensions: " + extensionClassNames);
        }

        // Process each extension
        for (Extension extension : extensionMap.values()) {
            if (extension instanceof LPhyExtension) {
                LPhyExtension lphyExt = (LPhyExtension) extension;

                // Process distributions
                Map<String, Set<Class<?>>> distMap = lphyExt.getDistributions();
                for (Map.Entry<String, Set<Class<?>>> entry : distMap.entrySet()) {
                    for (Class<?> clazz : entry.getValue()) {
                        if (GenerativeDistribution.class.isAssignableFrom(clazz)) {
                            processGenerativeDistribution((Class<? extends GenerativeDistribution>) clazz, extension.getExtensionName());
                        }
                    }
                }

                // Process functions
                Map<String, Set<Class<?>>> funcMap = lphyExt.getFunctions();
                for (Map.Entry<String, Set<Class<?>>> entry : funcMap.entrySet()) {
                    for (Class<?> clazz : entry.getValue()) {
                        if (BasicFunction.class.isAssignableFrom(clazz)) {
                            processFunction((Class<? extends BasicFunction>) clazz, extension.getExtensionName());
                        }
                    }
                }

                // Process types
                Set<Class<?>> types = lphyExt.getTypes();
                for (Class<?> type : types) {
                    processType(type, extension.getExtensionName());
                }
            }
        }

        // Add primitive types
        addPrimitiveTypes();
    }

    /**
     * Process a generative distribution
     */
    private void processGenerativeDistribution(Class<? extends GenerativeDistribution> clazz, String packageName) {
        if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
            // Add as type only
            addTypeDefinition(clazz);
            return;
        }

        String name = GeneratorUtils.getGeneratorName(clazz);
        if (generatorDefinitions.containsKey(name)) {
            return; // Already processed
        }

        JSONObject generator = new JSONObject();
        generator.put("name", name);

        // Use the extension name as the package
        generator.put("package", packageName);
        generator.put("fullyQualifiedName", clazz.getName());
        generator.put("generatorType", "distribution");

        // Get description from GeneratorInfo annotation on the sample() method
        String description = getGeneratorDescription(clazz);
        generator.put("description", description);

        // Get examples from GeneratorInfo
        String[] examples = GeneratorUtils.getGeneratorExamples(clazz);
        if (examples.length > 0) {
            JSONArray examplesArray = new JSONArray(examples);
            generator.put("examples", examplesArray);
        }

        // Get category from GeneratorInfo
        String category = getGeneratorCategory(clazz);
        if (category != null && !category.isEmpty()) {
            generator.put("category", category);
        }

        // Determine generated type
        Class<?> generatedType = GeneratorUtils.getReturnType(clazz);
        String generatedTypeName = getSimpleTypeName(generatedType);
        generator.put("generatedType", generatedTypeName);

        // Add generated type to types if not primitive
        if (!isPrimitive(generatedTypeName)) {
            addTypeDefinition(generatedType);
        }

        // Get constructor parameters
        JSONArray arguments = new JSONArray();
        try {
            Constructor<?>[] constructors = clazz.getConstructors();
            if (constructors.length > 0) {
                // Use first constructor (can be enhanced to choose best one)
                Constructor<?> constructor = constructors[0];
                List<ParameterInfo> paramInfos = GeneratorUtils.getParameterInfo(constructor);

                Type[] paramTypes = constructor.getGenericParameterTypes();
                for (int i = 0; i < paramInfos.size(); i++) {
                    ParameterInfo pInfo = paramInfos.get(i);
                    Type paramType = i < paramTypes.length ? paramTypes[i] : Object.class;

                    JSONObject arg = createArgument(pInfo, paramType);
                    arguments.put(arg);
                }
            }
        } catch (Exception e) {
            logger.warning("Could not process constructor for " + clazz.getName() + ": " + e.getMessage());
        }

        generator.put("arguments", arguments);

        // For distributions, the primary argument is typically the first Value parameter
        if (arguments.length() > 0) {
            for (int i = 0; i < arguments.length(); i++) {
                JSONObject arg = arguments.getJSONObject(i);
                String argType = arg.getString("type");
                if (isValueType(argType)) {
                    generator.put("primaryArgument", arg);
                    break;
                }
            }
        }

        generatorDefinitions.put(name, generator);
    }

    /**
     * Process a function
     */
    private void processFunction(Class<? extends BasicFunction> clazz, String packageName) {
        if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
            // Add as type only
            addTypeDefinition(clazz);
            return;
        }

        String name = GeneratorUtils.getGeneratorName(clazz);
        if (generatorDefinitions.containsKey(name)) {
            return; // Already processed
        }

        JSONObject generator = new JSONObject();
        generator.put("name", name);

        // Use the extension name as the package
        generator.put("package", packageName);
        generator.put("fullyQualifiedName", clazz.getName());
        generator.put("generatorType", "function");

        // Get description from GeneratorInfo annotation
        String description = getGeneratorDescription(clazz);
        generator.put("description", description);

        // Get category from GeneratorInfo
        String category = getGeneratorCategory(clazz);
        if (category != null && !category.isEmpty()) {
            generator.put("category", category);
        }

        // Determine generated type
        Class<?> generatedType = GeneratorUtils.getReturnType(clazz);
        String generatedTypeName = getSimpleTypeName(generatedType);
        generator.put("generatedType", generatedTypeName);

        // Add generated type to types if not primitive
        if (!isPrimitive(generatedTypeName)) {
            addTypeDefinition(generatedType);
        }

        // Get constructor parameters
        JSONArray arguments = new JSONArray();
        try {
            Constructor<?>[] constructors = clazz.getConstructors();
            if (constructors.length > 0) {
                Constructor<?> constructor = constructors[0];
                List<ParameterInfo> paramInfos = GeneratorUtils.getParameterInfo(constructor);

                Type[] paramTypes = constructor.getGenericParameterTypes();
                for (int i = 0; i < paramInfos.size(); i++) {
                    ParameterInfo pInfo = paramInfos.get(i);
                    Type paramType = i < paramTypes.length ? paramTypes[i] : Object.class;

                    JSONObject arg = createArgument(pInfo, paramType);
                    arguments.put(arg);
                }
            }
        } catch (Exception e) {
            logger.warning("Could not process constructor for " + clazz.getName() + ": " + e.getMessage());
        }

        generator.put("arguments", arguments);

        // If annotated with @IOFunction, emit ioHints automatically
        IOFunction ioAnn = clazz.getAnnotation(IOFunction.class);
        if (ioAnn != null) {
            JSONObject io = new JSONObject();
            io.put("role", ioAnn.role().name());
            io.put("extensions", new JSONArray(Arrays.asList(ioAnn.extensions())));
            io.put("fileArgument", ioAnn.fileArgument());
            generator.put("ioHints", io);
        }

        generatorDefinitions.put(name, generator);
    }

    /**
     * Process a type
     */
    private void processType(Class<?> clazz, String packageName) {
        addTypeDefinition(clazz);
    }

    /**
     * Get generator description from GeneratorInfo annotation
     */
    private String getGeneratorDescription(Class<?> clazz) {
        // First try to get from GeneratorUtils (which looks at methods)
        String description = GeneratorUtils.getGeneratorDescription(clazz);
        if (description != null && !description.isEmpty()) {
            return description;
        }

        // Fallback to a default description
        String name = GeneratorUtils.getGeneratorName(clazz);
        return "The " + name + " distribution.";
    }

    /**
     * Get generator category from GeneratorInfo annotation
     */
    private String getGeneratorCategory(Class<?> clazz) {
        GeneratorInfo info = GeneratorUtils.getGeneratorInfo(clazz);
        if (info != null && info.category() != GeneratorCategory.NONE) {
            return info.category().getName();
        }
        return null;
    }

    /**
     * Add a type definition
     */
    private void addTypeDefinition(Class<?> clazz) {
        String typeName = getSimpleTypeName(clazz);

        if (typeDefinitions.containsKey(typeName) || isPrimitive(typeName)) {
            return;
        }

        // Skip Java standard library classes
        if (clazz.getName().startsWith("java.") || clazz.getName().startsWith("javax.")) {
            return;
        }

        // Derive package from the actual class
        String actualPackage = getPackageFromClass(clazz);

        JSONObject type = new JSONObject();
        type.put("name", typeName);
        type.put("package", actualPackage);
        type.put("fullyQualifiedName", clazz.getName());
        type.put("description", "LPhy " + typeName);

        // Class modifiers
        type.put("isAbstract", Modifier.isAbstract(clazz.getModifiers()));
        type.put("isInterface", clazz.isInterface());

        // Inheritance
        if (clazz.getSuperclass() != null && !clazz.getSuperclass().equals(Object.class)) {
            String superTypeName = getSimpleTypeName(clazz.getSuperclass());
            type.put("extends", superTypeName);

            // Add superclass to types
            if (!isPrimitive(superTypeName)) {
                addTypeDefinition(clazz.getSuperclass());
            }
        }

        // Interfaces - only include LPhy-specific interfaces
        JSONArray interfaces = new JSONArray();
        for (Class<?> iface : clazz.getInterfaces()) {
            String ifaceName = getSimpleTypeName(iface);
            if (isLPhyInterface(iface)) {
                interfaces.put(ifaceName);

                // Add interface to types
                if (!isPrimitive(ifaceName)) {
                    addTypeDefinition(iface);
                }
            }
        }
        if (interfaces.length() > 0) {
            type.put("implements", interfaces);
        }

        // Check if it's a Value type that can accept primitives
        if (Value.class.isAssignableFrom(clazz)) {
            type.put("primitiveAssignable", true);

            // Determine accepted primitives based on generic type
            JSONArray acceptedPrimitives = determineAcceptedPrimitives(clazz);
            if (acceptedPrimitives.length() > 0) {
                type.put("acceptedPrimitives", acceptedPrimitives);
            }
        }

        typeDefinitions.put(typeName, type);
        knownTypeNames.add(typeName);
    }

    /**
     * Get package name from a class
     */
    private String getPackageFromClass(Class<?> clazz) {
        String packageName = clazz.getPackage() != null ? clazz.getPackage().getName() : "";

        // Map to simplified package names for LPhy components
        if (packageName.startsWith("lphy.core")) {
            return "lphy.core";
        } else if (packageName.startsWith("lphy.base")) {
            return "lphy.base";
        } else if (packageName.startsWith("lphy.")) {
            return "lphy.extensions";
        }

        return packageName;
    }

    /**
     * Check if an interface is LPhy-specific
     */
    private boolean isLPhyInterface(Class<?> iface) {
        String name = iface.getName();
        return name.startsWith("lphy.") &&
                !name.equals("java.io.Serializable") &&
                !name.equals("java.lang.Cloneable") &&
                !name.startsWith("java.") &&
                !name.startsWith("javax.");
    }

    /**
     * Create an argument definition
     */
    private JSONObject createArgument(ParameterInfo pInfo, Type paramType) {
        JSONObject arg = new JSONObject();
        arg.put("name", pInfo.name());
        arg.put("description", pInfo.description());

        // Determine if optional
        arg.put("required", !pInfo.optional());

        // Handle type
        String typeName = getTypeNameFromType(paramType);
        arg.put("type", typeName);

        // Add type to types if not primitive
        if (!isPrimitive(typeName) && paramType instanceof Class) {
            addTypeDefinition((Class<?>) paramType);
        }

        return arg;
    }

    /**
     * Add primitive type definitions
     */
    private void addPrimitiveTypes() {
        String[][] primitiveTypes = {
                {"Integer", "Integer", "Integer values"},
                {"Double", "Double", "Floating point values"},
                {"Number", "Number", "Numeric values"},
                {"Boolean", "Boolean", "True/false values"},
                {"String", "String", "Text values"},
                {"Integer[]", "Integer[]", "Integer array"},
                {"Double[]", "Double[]", "Double array"},
                {"Number[]", "Number[]", "Number array"},
                {"Boolean[]", "Boolean[]", "Boolean array"},
                {"String[]", "String[]", "String array"}
        };

        for (String[] typeInfo : primitiveTypes) {
            JSONObject type = new JSONObject();
            type.put("name", typeInfo[0]);
            type.put("package", "lphy.core.types");
            type.put("description", typeInfo[2]);
            type.put("primitiveAssignable", true);

            JSONArray accepted = new JSONArray();
            accepted.put(typeInfo[1]);
            type.put("acceptedPrimitives", accepted);

            typeDefinitions.put(typeInfo[0], type);
            knownTypeNames.add(typeInfo[0]);
        }
    }

    /**
     * Determine accepted primitives for a Value type
     */
    private JSONArray determineAcceptedPrimitives(Class<?> clazz) {
        JSONArray primitives = new JSONArray();

        if (Value.class.isAssignableFrom(clazz)) {
            // Try to get the generic type parameter
            Type genericSuper = clazz.getGenericSuperclass();
            if (genericSuper instanceof ParameterizedType) {
                Type[] typeArgs = ((ParameterizedType) genericSuper).getActualTypeArguments();
                if (typeArgs.length > 0) {
                    String typeName = getTypeNameFromType(typeArgs[0]);
                    if (LPHY_PRIMITIVES.contains(typeName)) {
                        primitives.put(typeName);
                    }
                }
            }
        }

        return primitives;
    }

    /**
     * Get simple type name from a Type
     */
    private String getTypeNameFromType(Type type) {
        if (type instanceof Class) {
            return getSimpleTypeName((Class<?>) type);
        } else if (type instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) type;
            Type rawType = pType.getRawType();
            if (rawType instanceof Class) {
                Class<?> rawClass = (Class<?>) rawType;

                // Special handling for Value types
                if (Value.class.isAssignableFrom(rawClass)) {
                    Type[] typeArgs = pType.getActualTypeArguments();
                    if (typeArgs.length > 0) {
                        return getTypeNameFromType(typeArgs[0]);
                    }
                }

                return getSimpleTypeName(rawClass);
            }
        }

        return type.getTypeName();
    }

    /**
     * Get simple type name, handling arrays
     */
    private String getSimpleTypeName(Class<?> clazz) {
        if (clazz.isArray()) {
            return getSimpleTypeName(clazz.getComponentType()) + "[]";
        }

        // Map Java primitives to LPhy types
        String name = clazz.getSimpleName();
        switch (name) {
            case "int":
                return "Integer";
            case "double":
                return "Double";
            case "boolean":
                return "Boolean";
            default:
                return name;
        }
    }

    /**
     * Check if a type name is primitive
     */
    private boolean isPrimitive(String typeName) {
        return LPHY_PRIMITIVES.contains(typeName) ||
                typeName.equals("Object") ||
                typeName.startsWith("java.");
    }

    /**
     * Check if a type is a Value type
     */
    private boolean isValueType(String typeName) {
        return typeName.equals("Value") ||
                typeDefinitions.containsKey(typeName) &&
                        typeDefinitions.get(typeName).has("extends") &&
                        isValueType(typeDefinitions.get(typeName).getString("extends"));
    }

    /**
     * Get version (placeholder - implement properly)
     */
    private String getVersion() {
        return "1.0.0";
    }

    /**
     * Main method
     */
    public static void main(String[] args) {
        try {
            List<String> extensionClassNames = Arrays.asList(
                    "lphy.core.spi.LPhyCoreImpl",
                    "lphy.base.spi.LPhyBaseImpl"
            );

            if (args.length > 0) {
                extensionClassNames = Arrays.asList(args[0].split(";"));
            }

            LPhyModelLibraryGenerator generator = new LPhyModelLibraryGenerator();
            String jsonOutput = generator.generateModelLibrary(extensionClassNames);

            Files.write(Paths.get("lphy-model-library.json"), jsonOutput.getBytes());

            System.out.println("Generated lphy-model-library.json");
            System.out.println("Types and generators have been created following the PhyloSpec schema format");

        } catch (Exception e) {
            logger.severe("Error generating model library: " + e.getMessage());
            e.printStackTrace();
        }
    }
}