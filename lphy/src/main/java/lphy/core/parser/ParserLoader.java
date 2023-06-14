package lphy.core.parser;

import lphy.core.model.DeterministicFunction;
import lphy.core.model.GenerativeDistribution;
import lphy.core.spi.LPhyCoreLoader;

import java.util.*;

public class ParserLoader {

    static Map<String, Set<Class<?>>> genDistDictionary;
    static Map<String, Set<Class<?>>> functionDictionary;
    public static Set<String> bivarOperators;
    static Set<String> univarfunctions;

    public static TreeSet<Class<?>> types;// = new TreeSet<>(Comparator.comparing(Class::getName));

    private static LPhyCoreLoader lphyCoreLoader = new LPhyCoreLoader();

    // data types are held in SequenceTypeFactory singleton

    static {
        // registration process
        lphyCoreLoader.loadAllExtensions();

        genDistDictionary = lphyCoreLoader.genDistDictionary;
        functionDictionary = lphyCoreLoader.functionDictionary;

        types = lphyCoreLoader.types;

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

    public ParserLoader() {}

    public static LPhyCoreLoader getLphyCoreLoader() {
        return lphyCoreLoader;
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


    public static Set<Class<?>> getGenerativeDistributionClasses(String name) {
        return genDistDictionary.get(name);
    }

    public static Set<Class<?>> getFunctionClasses(String name) {
        return functionDictionary.get(name);
    }

    public static List<Class<GenerativeDistribution>> getGenerativeDistributions() {
        List<Class<GenerativeDistribution>> genDists = new ArrayList<>();

        for (Set<Class<?>> classes : genDistDictionary.values()) {
            for (Class<?> c : classes) {
                genDists.add((Class<GenerativeDistribution>) c);
            }
        }
        return genDists;
    }

    public static List<Class<DeterministicFunction>> getDeterministicFunctions() {
        List<Class<DeterministicFunction>> functions = new ArrayList<>();

        for (Set<Class<?>> classes : functionDictionary.values()) {
            for (Class<?> c : classes) {
                functions.add((Class<DeterministicFunction>) c);
            }
        }
        return functions;
    }

}
