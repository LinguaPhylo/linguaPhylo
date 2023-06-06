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
     * @return
     */
    public static boolean matchingParameterTypes(List<Argument> arguments, Object[] initArgs, Map<String, Value> params, boolean lightweight) {

        int count = 0;
        for (int i = 0; i < arguments.size(); i++) {
            Argument argumentInfo = arguments.get(i);
            Object arg = initArgs[i];

            if (arg != null) {
                Class parameterType = argumentInfo.type;
                Class valueType = lightweight ? arg.getClass() : ((Value) arg).value().getClass();

                if (!parameterType.isAssignableFrom(valueType))
                    return false;
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
