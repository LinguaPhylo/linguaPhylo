package lphy.core.vectorization;

import lphy.core.model.Generator;
import lphy.core.model.Value;
import lphy.core.model.datatype.Vector;
import lphy.core.parser.argument.Argument;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class VectorUtils {

    public static final String INDEX_SEPARATOR = "_";

    public static boolean isVectorizedParameter(String argumentName, Value value, Map<String, Class> baseTypes) {
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

    public static int getVectorSize(List<Argument> argumentInfos, Object[] args) {

        int size = -1;
        for (int i = 0; i < argumentInfos.size(); i++) {
            Argument argumentInfo = argumentInfos.get(i);
            Value argValue = (Value) args[i];
            if (argValue == null) {
                if (!argumentInfo.optional)
                    throw new IllegalArgumentException("Required parameter " + argumentInfo.name + " not including in vector arguments");
            } else {
                Class argValueClass = argValue.value().getClass();

                if (argumentInfo.type.isAssignableFrom(argValueClass)) {
                    // direct type match
                } else if (argValueClass.isArray() && argumentInfo.type.isAssignableFrom(argValueClass.getComponentType())) {
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

    /**
     * @param generator the generator to start with
     * @param size      the size of the replicates value that we are looking for
     * @return the first value in the graphical model above this generator associated with a replicates argument that has the correct size, or null if none found
     */
    public static Value getReplicatesValue(Generator generator, int size) {
        for (Map.Entry<String, Value> entry : (Set<Map.Entry<String, Value>>) generator.getParams().entrySet()) {
            Generator paramGenerator = entry.getValue().getGenerator();
            if (paramGenerator != null) {
                if (paramGenerator instanceof IID) {
                    Value<Integer> replicatesValue = ((IID) (entry.getValue().getGenerator())).getReplicates();
                    if (replicatesValue.value() == size) return replicatesValue;
                } else {
                    Value<Integer> replicatesValue = VectorUtils.getReplicatesValue(paramGenerator, size);
                    if (replicatesValue != null) return replicatesValue;
                }
            }
        }
        return null;
    }


    public static List<Generator> getComponentGenerators(Constructor constructor, List<Argument> argumentInfos, Object[] vectorArgs) throws IllegalAccessException, InvocationTargetException, InstantiationException {

        int size = getVectorSize(argumentInfos, vectorArgs);
        List<Generator> generators = new ArrayList<>(size);
        for (int component = 0; component < size; component++) {
            generators.add(getComponentGenerator(constructor, argumentInfos, vectorArgs, component));
        }
        return generators;
    }

    /**
     * @param constructor   the constructor of the generator
     * @param argumentInfos the argument info provided
     * @param parentArgs    the arguments provided, including replicates argument
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     */
    public static Generator getElementGenerator(Constructor constructor, List<Argument> argumentInfos, Object[] parentArgs) throws IllegalAccessException, InvocationTargetException, InstantiationException {

        Object[] args = new Object[argumentInfos.size()];
        for (int i = 0; i < argumentInfos.size(); i++) {
            Argument argumentInfo = argumentInfos.get(i);
            Value argValue = (Value) parentArgs[i];

            if (argValue == null) {
                if (!argumentInfo.optional) {
                    throw new IllegalArgumentException("Required parameter " + argumentInfo.name + " not including in arguments");
                }
            } else {

                Class argValueClass = argValue.value().getClass();

                if (argumentInfo.type.isAssignableFrom(argValueClass)) {
                    // direct type match
                    args[i] = parentArgs[i];
                }
            }
        }

        return (Generator) constructor.newInstance(args);
    }


    public static Generator getComponentGenerator(Constructor constructor, List<Argument> argumentInfos, Object[] vectorArgs, int component) throws IllegalAccessException, InvocationTargetException, InstantiationException {

        Object[] args = new Object[argumentInfos.size()];
        for (int i = 0; i < argumentInfos.size(); i++) {
            Argument argumentInfo = argumentInfos.get(i);
            Value argValue = (Value) vectorArgs[i];

            if (argValue == null) {
                if (!argumentInfo.optional) {
                    throw new IllegalArgumentException("Required parameter " + argumentInfo.name + " not including in vector arguments");
                }
            } else {

                Class argValueClass = argValue.value().getClass();
                //fullargs.put(parameterInfo.name(), argValue);

                if (argumentInfo.type.isAssignableFrom(argValueClass)) {
                    // direct type match
                    args[i] = vectorArgs[i];
                } else if (argValueClass.isArray() && argumentInfo.type.isAssignableFrom(argValueClass.getComponentType())) {
                    // vector match

                    Object array = argValue.value();
                    int length = Array.getLength(array);
                    if (Array.getLength(array) <= component) {
                        throw new RuntimeException("Array " + array + " is length " + length + " but attempting to access element " + component);
                    }

                    if (argValue instanceof CompoundVector) {
                        args[i] = ((CompoundVector) argValue).getComponentValue(component);
                    } else {
                        // TODO should this be SliceValue?
                        args[i] = new Value(argValue.isAnonymous() ? null : argValue.getId() + VectorUtils.INDEX_SEPARATOR + component, Array.get(array, component));
                    }
                }
            }
        }

        return (Generator) constructor.newInstance(args);
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
                componentParams.put(name, new Value(v.getId() + VectorUtils.INDEX_SEPARATOR + component, input));
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

    public static Object getElement(Value value, int i) {
        if (value instanceof Vector) {
            return ((VectorValue)value).getComponent(i);
        }
        if (value.value().getClass().isArray()) {
            return Array.get(value.value(), i);
        }
        throw new IllegalArgumentException("Expected a Vector or array!");
    }
}
