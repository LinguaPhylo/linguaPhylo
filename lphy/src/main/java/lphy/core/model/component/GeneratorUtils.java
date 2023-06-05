package lphy.core.model.component;

import lphy.core.model.annotation.Citation;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.component.argument.ArgumentUtils;
import lphy.core.model.component.argument.ParameterInfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class GeneratorUtils {

    // getGeneratorMarkdown(...) is moved to lphy.doc.GeneratorMarkdown

    public static Citation getCitation(Class<?> c) {
        Annotation[] annotations = c.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation instanceof Citation) {
                return (Citation) annotation;
            }
        }
        return null;
    }

    public static List<ParameterInfo> getAllParameterInfo(Class c) {
        ArrayList<ParameterInfo> pInfo = new ArrayList<>();
        for (Constructor constructor : c.getConstructors()) {
            pInfo.addAll(ArgumentUtils.getParameterInfo(constructor));
        }
        return pInfo;
    }

    public static String getSignature(Class<?> aClass) {

        List<ParameterInfo> pInfo = ArgumentUtils.getParameterInfo(aClass, 0);

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
                return ReflectUtils.getGenericReturnType(method);
            }
        }
        if (GenerativeDistribution.class.isAssignableFrom(genClass)) {
            try {
                Method method = genClass.getMethod("sample");
                return ReflectUtils.getGenericReturnType(method);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        } else if (DeterministicFunction.class.isAssignableFrom(genClass)) {
            {
                try {
                    Method method = genClass.getMethod("apply");
                    return ReflectUtils.getGenericReturnType(method);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
        }
        return Object.class;
    }
}
