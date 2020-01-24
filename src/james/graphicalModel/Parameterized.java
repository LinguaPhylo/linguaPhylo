package james.graphicalModel;

import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public interface Parameterized {

    default String getParamName(int paramIndex, int constructorIndex) {
        return getParameterInfo(constructorIndex).get(paramIndex).name();
    }

    default String getParamName(int paramIndex) {
        if (this instanceof Function) {
            return getParameterInfo("apply").get(paramIndex).name();
        }
        return getParameterInfo(0).get(paramIndex).name();
    }

    default List<ParameterInfo> getParameterInfo(int constructorIndex) {

        ArrayList<ParameterInfo> pInfo = new ArrayList<>();

        Class classElement = getClass();

        Constructor constructor = classElement.getConstructors()[constructorIndex];
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

    default List<ParameterInfo> getParameterInfo(String methodName) {

        ArrayList<ParameterInfo> pInfo = new ArrayList<>();

        Class classElement = getClass();

        Method[] methods = classElement.getMethods();
        for (int i =0; i < methods.length; i++) {
            if (methods[i].getName().equals("apply")) {
                Annotation[][] annotations = methods[i].getParameterAnnotations();
                for (int j = 0; j < annotations.length; i++) {
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
        return null;
    }

}
