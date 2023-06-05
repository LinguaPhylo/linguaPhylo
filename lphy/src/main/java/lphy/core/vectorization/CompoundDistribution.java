package lphy.core.vectorization;

import lphy.core.model.component.GenerativeDistribution;
import lphy.core.model.component.RandomVariable;
import lphy.core.model.component.Value;
import lphy.core.vectorization.array.RangeElement;

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
