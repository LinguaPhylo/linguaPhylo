package lphy.core.distributions;

import lphy.graphicalModel.*;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.*;

public class RandomComposition implements GenerativeDistribution<Integer[]> {

    private final String nParamName;
    private final String kParamName;
    private Value<Integer> n;
    private Value<Integer> k;

    public RandomComposition(@ParameterInfo(name="n", description="the sum of the random tuple.") Value<Integer> n,
                             @ParameterInfo(name="k", description="the size of the random tuple.") Value<Integer> k) {
        this.n = n;
        this.k = k;
        nParamName = getParamName(0);
        kParamName = getParamName(1);
    }

    @GeneratorInfo(name="RandomComposition", description="Samples a random k-tuple of positive integers that sum to n.")
    public RandomVariable<Integer[]> sample() {
        List<Integer> bars = new ArrayList<>();
        RandomGenerator random = Utils.getRandom();

        bars.add(0);
        while (bars.size() < k.value()) {
            int candidate = random.nextInt(n.value() - 1) + 1;
            if (!bars.contains(candidate)) {
                bars.add(candidate);
            }
        }
        bars.add(n.value());
        Collections.sort(bars);

        Integer[] composition = new Integer[k.value()];
        for (int i = 0; i < composition.length; i++) {
            composition[i] = bars.get(i+1) - bars.get(i);
        }
        return new RandomVariable<>("x", composition, this);
    }

    public Map<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        map.put(nParamName, n);
        map.put(kParamName, k);
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(nParamName)) n = value;
        else if (paramName.equals(kParamName)) k = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);
    }
}
