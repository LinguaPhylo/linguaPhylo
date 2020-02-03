package james.graphicalModel;

import javax.swing.*;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

public abstract class Function<U, V> implements java.util.function.Function<Value<U>, Value<V>>, Parameterized, Viewable {

    public String getName() {
        FunctionInfo fInfo = getFunctionInfo();
        if (fInfo != null) return fInfo.name();
        return getClass().getSimpleName();
    }

    public String getDescription() {
        FunctionInfo fInfo = getFunctionInfo();
        if (fInfo != null) return fInfo.description();
        return "";
    }

    public String getRichDescription() {

        String html = "<html><h3>" + getName() + " function</h3> <ul>";
            html += "<li>" + getDescription();
        html += "</ul></html>";
        return html;
    }

    TreeMap<String, Value> paramMap = new TreeMap<>();

    public Map<String, Value> getParams() {
        return paramMap;
    }

    public Value<V> apply(Value<U> v, String id) {
        Value<V> val = apply(v);
        val.id = id;
        return val;
    }

    public void setParam(String paramName, Value value) {
        paramMap.put(paramName, value);
    }

    public JComponent getViewer() {
        return new JLabel(getRichDescription());
    }

    public String getCallName() {
        StringBuilder builder = new StringBuilder();
        builder.append(getName() + "(" + getParams().values().iterator().next().getId() + ")");
        return builder.toString();
    }

    public FunctionInfo getFunctionInfo() {

        Class classElement = getClass();

        Method[] methods = classElement.getMethods();

        for (Method method : methods) {
            Annotation[] annotations = method.getAnnotations();
            for (Annotation annotation : method.getAnnotations()) {
                if (annotation instanceof FunctionInfo) {
                    return (FunctionInfo)annotation;
                }
            }
        }

        return null;
    }

    public String codeString() {
        Map<String, Value> map = getParams();

        Iterator<Map.Entry<String, Value>> iterator = map.entrySet().iterator();

        Map.Entry<String, Value> entry = iterator.next();

        StringBuilder builder = new StringBuilder();

        builder.append(getName() + "(" + entry.getValue().id);
        while (iterator.hasNext()) {
            entry = iterator.next();
            builder.append(", " + entry.getValue().id);
        }
        builder.append(");");
        return builder.toString();
    }

    public void print(PrintWriter p) {
        p.print(codeString());
    }

}
