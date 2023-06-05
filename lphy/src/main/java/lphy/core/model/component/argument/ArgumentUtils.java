package lphy.core.model.component.argument;

import lphy.core.model.component.ReflectUtils;
import lphy.core.model.component.Value;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ArgumentUtils {


    public static List<ParameterInfo> getParameterInfo(Class<?> c, int constructorIndex) {
        return getParameterInfo(c.getConstructors()[constructorIndex]);
    }

    public static List<ParameterInfo> getParameterInfo(Constructor constructor) {
        ArrayList<ParameterInfo> pInfo = new ArrayList<>();

        Annotation[][] annotations = constructor.getParameterAnnotations();
        for (int i = 0; i < annotations.length; i++) {
            Annotation[] annotations1 = annotations[i];
            for (Annotation annotation : annotations1) {
                if (annotation instanceof ParameterInfo) {
                    pInfo.add((ParameterInfo) annotation);
                }
            }
        }

        return pInfo;
    }

    /**
     * @param constructor
     * @return an array of the generic types of arguments of the given constructor.
     */
    public static Class[] getParameterTypes(Constructor constructor) {
        Type[] generics = constructor.getGenericParameterTypes();
        Class[] types = new Class[generics.length];
        for (int i = 0; i < generics.length; i++) {
            types[i] = ReflectUtils.getClass(generics[i]);
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

    public static String getArgumentCodeString(Map.Entry<String, Value> entry) {
        return getArgumentCodeString(entry.getKey(), entry.getValue());
    }

    public static String getArgumentCodeString(String name, Value value) {
        String prefix = "";
        if (!ExpressionUtils.isInteger(name)) {
            prefix = name + "=";
        }

        if (value == null) {
            throw new RuntimeException("Value of " + name + " is null!");
        }

        if (value.isAnonymous()) return prefix + value.codeString();
        return prefix + value.getId();
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

}
