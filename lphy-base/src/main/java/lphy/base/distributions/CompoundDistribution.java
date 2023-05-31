package lphy.base.distributions;

import lphy.core.graphicalmodel.components.GenerativeDistribution;
import lphy.core.graphicalmodel.components.RandomVariable;
import lphy.core.graphicalmodel.components.Value;
import lphy.core.vectorization.RangeElement;

import java.util.Map;

public class CompoundDistribution<T> implements GenerativeDistribution<T[]> {

    public CompoundDistribution(GenerativeDistribution generativeDistribution, RangeElement element) {

    }

    public void add(RandomVariable variable, RangeElement element) {
        
    }

    @Override
    public RandomVariable<T[]> sample() {
        return null;
    }

    @Override
    public Map<String, Value> getParams() {
        return null;
    }
}
