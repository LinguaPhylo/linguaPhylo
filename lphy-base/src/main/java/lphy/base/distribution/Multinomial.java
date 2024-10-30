package lphy.base.distribution;

import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.mahout.math.random.WeightedThing;

import java.util.*;

public class Multinomial extends ParametricDistribution<Integer[]> {
    private Value<Integer> n;
    private Value<Double[]> p;
    private org.apache.mahout.math.random.Multinomial<String> multinomial;
    private List<WeightedThing<String>> weightedThings;

    public Multinomial(
            @ParameterInfo(name = DistributionConstants.nParamName, description = "number of trials.") Value<Integer> n,
            @ParameterInfo(name = DistributionConstants.pParamName, description = "event probabilities.") Value<Double[]> p) {
        super();
        this.n = n;
        this.p = p;

        constructDistribution(random);
    }

    public Multinomial(){
        constructDistribution(random);
    }

    @Override
    protected void constructDistribution(RandomGenerator random) {
    }

    @GeneratorInfo(name = "Multinomail", verbClause = "has", narrativeName = "multinomial prior",
            category = GeneratorCategory.PRIOR, examples = {"gt16ReadCountModel.lphy"},
            description = "The multinomial probability distribution.")
    @Override
    public RandomVariable<Integer[]> sample() {
        weightedThings = new ArrayList<>();
        String index;
        for (int i = 0; i < this.p.value().length; i++) {
            index = String.format("%d", i);
            weightedThings.add(new WeightedThing<>(index,this.p.value()[i]));
        }
        multinomial = new org.apache.mahout.math.random.Multinomial<String>(weightedThings);
        Integer[] results = new Integer[p.value().length];
        Arrays.fill(results, 0);
        for (int i = 0; i < n.value(); i++) {
            int result = Integer.parseInt(multinomial.sample());
            results[result]++;
        }
        return new RandomVariable<>(null, results, this);
    }

    @Override
    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(DistributionConstants.nParamName, n);
            put(DistributionConstants.pParamName, p);
        }};
    }

    @Override
    public void setParam(String paramName, Value value) {
        switch (paramName) {
            case DistributionConstants.nParamName:
                this.n = value;
                break;
            case DistributionConstants.pParamName:
                this.p = value;
                break;
            default:
                throw new RuntimeException("Unrecognised parameter name: " + paramName);
        }


//        super.setParam(paramName, value); // constructDistribution
    }
}
