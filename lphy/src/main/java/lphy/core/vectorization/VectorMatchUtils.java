package lphy.core.vectorization;

import lphy.core.model.DeterministicFunction;
import lphy.core.model.GenerativeDistribution;
import lphy.core.model.Generator;
import lphy.core.model.Value;
import lphy.core.parser.argument.Argument;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.apache.commons.lang3.BooleanUtils.or;

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

    //+++ For method call +++

    /**
     * @param methodName the method name
     * @param c the class of object on which the method is sought
     * @param paramTypes the param types that are vectorized version of actual parameter types
     * @return the first matching method, or null if none.
     */
    public static Method getVectorMatch(String methodName, Class c, Class[] paramTypes) {
        // check for vectorized arguments match
        for (Method method : c.getMethods()) {
            if (method.getName().equals(methodName)) {
                if (or(isVectorMatch(method, paramTypes))) {
                    return method;
                }
            }
        }
        return null;
    }

    /**
     * @param methodName the method that is a vector match for the given arguments, or null if no match found.
     * @param value the value for which a method call is attempted.
     * @param arguments the arguments of the method call.
     * @return
     */
    public static Method getVectorMatch(String methodName, Value<?> value, Value<?>[] arguments) {
        Class<?>[] paramTypes = new Class[arguments.length];

        for (int i = 0; i < paramTypes.length; i++) {
            paramTypes[i] = arguments[i].value().getClass();
        }

        Class c = value.value().getClass();

        try {
            // check for exact match
            Method method = c.getMethod(methodName, paramTypes);
        } catch (NoSuchMethodException nsme) {
            // check for vectorized match
            for (Method method : c.getMethods()) {
                if (method.getName().equals(methodName)) {
                    if (or(isVectorMatch(method, paramTypes))) {
                        return method;
                    }
                }
            }
            return null;
        }
        return null;
    }

    /**
     * @param method the method to check
     * @param paramTypes the param types to check for a vector match
     * @return a boolean array of length equal to paramTypes array, with true of each vector match, false otherwise.
     */
    public static boolean[] isVectorMatch(Method method, Class<?>[] paramTypes) {
        Class<?>[] methodParamTypes = method.getParameterTypes();

        if (methodParamTypes.length == paramTypes.length) {
            boolean[] vectorMatch = new boolean[paramTypes.length];
            for (int i = 0; i < methodParamTypes.length; i++) {
                vectorMatch[i] = isVectorMatch(methodParamTypes[i],paramTypes[i]);
            }
            return vectorMatch;
        }
        throw new IllegalArgumentException("paramTypes array must be same length as method param types array!");
    }

    /**
     * @param methodParamType the class of a method argument
     * @param paramType the class of a potential vector match
     * @return true if paramType is a vector match for methodParamType (i.e. paramType is an array and has components of a class assignable to methodParamType.
     */
    private static boolean isVectorMatch(Class<?> methodParamType, Class<?> paramType) {
        return (paramType.isArray() && methodParamType.isAssignableFrom(paramType.getComponentType()));
    }
}
