package lphy.parser.functions;

import lphy.graphicalModel.*;
import lphy.graphicalModel.Vector;
import lphy.graphicalModel.types.VectorValue;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static org.apache.commons.lang3.BooleanUtils.or;

public class MethodCall extends DeterministicFunction {

    public static final String objectParamName = "object";

    Value<?> value;
    final String methodName;
    MethodInfo methodInfo;
    Value<?>[] arguments;
    Class<?>[] paramTypes;
    Class c;
    Method method;
    boolean vectorizedArguments = false;
    boolean vectorizedObject = false;

    public MethodCall(String methodName, Value<?> value, Value<?>[] arguments) throws NoSuchMethodException {
        this.value = value;
        this.methodName = methodName;
        this.arguments = arguments;

        paramTypes = new Class[arguments.length];

        for (int i = 0; i < paramTypes.length; i++) {
            paramTypes[i] = arguments[i].value().getClass();
        }

        c = value.value().getClass();

        try {
            // check for exact match
            method = c.getMethod(methodName, paramTypes);
        } catch (NoSuchMethodException nsme) {

            // check for vectorized object match
            vectorizedObject = value instanceof Vector;

            if (vectorizedObject) {

                Class componentClass = ((Vector)value).getComponentType();

                // check for exact match within vectorized object
                try {
                    method = componentClass.getMethod(methodName, paramTypes);
                } catch (NoSuchMethodException nsme2) {
                    // check for doubly vectorized
                    method = getVectorMatch(methodName, componentClass, paramTypes);
                    if (method != null) vectorizedArguments = true;
                }
            } else {
                // check for vectorized argument match
                method = getVectorMatch(methodName, value, arguments);
                if (method != null) vectorizedArguments = true;
            }

            if (method == null) throw nsme;
        }

        methodInfo = method.getAnnotation(MethodInfo.class);

        if (methodInfo == null) {
            throw new IllegalArgumentException("This method is not permitted pass through! Must have MethodInfo annotation to allow pass through.");
        }

        setInput(objectParamName, value);
        for (int i = 0; i < arguments.length; i++) {
            setInput("arg" + i, arguments[i]);
        }
    }

    /**
     * @param methodName the method name
     * @param c the class of object on which the method is sought
     * @param paramTypes the param types that are vectorized version of actual parameter types
     * @return the first matching method, or null if none.
     */
    public Method getVectorMatch(String methodName, Class c, Class[] paramTypes) {
        // check for vectorized arguments match
        for (Method method : c.getMethods()) {
            if (method.getName().equals(methodName)) {
                if (or(isVectorMatch(method, paramTypes))) {
                    return method;
                }
            }
        }
        return null;
    }

    /**
     * @param methodName the method that is a vector match for the given arguments, or null if no match found.
     * @param value the value for which a method call is attempted.
     * @param arguments the arguments of the method call.
     * @return
     */
    public Method getVectorMatch(String methodName, Value<?> value, Value<?>[] arguments) {
        Class<?>[] paramTypes = new Class[arguments.length];

        for (int i = 0; i < paramTypes.length; i++) {
            paramTypes[i] = arguments[i].value().getClass();
        }

        Class c = value.value().getClass();

        try {
            // check for exact match
            Method method = c.getMethod(methodName, paramTypes);
        } catch (NoSuchMethodException nsme) {
            // check for vectorized match
            for (Method method : c.getMethods()) {
                if (method.getName().equals(methodName)) {
                    if (or(isVectorMatch(method, paramTypes))) {
                        return method;
                    }
                }
            }
            return null;
        }
        return null;
    }

    private static boolean[] isVectorMatch(Method method, Class<?>[] paramTypes) {
        Class<?>[] methodParamTypes = method.getParameterTypes();

        if (methodParamTypes.length == paramTypes.length) {
            boolean[] vectorMatch = new boolean[paramTypes.length];
            for (int i = 0; i < methodParamTypes.length; i++) {
                vectorMatch[i] = isVectorMatch(methodParamTypes[i],paramTypes[i]);
            }
            return vectorMatch;
        }
        throw new IllegalArgumentException("paramTypes array must be same length as method param types array!");
    }

    private static boolean isVectorMatch(Class<?> methodParamType, Class<?> paramType) {
        return methodParamType.isAssignableFrom(paramType) || (paramType.isArray() && methodParamType.isAssignableFrom(paramType.getComponentType()));
    }

    @Override
    public String getRichDescription(int index) {
        String html = "<html><h3>" + c.getSimpleName() + getName() + " method call</h3> <ul>";
        html += "<li>" + methodInfo.description();
        html += "</ul></html>";
        return html;
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

        if (vectorizedArguments && vectorizedObject) throw new UnsupportedOperationException("Doubly vectorized method calls not supported!");

        Object[] args = new Object[arguments.length];
        for (int i = 0; i < paramTypes.length; i++) {
            args[i] = arguments[i].value();
        }

        try {

            if (vectorizedObject) {
                int size = ((Vector)value).size();

                Object result = Array.newInstance(method.getReturnType(), size);
                for (int i = 0; i < size; i++) {
                    Array.set(result, i, method.invoke(((Vector)value).getComponent(i), args));
                }
                return new VectorValue(null, result, ((Vector)value).getComponentType(), this);
            }


            if (vectorizedArguments) {
                return vectorApply(args);
            }

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

    private Value<?> vectorApply(Object[] args) throws IllegalAccessException, InvocationTargetException {
        int vectorSize = getVectorSize(args);

        boolean[] isVector = isVectorMatch(method, Arrays.stream(args).map(Object::getClass).toArray(Class<?>[]::new));

        Object[] returnValues = new Object[vectorSize];

        Object[] callArgs = new Object[args.length];

        for (int i = 0; i < vectorSize; i++) {
            for (int j = 0; j < args.length; j++) {
                if (isVector[j]) {
                    // TODO implement recycle rule
                    callArgs[j] = Array.get(args[j],i);
                } else {
                    callArgs[j] = args[j];
                }
            }
            returnValues[i] =  method.invoke(value.value(), callArgs);
        }

        return new VectorValue(null,returnValues, this);
    }

    private int getVectorSize(Object[] args) {
        int size = 1;
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < args.length; i++) {
            if (!parameterTypes[i].isAssignableFrom(args[i].getClass())) {
                if (args[i].getClass().isArray() && parameterTypes[i].isAssignableFrom(args[i].getClass().getComponentType())) {
                    // vector match
                    int vecSize = Array.getLength(args[i]);
                    if (vecSize > size) size = vecSize;
                } else throw new RuntimeException("Argument mismatch!");
            }  // else do nothing, direct match
        }
        return size;
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
