package lphy.core.vectorization;

import lphy.core.model.DeterministicFunction;
import lphy.core.model.GenerativeDistribution;
import lphy.core.model.Generator;
import lphy.core.model.Value;
import lphy.core.parser.argument.Argument;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Vectorization for arrays
 */
public class VectorMatchUtils {

    public static int vectorMatch(List<Argument> arguments, Object[] initargs) {
        int vectorMatches = 0;
        for (int i = 0; i < arguments.size(); i++) {
            Argument argument = arguments.get(i);
            Value argValue = (Value) initargs[i];

            if (argValue == null) {
                if (!argument.optional) return 0;
            } else {
                if (argument.type.isAssignableFrom(argValue.value().getClass())) {
                    // direct type match
                } else if (argValue.value().getClass().isArray() &&
                        argument.type.isAssignableFrom(argValue.value().getClass().getComponentType())) {
                    // vector match
                    vectorMatches += 1;
                }
            }
        }
        return vectorMatches;
    }

    public static Generator vectorGenerator(Constructor constructor, List<Argument> arguments, Object[] vectorArgs) throws IllegalAccessException, InvocationTargetException, InstantiationException {

        if (GenerativeDistribution.class.isAssignableFrom(constructor.getDeclaringClass())) {
            return new VectorizedDistribution(constructor, arguments, vectorArgs);
        } else if (DeterministicFunction.class.isAssignableFrom(constructor.getDeclaringClass())) {
            return new VectorizedFunction(constructor, arguments, vectorArgs);
        } else
            throw new IllegalArgumentException("Unexpected Generator class! Expecting a GenerativeDistribution or a DeterministicFunction");

    }

    /**
     * @param argumentInfos  the list of {@link Argument}
     * @param vectorArgs
     * @return   the sorted map whose key is the argument name
     *           and value is the {@link Value} of that argument.
     */
    public static Map<String, Value> convertArgumentsToParameterMap(List<Argument> argumentInfos, Object[] vectorArgs) {
        Map<String, Value> params = new TreeMap<>();
        for (int i = 0; i < argumentInfos.size(); i++) {
            Argument argumentInfo = argumentInfos.get(i);
            Value value = (Value) vectorArgs[i];

            if (value != null) params.put(argumentInfo.name, value);
        }
        return params;
    }
}
