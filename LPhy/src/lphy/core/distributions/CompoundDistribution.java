package lphy.core.distributions;

import lphy.graphicalModel.GenerativeDistribution;
import lphy.graphicalModel.RandomVariable;
import lphy.graphicalModel.RangeElement;
import lphy.graphicalModel.Value;

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
