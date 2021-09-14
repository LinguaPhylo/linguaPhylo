package lphy.core.lightweight;

import lphy.core.lightweight.distributions.Beta;
import lphy.graphicalModel.*;

import java.util.*;

public class GenerativeDistributionAdapter<T> extends GeneratorAdapter<T> implements GenerativeDistribution<T> {

    public GenerativeDistributionAdapter(LGenerativeDistribution<T> baseDistribution, Map<String, Value> params) {
        super(baseDistribution, params);
    }

    public RandomVariable<T> sample() {
        return sample(null);
    }

    public RandomVariable<T> sample(String id) {
        return new RandomVariable<>(id, ((LGenerativeDistribution<T>)baseDistribution).sample(), this);
    }

    public Value<T> generate() {
        return sample();
    }

    public static void main(String[] args) {

        Beta beta = new Beta(1.0, 2.0);

        VectorizedGenerativeDistribution<Double> v = new VectorizedGenerativeDistribution<>(beta, null);

        SortedMap<String, Value> params = new TreeMap<>();
        params.put("alpha", new Value<>(null, new Double[]{200.0, 200.0, 200.0, 3.0, 3.0, 3.0}));
        params.put("beta", new Value<>(null, 2.0));

        long time = System.currentTimeMillis();
        Value<Double[]> rbeta = null;
        for (int i = 0; i < 10000; i++) {

            GenerativeDistributionAdapter<Double[]> w = new GenerativeDistributionAdapter<>(v, params);

            rbeta = w.generate();
        }
        long stopTime = System.currentTimeMillis();

        System.out.println("Elapsed time = " + (stopTime -time));

        System.out.println(Arrays.toString(rbeta.value()));

        System.out.println(Arrays.toString(beta.getClass().getConstructors()[0].getParameterTypes()));
    }
}
