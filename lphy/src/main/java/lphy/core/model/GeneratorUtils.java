package lphy.core.model;

import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
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
     * @return the generic class. e.g. if type is {@code lphy.graphicalModel.Value<java.lang.Number>} then this will return java.lang.Number.class
     */
    public static Class getClass(Type type) {
        Class typeClass;
        String name = type.getTypeName();

        if (name.indexOf('<') >= 0) {

            String typeClassString = name.substring(name.indexOf('<') + 1, name.lastIndexOf('>'));

            if (typeClassString.endsWith("[]")) {
                typeClassString = "L" + typeClassString;

                while (typeClassString.endsWith("[]")) {
                    typeClassString = "[" + typeClassString.substring(0, typeClassString.lastIndexOf('['));
                }
                typeClassString = typeClassString + ";";
            }

            try {
                typeClass = Class.forName(typeClassString);
            } catch (ClassNotFoundException e) {
                // TODO need to understand these cases better!
                typeClass = Object.class;
            }
        } else typeClass = Object.class;
        return typeClass;
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
