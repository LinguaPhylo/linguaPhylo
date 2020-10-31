package lphy.graphicalModel;

import javax.swing.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.List;

/**
 * Created by adru001 on 17/12/19.
 */
public interface GenerativeDistribution<T> extends Generator<T>, Viewable {

    RandomVariable<T> sample();

    default Value<T> generate() {
        return sample();
    }

//    default RandomVariable<T[]> sample(int dim, T[] array) {
//        for (int i = 0; i < array.length; i++) {
//            array[i] = sample().value();
//        }
//        return new RandomVariable<T[]>("x", array, this);
//    }

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

        String name = this.getClass().getSimpleName();

        GeneratorInfo ginfo = getInfo();
        if (ginfo != null) {
            name = ginfo.name();
        }
        return name;
    }

    default String getUniqueId() {
        return hashCode() + "";
    }

    default JComponent getViewer() {
        return new JLabel(getRichDescription(0));
    }

    default String codeString() {
        Map<String, Value> map = getParams();

        Iterator<Map.Entry<String, Value>> iterator = map.entrySet().iterator();

        Map.Entry<String, Value> entry = iterator.next();

        StringBuilder builder = new StringBuilder();

        builder.append(getName()).append("(").append(Generator.getArgumentCodeString(entry));
        while (iterator.hasNext()) {
            entry = iterator.next();
            builder.append(", ").append(Generator.getArgumentCodeString(entry));
        }
        builder.append(");");
        return builder.toString();
    }

    /**
     * Implementation of GraphicalModelNode value method. Unsupported for Generative Distributions.
     * @throws UnsupportedOperationException
     */
    default T value() {
        throw new UnsupportedOperationException();
    }

}
