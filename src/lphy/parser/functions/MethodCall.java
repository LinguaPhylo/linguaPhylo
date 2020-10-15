package lphy.parser.functions;

import lphy.graphicalModel.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class MethodCall extends DeterministicFunction {

    public static final String objectParamName = "object";

    Value<?> value;
    final String methodName;
    MethodInfo methodInfo;
    Value<?>[] arguments;
    Class<?>[] paramTypes;
    Class c;
    Method method;

    public MethodCall(String methodName, Value<?> value, Value<?>[] arguments) throws NoSuchMethodException {
        this.value = value;
        this.methodName = methodName;
        this.arguments = arguments;

        paramTypes = new Class[arguments.length];

        for (int i = 0; i < paramTypes.length; i++) {
            paramTypes[i] = arguments[i].value().getClass();
        }

        c = value.value().getClass();
        method = c.getMethod(methodName, paramTypes);

        methodInfo =  method.getAnnotation(MethodInfo.class);

        if (methodInfo == null) {
            throw new IllegalArgumentException("This method is not permitted pass through! Must have MethodInfo annotation to allow pass through.");
        }

        setInput(objectParamName, value);
        for (int i = 0; i < arguments.length; i++) {
            setInput("arg" + i, arguments[i]);
        }
    }

    public String getName() {
        return "." + methodName;
    }

    public String getDescription() {
        return methodInfo.description();
    }

    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(objectParamName, value);
            for (int i = 0; i < arguments.length; i++) {
                put("arg" + i, arguments[i]);
            }
        }};
    }

    public void setParam(String paramName, Value param) {
        if (paramName.equals(objectParamName)) {
            value = param;
        } else if (paramName.startsWith("arg")) {
            int index = Integer.parseInt(paramName.substring(3));
            arguments[index] = param;
        } else throw new IllegalArgumentException("Param name " + paramName + " not recognised!");
    }

    public Value<?> apply() {

        Object[] args = new Object[arguments.length];
        for (int i = 0; i < paramTypes.length; i++) {
            args[i] = arguments[i].value();
        }

        try {
            Object obj = method.invoke(value.value(), args);

            // unwrap
            if (obj instanceof Value) {
                obj = ((Value) obj).value();
            }
            return ValueUtils.createValue(obj, this);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String codeString() {
        Map<String, Value> map = getParams();

        StringBuilder builder = new StringBuilder();
        builder.append(value.getId() + getName());
        builder.append("(");

        if (arguments.length > 0) {
            builder.append(arguments[0].codeString());
        }
        for (int i = 1; i < arguments.length; i++) {
            builder.append(", ");
            builder.append(arguments[i].codeString());
        }

        builder.append(")");
        return builder.toString();
    }
}
