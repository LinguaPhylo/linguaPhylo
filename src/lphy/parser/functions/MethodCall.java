package lphy.parser.functions;

import lphy.core.functions.VectorizedFunction;
import lphy.core.narrative.Narrative;
import lphy.graphicalModel.Vector;
import lphy.graphicalModel.*;
import lphy.graphicalModel.types.CompoundVectorValue;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static org.apache.commons.lang3.BooleanUtils.or;

/**
 * This class handles all method calls, including "vectorization by calling object" and "vectorization by arguments".
 */
public class MethodCall extends DeterministicFunction {

    public static final String objectParamName = "object";
    public static final String argParamName = "arg";

    // the object that the method is being called on
    Value<?> value;

    // the method name of the method being called
    final String methodName;

    // the method info for the method
    MethodInfo methodInfo;

    // the arguments of the method call
    Value<?>[] arguments;

    // the types of the passed arguments
    Class<?>[] paramTypes;

    // the class of the object that the method is being called on
    Class<?> c;

    // the method being called.
    Method method = null;

    // true if the arguments are a vector match for this method
    boolean vectorizedArguments = false;

    // true if the method is called on the components of a VectorValue
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

            if (value instanceof Vector) {
                // check for vectorized object match

                Class<?> componentClass = ((Vector<?>)value).getComponentType();

                // check for exact match within vectorized object
                try {
                    method = componentClass.getMethod(methodName, paramTypes);
                    vectorizedObject = true;
                } catch (NoSuchMethodException nsme2) {
                    // check for doubly vectorized
                    method = getVectorMatch(methodName, componentClass, paramTypes);
                    if (method != null) {
                        vectorizedObject = true;
                        vectorizedArguments = true;
                    }
                }
            }

            // if unsuccessful so far
            if (method == null) {
                // check for vectorized argument match
                method = getVectorMatch(methodName, value, arguments);
                if (method != null) vectorizedArguments = true;
            }

            if (method == null) throw nsme;
        }

        methodInfo = getMethodInfo(method);

        if (methodInfo == null) {
            throw new IllegalArgumentException("This method is not permitted to be passed through! " +
                    "Methods must have MethodInfo annotation to allow pass through to LPhy.");
        }

        setInput(objectParamName, value);
        for (int i = 0; i < arguments.length; i++) {
            setInput(argParamName + i, arguments[i]);
        }
    }

    public static boolean isMethodCall(Object o) {
        return o instanceof MethodCall || (o instanceof VectorizedFunction && ((VectorizedFunction)o).getComponentFunction(0) instanceof MethodCall);
    }

//    /**
//     * @param value
//     * @return the narrative name for the given value, being a parameter of this generator.
//     */
//    public String getNarrativeName(Value value) {
//        String name = getParamName(value);
//        List<ParameterInfo> parameterInfos = getParameterInfo(0);
//        for (ParameterInfo parameterInfo : parameterInfos) {
//            if (parameterInfo.name().equals(name)) {
//                if (parameterInfo.narrativeName().length() > 0) {
//                    return parameterInfo.narrativeName();
//                }
//            }
//        }
//        return name;
//    }

    private MethodInfo getMethodInfo(Method method) {

        MethodInfo methodInfo = method.getAnnotation(MethodInfo.class);

        if (methodInfo != null) return methodInfo;

        // TODO should we check super classes here?
        return null;
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

    /**
     * @param method the method to check
     * @param paramTypes the param types to check for a vector match
     * @return a boolean array of length equal to paramTypes array, with true of each vector match, false otherwise.
     */
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

    /**
     * @param methodParamType the class of a method argument
     * @param paramType the class of a potential vector match
     * @return true if paramType is a vector match for methodParamType (i.e. paramType is an array and has components of a class assignable to methodParamType.
     */
    private static boolean isVectorMatch(Class<?> methodParamType, Class<?> paramType) {
        return (paramType.isArray() && methodParamType.isAssignableFrom(paramType.getComponentType()));
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
                put(argParamName + i, arguments[i]);
            }
        }};
    }

    public void setParam(String paramName, Value param) {
        if (paramName.equals(objectParamName)) {
            value = param;
        } else if (paramName.startsWith(argParamName)) {
            int index = Integer.parseInt(paramName.substring(argParamName.length()));
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

                List<Value> resultValues = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    resultValues.add(ValueUtils.createValue(method.invoke(((Vector)value).getComponent(i), args), this));
                }
                return new CompoundVectorValue(null, resultValues, this);
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

        List<Value> returnValues = new ArrayList<>();

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
            returnValues.add(ValueUtils.createValue(method.invoke(value.value(), callArgs), this));
        }

        return new CompoundVectorValue<>(null,returnValues, this);
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

    public String getInferenceNarrative(Value value, boolean unique, Narrative narrative) {

        String narrativeName = getNarrativeName();
        if (vectorizedArguments || vectorizedObject) {
            narrativeName = NarrativeUtils.pluralize(narrativeName);
        }

        StringBuilder builder = new StringBuilder();
        builder.append(NarrativeUtils.getValueClause(value, unique, narrative));
        builder.append(vectorizedArguments || vectorizedObject ? " are " : " is ");
        builder.append(NarrativeUtils.getDefiniteArticle(narrativeName, true));
        builder.append(" ");
        builder.append(narrativeName);

        if (arguments.length > 0) {
            builder.append(" ");
            int count = 0;
            for (Value arg : arguments) {
                if (count > 0) {
                    if (count == arguments.length-1) {
                        builder.append(" and ");
                    } else {
                        builder.append(", ");
                    }
                }
                builder.append(narrative.text(arg.toString()));
                count += 1;
            }
        }

        builder.append(" of " + (vectorizedObject ? " each element in " : ""));
        builder.append(NarrativeUtils.getValueClause(this.value, vectorizedObject, true, vectorizedObject, narrative));

        builder.append(".");
        return builder.toString();
    }


    public String codeString() {

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

    public String getNarrativeName() {
        String narrativeName = methodInfo.narrativeName();
        if (narrativeName.length() > 0) return narrativeName;
        return getName();
    }

    public String getTypeName() {
        if (vectorizedArguments || vectorizedObject) return "vector of " + NarrativeUtils.pluralize(method.getReturnType().getSimpleName());
        return method.getReturnType().getSimpleName();
    }

    /**
     * @param value
     * @return the narrative name for the given value, being a parameter of this generator.
     */
    public String getNarrativeName(Value value) {
        String paramName = getParamName(value);
        if (paramName.startsWith(argParamName)) {
            int argumentIndex = Integer.parseInt(paramName.substring(argParamName.length()));
            return "argument " + argumentIndex;
        }
        if (paramName.equals(objectParamName)) return NarrativeUtils.getTypeName(value);
        throw new RuntimeException("Expected either " + argParamName + "[0-9] or " + objectParamName + ", but got " + paramName);
    }
}
