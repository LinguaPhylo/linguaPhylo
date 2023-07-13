package lphy.core.spi;

import lphy.core.logger.ValueFormatResolver;
import lphy.core.logger.ValueFormatter;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.GenerativeDistribution;

import java.util.*;

/**
 * Load all loaders here
 */
public class LoaderManager {

    private static Map<String, Set<Class<?>>> genDistDictionary;
    private static Map<String, Set<Class<?>>> functionDictionary;
    private static Set<String> bivarOperators;
    private static Set<String> univarfunctions;

    private static TreeSet<Class<?>> types;// = new TreeSet<>(Comparator.comparing(Class::getName));

    private static LPhyCoreLoader lphyCoreLoader = new LPhyCoreLoader();

    // TODO handle resolve strategies here?
    public static ValueFormatResolver valueFormatResolver;

    // data types are held in SequenceTypeFactory singleton

    static {
        // registration process
        lphyCoreLoader.loadAllExtensions();

        genDistDictionary = lphyCoreLoader.genDistDictionary;
        functionDictionary = lphyCoreLoader.functionDictionary;

        types = lphyCoreLoader.types;

        ValueFormatterLoader lphyValueFormatterLoader = new ValueFormatterLoader();
        lphyValueFormatterLoader.loadAllExtensions();
        Map<Class<?>, Set<Class<? extends ValueFormatter>>> valueFormatters =
                lphyValueFormatterLoader.getValueFormattersClasses();
        // pass all ValueFormatter classes to the Resolver
        valueFormatResolver = new ValueFormatResolver(valueFormatters);

        bivarOperators = new HashSet<>();
        for (String s : new String[]{"+", "-", "*", "/", "**", "&&", "||", "<=", "<", ">=", ">", "%", ":", "^", "!=", "==", "&", "|", "<<", ">>", ">>>"}) {
            bivarOperators.add(s);
        }
        univarfunctions = new HashSet<>();
        for (String s : new String[]{"abs", "acos", "acosh", "asin", "asinh", "atan", "atanh", "cLogLog", "cbrt", "ceil", "cos", "cosh", "exp", "expm1", "floor", "log", "log10", "log1p", "logFact", "logGamma", "logit", "phi", "probit", "round", "signum", "sin", "sinh", "sqrt", "step", "tan", "tanh"}) {
            univarfunctions.add(s);
        }

        // register data types
//        Map<String, SequenceType> dataTypeMap = lphyLoader.dataTypeMap;
//        SequenceTypeFactory.INSTANCE.setDataTypeMap(dataTypeMap);
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
