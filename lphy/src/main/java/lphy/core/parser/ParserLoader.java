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

    // data types are held in SequenceTypeFactory singleton

    static {
        // registration process
        LPhyCoreLoader lphyCoreLoader = LPhyCoreLoader.getInstance();
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
