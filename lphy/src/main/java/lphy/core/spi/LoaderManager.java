package lphy.core.spi;

import lphy.core.logger.FileLogger;
import lphy.core.logger.RandomValueLogger;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.GenerativeDistribution;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Load all loaders here
 */
public class LoaderManager {

    private static Map<String, Set<Class<?>>> genDistDictionary;
    private static Map<String, Set<Class<?>>> functionDictionary;
    private static Set<String> bivarOperators;
    private static Set<String> univarfunctions;

    private static TreeSet<Class<?>> types;// = new TreeSet<>(Comparator.comparing(Class::getName));

    private static Map<String, Set<Class<?>>> simulationLoggers;

    private static LPhyCoreLoader lphyCoreLoader = new LPhyCoreLoader();

    private static LPhyLoggerLoader lphyLoggerLoader = new LPhyLoggerLoader();

    // data types are held in SequenceTypeFactory singleton

    static {
        // registration process
        lphyCoreLoader.loadAllExtensions();

        genDistDictionary = lphyCoreLoader.genDistDictionary;
        functionDictionary = lphyCoreLoader.functionDictionary;

        types = lphyCoreLoader.types;

        lphyLoggerLoader.loadAllExtensions();
        simulationLoggers = lphyLoggerLoader.simulationLoggers;

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

    public LoaderManager() {}

    public static LPhyCoreLoader getLphyCoreLoader() {
        return lphyCoreLoader;
    }

    public static LPhyLoggerLoader getLPhyLoggerLoader() {
        return lphyLoggerLoader;
    }

    public static Set<Class<?>> getAllGenerativeDistributionClasses(String name) {
        return genDistDictionary.get(name);
    }

    public static Set<Class<?>> getFunctionClasses(String name) {
        return functionDictionary.get(name);
    }

    /**
     * @return  a list of {@link GenerativeDistribution} flattened
     *          from a map of sets in {@link #genDistDictionary}
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
     * @return  a list of {@link DeterministicFunction} flattened
     *          from a map of sets in {@link #functionDictionary}
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

    /**
     * @return  a list of {@link RandomValueLogger} flattened
     *          from a map of sets in {@link #simulationLoggers}
     */
    public static List<Class<RandomValueLogger>> getAllSimulationLoggerClasses() {
        List<Class<RandomValueLogger>> loggers = new ArrayList<>();

        for (Set<Class<?>> classes : simulationLoggers.values()) {
            for (Class<?> c : classes) {
                loggers.add((Class<RandomValueLogger>) c);
            }
        }
        return loggers;
    }

    public static List<RandomValueLogger> getSimulationLoggers() {

        List<Class<RandomValueLogger>> classList = getAllSimulationLoggerClasses();

        List<RandomValueLogger> instances = classList.stream()
                .map(cls -> {
                    try {
                        return cls.newInstance();
                    } catch (InstantiationException | IllegalAccessException e) {
                        e.printStackTrace();
                        return null; // or handle the exception as needed
                    }
                }).collect(Collectors.toList());

        return instances;
    }

    public static List<FileLogger> getFileLoggers() {
        return getSimulationLoggers().stream()
                .filter(FileLogger.class::isInstance)
                .map(FileLogger.class::cast)
                .collect(Collectors.toList());
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
