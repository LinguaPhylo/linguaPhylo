package james.graphicalModel;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by adru001 on 17/12/19.
 */
public interface GenerativeDistribution<T> {

    RandomVariable<T> sample();

    default RandomVariable<T> sample(String name) {
        RandomVariable<T> v = sample();
        v.name = name;
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

    List<Value> getParams();

    default void print(PrintWriter p) {
        List<Value> params = getParams();

        p.print(getName()+"("+params.get(0).name);
        for (int i = 1; i < params.size(); i++) {
            p.print(", " + params.get(i).name);
        }
        p.print(");");
    }
}
