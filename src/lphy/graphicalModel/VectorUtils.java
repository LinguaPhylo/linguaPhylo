package lphy.graphicalModel;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class VectorUtils {

    public static boolean isVectorizedParameter(String argumentName,  Value value, Map<String, Class> baseTypes) {
        return (isArrayOfType(value, baseTypes.get(argumentName)));
    }

    public static int getVectorSize(Map<String, Value> params, Map<String, Class> baseTypes) {
        int size = 1;
        for (Map.Entry<String, Value> entry : params.entrySet()) {
            String name = entry.getKey();
            Value v = entry.getValue();
            if (isArrayOfType(v, baseTypes.get(name))) {
                int vectorSize = Array.getLength(v.value());
                if (size == 1) {
                    size = vectorSize;
                } else if (size != vectorSize) {
                    throw new RuntimeException("Vector sizes do not match!");
                }
            }
        }
        return size;
    }

    public static int getVectorSize(List<ParameterInfo> parameterInfos, Object[] args) {

        int size = -1;
        for (int i = 0; i < parameterInfos.size(); i++) {
            ParameterInfo parameterInfo = parameterInfos.get(i);
            Value argValue = (Value) args[i];
            if (argValue == null) {
                if (!parameterInfo.optional())
                    throw new IllegalArgumentException("Required parameter " + parameterInfo.name() + " not including in vector arguments");
            } else {
                Class argValueClass = argValue.value().getClass();

                if (parameterInfo.type().isAssignableFrom(argValueClass)) {
                    // direct type match
                } else if (argValueClass.isArray() && parameterInfo.type().isAssignableFrom(argValueClass.getComponentType())) {
                    // vector match
                    int length = Array.getLength(argValue.value());
                    if (size == -1) {
                        size = length;
                    } else {
                        if (size != length) throw new IllegalArgumentException("Vector lengths don't match!");
                    }
                }
            }
        }

        return size;
    }

    public static List<Generator> getComponentGenerators(Constructor constructor, List<ParameterInfo> parameterInfos, Object[] vectorArgs) throws IllegalAccessException, InvocationTargetException, InstantiationException {

        int size = getVectorSize(parameterInfos, vectorArgs);
        List<Generator> generators = new ArrayList<>(size);
        for (int component = 0; component < size; component++) {
            generators.add(getComponentGenerator(constructor, parameterInfos, vectorArgs, component));
        }
        return generators;
    }

    public static Generator getComponentGenerator(Constructor constructor, List<ParameterInfo> parameterInfos, Object[] vectorArgs, int component) throws IllegalAccessException, InvocationTargetException, InstantiationException {

        Object[] args = new Object[parameterInfos.size()];
        for (int i = 0; i < parameterInfos.size(); i++) {
            ParameterInfo parameterInfo = parameterInfos.get(i);
            Value argValue = (Value) vectorArgs[i];

            if (argValue == null) {
                if (!parameterInfo.optional()) {
                    throw new IllegalArgumentException("Required parameter " + parameterInfo.name() + " not including in vector arguments");
                }
            } else {

                Class argValueClass = argValue.value().getClass();
                //fullargs.put(parameterInfo.name(), argValue);

                if (parameterInfo.type().isAssignableFrom(argValueClass)) {
                    // direct type match
                    args[i] = vectorArgs[i];
                } else if (argValueClass.isArray() && parameterInfo.type().isAssignableFrom(argValueClass.getComponentType())) {
                    // vector match
                    args[i] = new Value(null, Array.get(argValue.value(), component), null);
                }
            }
        }

        return (Generator)constructor.newInstance(args);
    }

    /**
     * @param params    the vectorized parameters
     * @param baseTypes the base types of the component generator
     * @param component the component to produce parameters for
     * @return the parameters for the base distribution for the i'th component.
     */
    public static Map<String, Value<?>> getComponentParameters(Map<String, Value> params, Map<String, Class> baseTypes, int component) {
        int size = 1;
        Map<String, Value<?>> componentParams = new TreeMap<>();
        for (Map.Entry<String, Value> entry : params.entrySet()) {
            String name = entry.getKey();
            Value v = entry.getValue();
            if (isArrayOfType(v, baseTypes.get(name))) {
                int vectorSize = Array.getLength(v.value());
                if (size == 1) {
                    size = vectorSize;
                } else if (size != vectorSize) {
                    throw new RuntimeException("Vector sizes do not match!");
                }
                Object input = Array.get(v.value(), component);
                componentParams.put(name, new Value(v.getId() + "." + component, input));
            } else {
                componentParams.put(name, v);
            }
        }
        return componentParams;
    }

    /**
     * @param maybeArray
     * @param ofType
     * @return true if the given value is an array that can be populated by the given component type
     */
    public static boolean isArrayOfType(Value maybeArray, Class ofType) {

        if (maybeArray.value().getClass().isArray()) {
            Class componentClass = maybeArray.value().getClass().getComponentType();
            return componentClass.isAssignableFrom(ofType);
        }
        return false;
    }
}
