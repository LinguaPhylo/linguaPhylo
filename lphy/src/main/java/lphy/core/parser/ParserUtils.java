package lphy.core.parser;

import lphy.core.logger.LoggerUtils;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.Generator;
import lphy.core.model.Value;
import lphy.core.parser.argument.Argument;
import lphy.core.parser.argument.ArgumentUtils;
import lphy.core.spi.LoaderManager;
import lphy.core.vectorization.IID;
import lphy.core.vectorization.VectorMatchUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static lphy.core.vectorization.IID.REPLICATES_PARAM_NAME;

/**
 * Methods for {@link LPhyListenerImpl}
 */
public class ParserUtils {

    private static final int MAX_UNNAMED_ARGS = 3;

    /** deprecation messages already emitted, so each is logged only once */
    private static final Set<String> warnedDeprecations = ConcurrentHashMap.newKeySet();

    /** Log a deprecation warning the first time a given message is encountered. */
    private static void warnDeprecated(String message) {
        if (warnedDeprecations.add(message))
            LoggerUtils.log.warning(message);
    }

    public static List<Generator> getMatchingFunctions(String name, Value[] argValues) {
        List<Generator> matches = new ArrayList<>();
        for (Class functionClass : LoaderManager.getFunctionClasses(name)) {
            matches.addAll(getFunctionByArguments(name, argValues, functionClass));
        }
        return matches;
    }

    public static List<Generator> getMatchingFunctions(String name, Map<String, Value> arguments) {
        List<Generator> matches = new ArrayList<>();
        Set<Class<?>> functions = LoaderManager.getFunctionClasses(name);
        if (functions != null) {
            warnIfDeprecatedName(name);
            for (Class functionClass : functions) {
                matches.addAll(getGeneratorByArguments(name, arguments, functionClass));
            }
        }
        return matches;
    }

    public static List<Generator> getMatchingGenerativeDistributions(String name, Map<String, Value> arguments) {
        List<Generator> matches = new ArrayList<>();

        Set<Class<?>> generators = LoaderManager.getAllGenerativeDistributionClasses(name);

        if (generators != null) {
            warnIfDeprecatedName(name);
            for (Class genClass : generators) {
                matches.addAll(getGeneratorByArguments(name, arguments, genClass));
            }
        } else {
            LoggerUtils.log.severe("No generator with name " + name + " available.");
        }
        return matches;
    }

    /** Warn if {@code name} is a deprecated generator/function alias, pointing to the canonical name. */
    private static void warnIfDeprecatedName(String name) {
        String canonical = LoaderManager.getCanonicalGeneratorName(name);
        if (canonical != null)
            warnDeprecated("'" + name + "' is a deprecated name; use '" + canonical + "' instead.");
    }

    //*** private ***//

    private static List<Generator> getGeneratorByArguments(String name, Map<String, Value> arguments, Class generatorClass) {

        List<Generator> matches = new ArrayList<>();

        for (Constructor constructor : generatorClass.getConstructors()) {
            List<Argument> argumentInfo = ArgumentUtils.getArguments(constructor);
            List<Object> initargs = new ArrayList<>();

            if (match(arguments, argumentInfo)) {

                for (int i = 0; i < argumentInfo.size(); i++) {
                    Argument argument = argumentInfo.get(i);
                    String key = argument.matchingKey(arguments.keySet());
                    Value arg = key != null ? arguments.get(key) : null;
                    if (arg != null) {
                        // a deprecated alias was used: canonicalise the key in the shared arguments
                        // map so the downstream setInput() call uses the generator's real param name
                        if (!key.equals(argument.name)) {
                            warnDeprecated("Argument '" + key + "' of '" + name +
                                    "' is a deprecated name; use '" + argument.name + "' instead.");
                            arguments.remove(key);
                            arguments.put(argument.name, arg);
                        }
                        initargs.add(arg);
                    } else if (!argument.optional) {
                        throw new RuntimeException("Required argument " + argument.name + " not found!");
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

        // each argument consumes its supplying key (canonical name or a deprecated alias);
        // a required argument with no supplying key fails the match
        Set<String> keys = new TreeSet<>(arguments.keySet());

        for (Argument argumentInf : argumentInfo) {
            String key = argumentInf.matchingKey(keys);
            if (key != null) {
                keys.remove(key);
            } else if (!argumentInf.optional) {
                return false;
            }
        }

        // any remaining keys (other than "replicates") mean the arguments do not match
        return keys.size() == 0 || (keys.size() == 1 && keys.contains(REPLICATES_PARAM_NAME));
    }

    private static List<DeterministicFunction> getFunctionByArguments(String name, Value[] argValues, Class generatorClass) {

        List<DeterministicFunction> matches = new ArrayList<>();
        for (Constructor constructor : generatorClass.getConstructors()) {
            List<Argument> arguments = ArgumentUtils.getArguments(constructor);

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
            if (ArgumentUtils.matchingParameterTypes(arguments, initargs, params, lightweight)) {
                return (Generator) constructor.newInstance(initargs);
            } else if (IID.match(constructor, arguments, initargs, params)) {
                IID iid = new IID(constructor, initargs, params);
                // if replicates = 1, do not apply IID
//                if (iid.size() == 1) {
//                    return (Generator) constructor.newInstance(initargs);
//                } else
                    return iid;
            } else if (VectorMatchUtils.vectorMatch(arguments, initargs) > 0) {
                // do vector match
                return VectorMatchUtils.vectorGenerator(constructor, arguments, initargs);
            } else {
                throw new RuntimeException("ERROR! No match in '" + name + "' constructor arguments, including vector match! ");
            }

        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            LoggerUtils.logStackTrace(e);
            LoggerUtils.log.severe("Parsing generator " + name + " failed.");
        }
        return null;
    }
}
