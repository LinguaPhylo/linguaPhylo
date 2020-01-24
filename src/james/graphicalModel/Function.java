package james.graphicalModel;

import javax.swing.*;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public abstract class Function<U, V> implements java.util.function.Function<Value<U>, Value<V>>, Parameterized, Viewable {

    public abstract String getName();

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
        return new JLabel(getName());
    }

    public String getCallName() {
        StringBuilder builder = new StringBuilder();
        builder.append(getName() + "(" + getParams().values().iterator().next().getId() + ")");
        return builder.toString();
    }

    public void print(PrintWriter p) {
        Map<String, Value> map = getParams();

        Iterator<Map.Entry<String, Value>> iterator = map.entrySet().iterator();

        Map.Entry<String, Value> entry = iterator.next();

        p.print(getName() + "(" + entry.getValue().id);
        while (iterator.hasNext()) {
            entry = iterator.next();
            p.print(", " + entry.getValue().id);
        }
        p.print(");");
    }
}
