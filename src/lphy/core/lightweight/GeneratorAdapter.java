package lphy.core.lightweight;

import lphy.graphicalModel.Generator;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.RandomVariable;
import lphy.graphicalModel.Value;

import java.lang.reflect.Array;
import java.util.*;

public abstract class GeneratorAdapter<T> implements Generator<T> {

    LightweightGenerator<T> baseDistribution;

    Map<String, Value> params;

    public GeneratorAdapter(LightweightGenerator<T> baseDistribution, Map<String, Value> params) {
        this.baseDistribution = baseDistribution;

        this.params = params;

        if (this.params == null) this.params = new TreeMap<>();

        setup();
    }

    void setup() {
       for (Map.Entry<String, Value> entry : params.entrySet()) {
           baseDistribution.setArgumentValue(entry.getKey(), entry.getValue().value());
       }
    }

    @Override
    public String getName() {
        return baseDistribution.getName();
    }

    public Value<T> generate() {
        return new Value<T>(null, baseDistribution.generateLight());
    }

    @Override
    public Map<String, Value> getParams() {
        return params;
    }

    @Override
    public String getUniqueId() {
        return null;
    }

    @Override
    public T value() {
        return baseDistribution.generateLight();
    }
}
