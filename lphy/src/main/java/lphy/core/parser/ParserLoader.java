package lphy.core.parser;

import lphy.core.exception.LoggerUtils;
import lphy.core.model.components.*;
import lphy.core.spi.LPhyLoader;
import lphy.core.vectorization.IID;
import lphy.core.vectorization.VectorMatch;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static lphy.core.vectorization.IID.REPLICATES_PARAM_NAME;

public class ParserLoader {

    static Map<String, Set<Class<?>>> genDistDictionary;
    static Map<String, Set<Class<?>>> functionDictionary;
    public static Set<String> bivarOperators;
    static Set<String> univarfunctions;

    public static TreeSet<Class<?>> types;// = new TreeSet<>(Comparator.comparing(Class::getName));

    // data types are held in SequenceTypeFactory singleton

    static {
        // registration process
        LPhyLoader lphyLoader = LPhyLoader.getInstance();
        genDistDictionary = lphyLoader.genDistDictionary;
        functionDictionary = lphyLoader.functionDictionary;

        types = lphyLoader.types;

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

    private static final int MAX_UNNAMED_ARGS = 3;

    public static List<Generator> getMatchingFunctions(String name, Value[] argValues) {
        List<Generator> matches = new ArrayList<>();
        for (Class functionClass : getFunctionClasses(name)) {
            matches.addAll(getFunctionByArguments(name, argValues, functionClass));
        }
        return matches;
    }

    public static List<Generator> getMatchingFunctions(String name, Map<String, Value> arguments) {
        List<Generator> matches = new ArrayList<>();
        for (Class functionClass : getFunctionClasses(name)) {
            matches.addAll(getGeneratorByArguments(name, arguments, functionClass));
        }
        return matches;
    }

    public static List<Generator> getMatchingGenerativeDistributions(String name, Map<String, Value> arguments) {
        List<Generator> matches = new ArrayList<>();

        Set<Class<?>> generators = getGenerativeDistributionClasses(name);

        if (generators != null) {
            for (Class genClass : getGenerativeDistributionClasses(name)) {
                matches.addAll(getGeneratorByArguments(name, arguments, genClass));
            }
        } else {
            LoggerUtils.log.severe("No generator with name " + name + " available.");
        }
        return matches;
    }

    public static List<Generator> getGeneratorByArguments(String name, Map<String, Value> arguments, Class generatorClass) {

        List<Generator> matches = new ArrayList<>();

        for (Constructor constructor : generatorClass.getConstructors()) {
            List<Argument> argumentInfo = Generator.getArguments(constructor);
            List<Object> initargs = new ArrayList<>();

            if (match(arguments, argumentInfo)) {

                for (int i = 0; i < argumentInfo.size(); i++) {
                    Value arg = arguments.get(argumentInfo.get(i).name);
                    if (arg != null) {
                        initargs.add(arg);
                    } else if (!argumentInfo.get(i).optional) {
                        throw new RuntimeException("Required argument " + argumentInfo.get(i).name + " not found!");
                    } else {
                        initargs.add(null);
                    }
                }

                matches.add(constructGenerator(name, constructor, argumentInfo, initargs.toArray(), arguments,false));
            }
        }
        return matches;
    }


    /**
     * A match occurs if the required parameters are in the argument map and the remaining arguments in the map match names of optional arguments.
     *
     * an iid match occurs if the required parameters are in the argument map and the remaining arguments include "replicates" and are otherwise named matchs for the optional arguments
     *
     * @param arguments the arguments that are attempting to be passed.
     * @param argumentInfo the arguments of the constructor
     * @return
     */
    private static boolean match(Map<String, Value> arguments, List<Argument> argumentInfo) {

        Set<String> requiredArguments = new TreeSet<>();
        Set<String> optionalArguments = new TreeSet<>();
        Set<String> keys = new TreeSet<>();
        keys.addAll(arguments.keySet());

        for (Argument argumentInf : argumentInfo) {
            if (argumentInf.optional) {
                optionalArguments.add(argumentInf.name);
            } else {
                requiredArguments.add(argumentInf.name);
            }
        }

        // return false if not all required arguments are present
        if (!keys.containsAll(requiredArguments)) {
            return false;
        }

        keys.removeAll(requiredArguments);
        keys.removeAll(optionalArguments);
        return keys.size() == 0 || (keys.size() == 1 && keys.contains(REPLICATES_PARAM_NAME));
    }

    private static List<DeterministicFunction> getFunctionByArguments(String name, Value[] argValues, Class generatorClass) {

        List<DeterministicFunction> matches = new ArrayList<>();
        for (Constructor constructor : generatorClass.getConstructors()) {
            List<Argument> arguments = Generator.getArguments(constructor);

            // unnamed args
            if (argValues.length == arguments.size() &&
                    (argValues.length > 0 && argValues.length <= MAX_UNNAMED_ARGS) ){
                DeterministicFunction f = (DeterministicFunction) constructGenerator(name, constructor, arguments, argValues, null, false);
                if (f != null) {
                    matches.add(f);
                }
            } else if (argValues.length == 0 && arguments.stream().allMatch(x -> x.optional)) {
                DeterministicFunction f = (DeterministicFunction) constructGenerator(name, constructor, arguments, new Object[arguments.size()], null, false);
                if (f != null) {
                    matches.add(f);
                }
            }
        }
        return matches;
    }

    /**
     * @param name the name of the generator
     * @param constructor the constructor
     * @param arguments
     * @param initargs
     * @param params
     * @param lightweight
     * @return
     */
    private static Generator constructGenerator(String name, Constructor constructor, List<Argument> arguments, Object[] initargs, Map<String, Value> params, boolean lightweight) {
        try {
            if (Generator.matchingParameterTypes(arguments, initargs, params, lightweight)) {
                return (Generator) constructor.newInstance(initargs);
            } else if (IID.match(constructor, arguments, initargs, params)) {
                IID iid = new IID(constructor, initargs, params);
                // if replicates = 1, do not apply IID
//                if (iid.size() == 1) {
//                    return (Generator) constructor.newInstance(initargs);
//                } else
                    return iid;
            } else if (VectorMatch.vectorMatch(arguments, initargs) > 0) {
                // do vector match
                return VectorMatch.vectorGenerator(constructor, arguments, initargs);
            } else {
                throw new RuntimeException("ERROR! No match in '" + name + "' constructor arguments, including vector match! ");
            }

        } catch (InstantiationException|IllegalAccessException|InvocationTargetException e) {
            LoggerUtils.logStackTrace(e);
            LoggerUtils.log.severe("Parsing generator " + name + " failed.");
        }
        return null;
    }

    private static Set<Class<?>> getGenerativeDistributionClasses(String name) {
        return genDistDictionary.get(name);
    }

    static Set<Class<?>> getFunctionClasses(String name) {
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
