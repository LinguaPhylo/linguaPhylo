package lphy.core.parser.argument;

import lphy.core.model.GeneratorUtils;
import lphy.core.model.Value;
import lphy.core.model.annotation.ParameterInfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ArgumentUtils {

    /**
     * @param constructor
     * @return an array of the generic types of arguments of the given constructor.
     */
    public static Class[] getParameterTypes(Constructor constructor) {
        Type[] generics = constructor.getGenericParameterTypes();
        Class[] types = new Class[generics.length];
        for (int i = 0; i < generics.length; i++) {
            types[i] = GeneratorUtils.getClass(generics[i]);
        }
        return types;
    }

    public static List<Argument> getArguments(Class<?> c, int constructorIndex) {
        return getArguments(c.getConstructors()[constructorIndex]);
    }

    public static List<Argument> getArguments(Constructor constructor) {

        List<Argument> arguments = new ArrayList<>();

        Annotation[][] annotations = constructor.getParameterAnnotations();
        Class<?>[] parameterTypes = getParameterTypes(constructor);

        // top for loop
        for (int i = 0; i < annotations.length; i++) {
            Annotation[] annotations1 = annotations[i];
            for (Annotation annotation : annotations1) {
                if (annotation instanceof ParameterInfo) {
                    arguments.add(new Argument(i, (ParameterInfo) annotation, parameterTypes[i]));
                }
            }
        }
        return arguments;
    }

    /**
     * @param arguments the arguments of the Generator
     * @param initArgs the parallel array of initial arguments that match the arguments of the generator - may contain nulls where no name match
     * @param params the map of all params, may be more than non-null in initial arguments
     * @param lightweight
     * @return       if the types of values parsed from lphy script are matching
     *               the required types from the implemented function or distribution.
     */
    public static boolean matchingParameterTypes(List<Argument> arguments, Object[] initArgs, Map<String, Value> params, boolean lightweight) {

        int count = 0;
        for (int i = 0; i < arguments.size(); i++) {
            Argument argumentInfo = arguments.get(i);
            Object arg = initArgs[i];

            if (arg != null) {
                Class parameterType = argumentInfo.type;
                Class valueType = lightweight ? arg.getClass() : ((Value) arg).value().getClass();

                // this code only checks whether types are matching, the cast has to be inside the function or distribution.
                // check if the type is an array, e.g. TimeTreeNode[].
                if (parameterType.isArray()) {
                    if (!valueType.isArray())
                        return false; // invalid, if given type is array, but required parameter type is not array

                    Object[] arr = lightweight ? (Object[]) arg : (Object[]) ((Value) arg).value();
                    Class paramCompType = parameterType.getComponentType();
                    for (Object o : arr) {
                        // this make "Value<Object[]> clades" works, when the given value type is TimeTreeNode[].
                        if (! paramCompType.isAssignableFrom(o.getClass()) ) {
                            return false; // check if each component type matches
                        }
                    }
                } else if (!parameterType.isAssignableFrom(valueType)) {
                    return false;
                }

                // if the value type is matched to one argument, then plus 1
                count += 1;

            } else {
                if (!argumentInfo.optional)
                    return false;
            }
        }
        return params == null || count == params.size();
    }

    public static List<ParameterInfo> getAllParameterInfo(Class c) {
        ArrayList<ParameterInfo> pInfo = new ArrayList<>();
        for (Constructor constructor : c.getConstructors()) {
            pInfo.addAll(GeneratorUtils.getParameterInfo(constructor));
        }
        return pInfo;
    }
}
