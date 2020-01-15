package james.graphicalModel;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.util.*;

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

        p.print(getName()+"("+entry.getKey() + "=" + entry.getValue().id);
        while (iterator.hasNext()) {
            entry = iterator.next();
            p.print(", " +entry.getKey() + "=" + entry.getValue().id);
        }
        p.print(");");
    }

    JComponent getViewer();
}
