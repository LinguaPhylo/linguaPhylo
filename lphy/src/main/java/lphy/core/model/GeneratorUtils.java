package lphy.core.model;

import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;

public class GeneratorUtils {

    // getGeneratorMarkdown(...) is moved to lphy.doc.GeneratorMarkdown

    public static String getSignature(Class<?> aClass) {

        List<ParameterInfo> pInfo = getParameterInfo(aClass, 0);

        StringBuilder builder = new StringBuilder();
        builder.append(getGeneratorName(aClass));
        builder.append("(");
        if (pInfo.size() > 0) {
            builder.append(pInfo.get(0).name());
            for (int i = 1; i < pInfo.size(); i++) {
                builder.append(", ");
                builder.append(pInfo.get(i).name());
            }
        }
        builder.append(")");
        return builder.toString();
    }

    public static String getGeneratorName(Class<?> c) {
        GeneratorInfo ginfo = getGeneratorInfo(c);
        if (ginfo != null) return ginfo.name();
        return c.getSimpleName();
    }

    /**
     * @param c a generator class
     * @return the deprecated alias names declared in its {@link GeneratorInfo#aliases()},
     * or an empty array if none (or no annotation present).
     */
    public static String[] getGeneratorAliases(Class<?> c) {
        GeneratorInfo ginfo = getGeneratorInfo(c);
        if (ginfo != null) return ginfo.aliases();
        return new String[]{};
    }

    public static String[] getGeneratorExamples(Class<?> c) {
        GeneratorInfo ginfo = getGeneratorInfo(c);
        if (ginfo != null) return ginfo.examples();
        return new String[]{};
    }

    public static String getGeneratorDescription(Class<?> c) {
        GeneratorInfo ginfo = getGeneratorInfo(c);
        if (ginfo != null) return ginfo.description();
        return "";
    }

    public static GeneratorInfo getGeneratorInfo(Class<?> c) {

        Method[] methods = c.getMethods();
        for (Method method : methods) {
            for (Annotation annotation : method.getAnnotations()) {
                if (annotation instanceof GeneratorInfo) {
                    return (GeneratorInfo) annotation;
                }
            }
        }
        return null;
    }

    public static Class<?> getReturnType(Class<?> genClass) {
        Method[] methods = genClass.getMethods();

        for (Method method : methods) {
            GeneratorInfo generatorInfo = method.getAnnotation(GeneratorInfo.class);
            if (generatorInfo != null) {
                return getGenericReturnType(method);
            }
        }
        if (GenerativeDistribution.class.isAssignableFrom(genClass)) {
            try {
                Method method = genClass.getMethod("sample");
                return getGenericReturnType(method);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        } else if (DeterministicFunction.class.isAssignableFrom(genClass)) {
            {
                try {
                    Method method = genClass.getMethod("apply");
                    return getGenericReturnType(method);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
        }
        return Object.class;
    }

    public static Class getGenericReturnType(Method method) {
        return getClass(method.getGenericReturnType());
    }

    /**
     * @param type the type signature for a return value or parameter
     * @return the generic class. e.g. if type is {@code lphy.graphicalModel.Value<java.lang.Number>} then this will return java.lang.Number.class.
     * Handles a {@code T} that is itself generic too, e.g. {@code Value<Map<String, Object>>} returns
     * {@code Map.class}, not just {@code Value<T>} where T is a plain, non-generic class.
     */
    public static Class getClass(Type type) {
        if (type instanceof ParameterizedType parameterizedType) {
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            if (actualTypeArguments.length > 0) {
                return resolveRawClass(actualTypeArguments[0]);
            }
        }
        return Object.class;
    }

    /**
     * Resolves a (possibly still generic) {@link Type} down to its raw, erased {@link Class} --
     * e.g. {@code Map<String, Object>} (a {@link ParameterizedType}) resolves to {@code Map.class},
     * and {@code List<String>[]} (a {@link GenericArrayType}, since its component type is itself
     * generic) resolves to {@code List[].class}. A plain array like {@code Double[]} never reaches
     * the {@link GenericArrayType} branch: with no type variable involved, the JVM already
     * represents it as the concrete {@code Class} {@code Double[].class}, caught by the first case.
     */
    private static Class resolveRawClass(Type type) {
        if (type instanceof Class<?> c) {
            return c;
        } else if (type instanceof ParameterizedType parameterizedType) {
            return resolveRawClass(parameterizedType.getRawType());
        } else if (type instanceof GenericArrayType genericArrayType) {
            Class<?> componentClass = resolveRawClass(genericArrayType.getGenericComponentType());
            return Array.newInstance(componentClass, 0).getClass();
        } else if (type instanceof WildcardType wildcardType) {
            Type[] upperBounds = wildcardType.getUpperBounds();
            return upperBounds.length > 0 ? resolveRawClass(upperBounds[0]) : Object.class;
        } else if (type instanceof TypeVariable<?> typeVariable) {
            Type[] bounds = typeVariable.getBounds();
            return bounds.length > 0 ? resolveRawClass(bounds[0]) : Object.class;
        }
        return Object.class;
    }

    public static boolean hasSingleGeneratorOutput(Value value) {
        return value != null && value.getOutputs().size() == 1 && (value.getOutputs().get(0) instanceof Generator);
    }

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
}
