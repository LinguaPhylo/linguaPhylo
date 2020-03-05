package james.graphicalModel;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface Parameterized extends GraphicalModelNode {

    String getName();

    default String getParamName(int paramIndex, int constructorIndex) {
        return getParameterInfo(constructorIndex).get(paramIndex).name();
    }

    default String getParamName(int paramIndex) {
        return getParamName(paramIndex, 0);
    }

    static List<ParameterInfo> getParameterInfo(Class<?> c, int constructorIndex) {
        return getParameterInfo(c.getConstructors()[constructorIndex]);
    }

    default List<ParameterInfo> getParameterInfo(int constructorIndex) {
        return getParameterInfo(this.getClass(), constructorIndex);
    }

    default List<ParameterInfo> getParameterInfo(String methodName) {

        ArrayList<ParameterInfo> pInfo = new ArrayList<>();

        Class<?> classElement = getClass();

        Method[] methods = classElement.getMethods();
        for (Method method : methods) {
            if (method.getName().equals("apply")) {
                Annotation[][] annotations = method.getParameterAnnotations();
                for (Annotation[] annotations1 : annotations) {
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

    static List<ParameterInfo> getParameterInfo(Constructor constructor) {
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

    static List<ParameterInfo> getAllParameterInfo(Class c) {
        ArrayList<ParameterInfo> pInfo = new ArrayList<>();
        for (Constructor constructor : c.getConstructors()) {
            pInfo.addAll(getParameterInfo(constructor));
        }
        return pInfo;
    }

    @Override
    default List<GraphicalModelNode> getInputs() {
        return new ArrayList<>(getParams().values());
    }

    Map<String, Value> getParams();

    void setParam(String paramName, Value<?> value);

    default void setInput(String paramName, Value<?> value) {
        setParam(paramName, value);
        value.addOutput(this);
    }

    default String getParamName(Value<?> value) {
        Map<String, Value> params = getParams();
        for (String key : params.keySet()) {
            if (params.get(key) == value) return key;
        }
        return null;
    }

    String codeString();

    static String getArgumentCodeString(Map.Entry<String, Value> entry) {
        String prefix = "";
        if (!Utils.isInteger(entry.getKey())) {
            prefix = entry.getKey() + "=";
        }
        if (entry.getValue().isAnonymous()) return prefix + entry.getValue().codeString();
        return prefix + entry.getValue().getId();
    }

    static String getArgumentValue(Map.Entry<String, Value> entry) {
        if (entry.getValue().isAnonymous()) return entry.getValue().codeString();
        return entry.getValue().getId();
    }

    /**
     * @return true if any of the parameters are random variables,
     * or are themselves that result of a function with random parameters as arguments.
     */
    default boolean hasRandomParameters() {
        for (Value<?> v : getParams().values()) {
            if (v.isRandom()) return true;
        }
        return false;
    }
}
