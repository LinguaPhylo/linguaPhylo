package lphy.core.spi;

import lphy.core.logger.LoggerUtils;
import lphy.core.logger.ValueFormatResolver;
import lphy.core.logger.ValueFormatter;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.GenerativeDistribution;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Load all loaders here
 */
public class LoaderManager {

    private static Map<String, Set<Class<?>>> genDistDictionary = new TreeMap<>();
    private static Map<String, Set<Class<?>>> functionDictionary = new TreeMap<>();
    private static TreeSet<Class<?>> types = new TreeSet<>(Comparator.comparing(Class::getName));

    private static Set<String> bivarOperators;
    private static Set<String> univarfunctions;

    private static LPhyCoreLoader lphyCoreLoader = new LPhyCoreLoader();

    // TODO handle resolve strategies here?
    public static ValueFormatResolver valueFormatResolver;

    // data types are held in SequenceTypeFactory singleton

    static {
        // registration process
        lphyCoreLoader.loadExtensions();

        collectAllRegisteredClasses();
        report();
    }

    static void collectAllRegisteredClasses() {
        Map<Class<?>, Set<Class<? extends ValueFormatter>>> allValueFormatters = new LinkedHashMap<>();

        Map<String, Extension> extMap = lphyCoreLoader.getExtensionMap();
        // loop through all extesions
        for (Map.Entry<String, Extension> entry : extMap.entrySet()) {
            Extension extension = entry.getValue();
            if (LPhyExtension.class.isAssignableFrom(extension.getClass())) {
                // {@link GenerativeDistribution}, {@link BasicFunction}.
                genDistDictionary.putAll(((LPhyExtension) extension).getDistributions());
                functionDictionary.putAll(((LPhyExtension) extension).getFunctions());
                types.addAll(((LPhyExtension) extension).getTypes());
            } else if (ValueFormatterExtension.class.isAssignableFrom(extension.getClass())) {
                // TODO will this overwrite ?
                allValueFormatters.putAll(((ValueFormatterExtension) extension).getValueFormatters());
            } else {
                LoggerUtils.log.fine("Unsolved extension from core : " + extension.getExtensionName()
                        + ", which may be registered in " + extension.getModuleName());
            }
        }
        // pass all ValueFormatter classes to the Resolver
        valueFormatResolver = new ValueFormatResolver(allValueFormatters);

        bivarOperators = new HashSet<>();
        for (String s : new String[]{"+", "-", "*", "/", "**", "&&", "||", "<=", "<", ">=", ">", "%", ":", "^", "!=", "==", "&", "|", "<<", ">>", ">>>"}) {
            bivarOperators.add(s);
        }
        univarfunctions = new HashSet<>();
        for (String s : new String[]{"abs", "acos", "acosh", "asin", "asinh", "atan", "atanh", "cLogLog", "cbrt", "ceil", "cos", "cosh", "exp", "expm1", "floor", "log", "log10", "log1p", "logFact", "logGamma", "logit", "phi", "probit", "round", "signum", "sin", "sinh", "sqrt", "step", "tan", "tanh"}) {
            univarfunctions.add(s);
        }

    }

    static void report() {
        System.out.println("\nGenerativeDistribution : " + Arrays.toString(genDistDictionary.keySet().toArray()));
        System.out.println("Functions : " + Arrays.toString(functionDictionary.keySet().toArray()));
        // for non-module release
        if (genDistDictionary.size() < 1 || functionDictionary.size() < 1)
            LoggerUtils.log.warning("LPhy base or equivalent lib was not loaded ! ");

        TreeSet<String> typeNames = types.stream().map(Class::getSimpleName).collect(Collectors.toCollection(TreeSet::new));
        System.out.println("LPhy data types : " + typeNames);
//            System.out.println("LPhy sequence types : " + Arrays.toString(dataTypeMap.values().toArray(new SequenceType[0])));

        System.out.println("Value formatter for : " +
                Arrays.toString(valueFormatResolver.getResolvedFormatterClasses().keySet().stream()
                        // sorted SimpleName of Classes
                        .map(Class::getSimpleName).sorted().toArray() ));
    }


    public LoaderManager() {
    }

    public static <T> void registerClasses(List<Class<? extends T>> clsInExtension,
                                           Map<String, Set<Class<?>>> clsDictionary) {
        for (Class<? extends T> cls : clsInExtension) {
            String name = cls.getSimpleName();

            Set<Class<?>> clsSet = clsDictionary.computeIfAbsent(name, k -> new HashSet<>());
            clsSet.add(cls);
        }
    }

    public static LPhyCoreLoader getLphyCoreLoader() {
        return lphyCoreLoader;
    }


    public static Set<Class<?>> getAllGenerativeDistributionClasses(String name) {
        return genDistDictionary.get(name);
    }

    public static Set<Class<?>> getFunctionClasses(String name) {
        return functionDictionary.get(name);
    }

    /**
     * @return a list of {@link GenerativeDistribution} flattened
     * from a map of sets in {@link #genDistDictionary}
     */
    public static List<Class<GenerativeDistribution>> getAllGenerativeDistributionClasses() {
        List<Class<GenerativeDistribution>> genDists = new ArrayList<>();

        for (Set<Class<?>> classes : genDistDictionary.values()) {
            for (Class<?> c : classes) {
                genDists.add((Class<GenerativeDistribution>) c);
            }
        }
        return genDists;
    }

    /**
     * @return a list of {@link DeterministicFunction} flattened
     * from a map of sets in {@link #functionDictionary}
     */
    public static List<Class<DeterministicFunction>> getAllFunctionsClasses() {
        List<Class<DeterministicFunction>> functions = new ArrayList<>();

        for (Set<Class<?>> classes : functionDictionary.values()) {
            for (Class<?> c : classes) {
                functions.add((Class<DeterministicFunction>) c);
            }
        }
        return functions;
    }


    public static Map<String, Set<Class<?>>> getGenDistDictionary() {
        return genDistDictionary;
    }

    public static Map<String, Set<Class<?>>> getFunctionDictionary() {
        return functionDictionary;
    }

    public static Set<String> getBivarOperators() {
        return bivarOperators;
    }

    public static Set<String> getUnivarfunctions() {
        return univarfunctions;
    }

    public static TreeSet<Class<?>> getTypes() {
        return types;
    }


    //    private static List<LPhyLoader> lPhyLoaders = new ArrayList<>();
//
//    static void loadAllLoaders() {
//        try {
//            ServiceLoader<LPhyLoader> serviceLoader = ServiceLoader.load(LPhyLoader.class);
//            for (LPhyLoader service : serviceLoader) {
//                System.out.println("Load " + service.getClass().getName());
//
//                service.loadAllExtensions();
//                lPhyLoaders.add(service);
//            }
//
//        } catch (ServiceConfigurationError serviceError) {
//            System.err.println(serviceError);
//            serviceError.printStackTrace();
//        }
//    }
//
//    private static ParserLoader parserLoader;
//    private ParserLoader() {
//
//    }
//
//    // singleton
//    public static ParserLoader getInstance() {
//        if (parserLoader == null)
//            parserLoader = new ParserLoader();
//        return parserLoader;
//    }
//
//    public LPhyLoader getALoader(Class<? extends LPhyLoader> loaderClass) {
//        for (LPhyLoader lPhyLoader : lPhyLoaders) {
//            if (lPhyLoader.getClass().isAssignableFrom(loaderClass))
//                return lPhyLoader;
//        }
//        throw new RuntimeException("Cannot find the loader class ! " + loaderClass.getName());
////        return null;
//    }
//
//    private static LPhyCoreLoader getLPhyCoreLoader() {
//        for (LPhyLoader lPhyLoader : lPhyLoaders) {
//            if (lPhyLoader instanceof LPhyCoreLoader lPhyCoreLoader)
//                return lPhyCoreLoader;
//        }
//        throw new RuntimeException("Cannot find the loader class ! " + LPhyCoreLoader.class.getName());
////        return null;
//    }


}
