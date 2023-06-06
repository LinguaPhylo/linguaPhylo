package lphy.core.model;

import java.util.Collections;
import java.util.List;

/**
 * Created by Alexei Drummond on 18/12/19.
 */
public class RandomVariable<T> extends Value<T> {

    GenerativeDistribution<T> g;

    public RandomVariable(String name, T value, GenerativeDistribution<T> g) {
        super(name, value);
        this.g = g;
    }

    public Generator<T> getGenerator() {
        return g;
    }

    public GenerativeDistribution<T> getGenerativeDistribution() {
        return g;
    }

    @Override
    public List<GraphicalModelNode> getInputs() {
        return Collections.singletonList(g);
    }

}