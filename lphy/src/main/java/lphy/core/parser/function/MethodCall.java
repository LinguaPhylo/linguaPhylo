package lphy.core.parser.function;

import lphy.core.model.*;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.MethodInfo;
import lphy.core.parser.graphicalmodel.ValueCreator;
import lphy.core.vectorization.CompoundVectorValue;
import lphy.core.vectorization.VectorMatchUtils;
import lphy.core.vectorization.VectorizedFunction;
import lphy.core.vectorization.operation.ElementsAt;
import lphy.core.vectorization.operation.Slice;
import lphy.phylospec.types.Vector;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * This class handles all method calls, including "vectorization by calling object"
 * and "vectorization by arguments".
 *
 * <b>Note:</b> all method calls must keep the immutable rule in LPhy,
 * so any setter or similar method is not allowed.
 * The implementation must be a function to create a new Value from the old one,
 * if the value change is proposed.
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
                    method = VectorMatchUtils.getVectorMatch(methodName, componentClass, paramTypes);
                    if (method != null) {
                        vectorizedObject = true;
                        vectorizedArguments = true;
                    }
                }
            }

            // if unsuccessful so far
            if (method == null) {
                // check for vectorized argument match
                method = VectorMatchUtils.getVectorMatch(methodName, value, arguments);
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

    private MethodInfo getMethodInfo(Method method) {

        MethodInfo methodInfo = method.getAnnotation(MethodInfo.class);

        if (methodInfo != null) return methodInfo;

        // TODO should we check super classes here?
        return null;
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
                    resultValues.add(ValueCreator.createValue(method.invoke(((Vector)value).getComponent(i), args), this));
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
            return ValueCreator.createValue(obj, this);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Value<?> vectorApply(Object[] args) throws IllegalAccessException, InvocationTargetException {
        int vectorSize = getVectorSize(args);

        boolean[] isVector = VectorMatchUtils.isVectorMatch(method, Arrays.stream(args).map(Object::getClass).toArray(Class<?>[]::new));

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
            returnValues.add(ValueCreator.createValue(method.invoke(value.value(), callArgs), this));
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

        String id = value.getId();

        if (value.isAnonymous()) {
            if (value.getGenerator() instanceof ElementsAt || value.getGenerator() instanceof Slice) {
                id = value.getGenerator().codeString();
            }
        }

        builder.append(id + getName());
        builder.append("(");

        if (arguments.length > 0) {
            builder.append(argumentString(arguments[0]));
        }
        for (int i = 1; i < arguments.length; i++) {
            builder.append(", ");
            builder.append(argumentString(arguments[i]));
        }

        builder.append(")");
        return builder.toString();
    }

    private String argumentString(Value value) {
        if (value.isAnonymous()) return value.codeString();
        return value.getId();
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
        if (paramName.equals(objectParamName)) return NarrativeTypeNameUtils.getTypeName(value);
        throw new RuntimeException("Expected either " + argParamName + "[0-9] or " + objectParamName + ", but got " + paramName);
    }

    public static TreeMap<String, MethodInfo> getMethodCalls(Class<?> typeCls) {
        TreeMap<String, MethodInfo> methodInfoTreeMap = new TreeMap<>();
        for (Method method : typeCls.getMethods()) {
            MethodInfo methodInfo = method.getAnnotation(MethodInfo.class);
            if (methodInfo != null) {
                methodInfoTreeMap.put(method.getName(), methodInfo);
            }
        }
        return methodInfoTreeMap;
    }

    /**
     * @param methodInfoTreeMap from {@link #getMethodCalls(Class)}
     * @return  the 1st detected {@link GeneratorCategory} which is not NONE,
     *          otherwise return NONE.
     */
    public static GeneratorCategory getCategory(TreeMap<String, MethodInfo>  methodInfoTreeMap) {
        for (Map.Entry<String,MethodInfo> methodInfoEntry : methodInfoTreeMap.entrySet()) {
            MethodInfo methodInfo = methodInfoEntry.getValue();
            if (methodInfo.category() != GeneratorCategory.NONE)
                return methodInfo.category();
        }
        return GeneratorCategory.NONE;
    }

    /**
     * @param methodInfoTreeMap  from {@link #getMethodCalls(Class)}
     * @return  all examples concatenated in 1 array
     */
    public static String[] getExamples(TreeMap<String, MethodInfo>  methodInfoTreeMap) {
        if (methodInfoTreeMap.size() < 1) return null;

        Set<String> exSet = new HashSet<>();
        for (Map.Entry<String,MethodInfo> methodInfoEntry : methodInfoTreeMap.entrySet()) {
            MethodInfo methodInfo = methodInfoEntry.getValue();
            if (Objects.requireNonNull(methodInfo.examples()).length > 0)
                exSet.addAll(Arrays.stream(methodInfo.examples()).toList());
        }
        return exSet.toArray(String[]::new);
    }

    public static String getHtmlDoc(String name, TreeMap<String, MethodInfo>  methodInfoTreeMap,
                                    String[] examples) {
        // main content
        StringBuilder html = new StringBuilder("<html><h2>");
        html.append(name).append("</h2>");

        if (methodInfoTreeMap.size() > 0) {
            html.append("<h3>Methods:</h3>").append("<ul>");

            for (Map.Entry<String,MethodInfo> methodInfoEntry : methodInfoTreeMap.entrySet()) {
                html.append("<li>").append(" <b>").append(methodInfoEntry.getKey()).append("</b>")
                        .append(" - <font color=\"#808080\">")
                        .append(methodInfoEntry.getValue().description()).append("</font></li>");
            }
            html.append("</ul>");

            if (examples != null && examples.length > 0) {
                html.append("<h3>Examples</h3>");
                for (int i = 0; i < examples.length; i++) {
                    String ex = examples[i];
                    // add hyperlink
                    if (ex.startsWith("http"))
                        ex = "&nbsp;<a href=\"" + ex + "\">" + ex + "</a>";
                    html.append(ex);
                    if (i < examples.length - 1)
                        html.append(", ");
                }
            }
        }
        html.append("</html>");
        return html.toString();
    }
}
