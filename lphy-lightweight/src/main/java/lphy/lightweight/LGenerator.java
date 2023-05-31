package lphy.base.lightweight;

import lphy.core.graphicalmodel.components.Argument;
import lphy.core.graphicalmodel.components.ParameterInfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A lightweight generator interface that involves only raw values for input and output and a beans style pattern for
 * setting and getting.
 * @param <T> the return type of the generator.
 */
public interface LGenerator<T> {

    /**
     * @return an object generated from this generator.
     */
    T generateRaw();

    /**
     * @return true if this generator produces objects that are a deterministic function of the arguments
     */
    boolean isRandomGenerator();

    default void setArgumentValue(Argument argument, Object val) {
        try {
            Method method = getSetMethod(argument);
            method.invoke(this, val);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param argument the argument to get the set method of
     * @return the method that allows the setting of the given argument
     */
    default Method getSetMethod(Argument argument) {
        try {
            return getClass().getMethod(argument.setMethodName(), argument.type);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    default void setArgumentValue(String name, Object val) {
        try {
            Method method = getClass().getMethod(Argument.setMethodName(name), val.getClass());
            method.invoke(this, val);
        } catch (NoSuchMethodException e) {

            Method[] methods = getClass().getMethods();
            for (Method method : methods) {
                if (method.getName().equals(name)) {
                    try {
                        method.invoke(this,val);
                        break;
                    } catch (InvocationTargetException | IllegalAccessException ignored) {
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    default Object getArgumentValue(Argument argument) {
        try {
            Method method = getClass().getMethod(argument.getMethodName());
            return method.invoke(this);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    default String getName() {
        return getClass().getSimpleName();
    }

    default List<Argument> getArguments() {
        return getArguments(getClass(), 0);
    }


    default Argument getArgumentByName(String name) {
        List<Argument> arguments = getArguments();
        for (Argument arg : arguments) {
            if (arg.name.equals(name)) return arg;
        }
        return null;
    }

    static Class<?> getReturnType(Class<LGenerator> c) {
        try {
            return c.getMethod("generateRaw").getReturnType();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    static List<Argument> getArguments(Class c, int constructorIndex) {

        System.out.println("Getting arguments for " + c);

        Constructor constructor = c.getConstructors()[constructorIndex];

        List<Argument> arguments = new ArrayList<>();

        Annotation[][] annotations = constructor.getParameterAnnotations();
        Class[] parameterTypes = constructor.getParameterTypes();
        System.out.println("  parameter types: " + Arrays.toString(parameterTypes));

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
}
