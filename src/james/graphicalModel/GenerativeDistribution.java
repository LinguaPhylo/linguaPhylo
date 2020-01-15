package james.graphicalModel;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.List;

/**
 * Created by adru001 on 17/12/19.
 */
public interface GenerativeDistribution<T> {

    RandomVariable<T> sample();

    default RandomVariable<T> sample(String id) {
        RandomVariable<T> v = sample();
        v.id = id;
        return v;
    }

    default double density(T t) {
        return Math.exp(logDensity(t));
    }

    default double logDensity(T t) {
        return Math.log(density(t));
    }

    default String getName() {
        return this.getClass().getSimpleName();
    }

    Map<String, Value> getParams();

    void setParam(String paramName, Value value);

    default void print(PrintWriter p) {
        Map<String, Value> map = getParams();

        Iterator<Map.Entry<String, Value>> iterator = map.entrySet().iterator();

        Map.Entry<String, Value> entry = iterator.next();

        p.print(getName() + "(" + entry.getKey() + "=" + entry.getValue().id);
        while (iterator.hasNext()) {
            entry = iterator.next();
            p.print(", " + entry.getKey() + "=" + entry.getValue().id);
        }
        p.print(");");
    }

    default String getParamName(int paramIndex, int constructorIndex) {
        return getParameterInfo(constructorIndex).get(paramIndex).name();
    }

    default String getParamName(int paramIndex) {
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

    default String getRichDescription(int index) {

        List<ParameterInfo> pInfo = getParameterInfo(index);

        Map<String, Value> paramValues = getParams();

        String html = "<html><h3>" + getName() + " distribution</h3>parameters: <ul>";
        for (ParameterInfo pi : pInfo) {
            html += "<li>" + pi.name() + " (" + paramValues.get(pi.name()) + "); <font color=\"#808080\">" + pi.description() + "</font></li>";
        }
        html += "</ul></html>";
        return html;
    }

    default JComponent getViewer() {
        return new JLabel(getRichDescription(0));
    }
}
