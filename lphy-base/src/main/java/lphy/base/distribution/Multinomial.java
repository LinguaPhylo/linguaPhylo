package lphy.base.distribution;

import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

public class Multinomial extends ParametricDistribution<Integer[]> {

    private Value<Integer> n;
    private Value<Double[]> p;
    private Value<Double[]> q;

    public Multinomial(
            @ParameterInfo(name = DistributionConstants.nParamName, description = "number of trials.") Value<Integer> n,
            @ParameterInfo(name = DistributionConstants.pParamName, description = "event probabilities.") Value<Double[]> p) {
        super();
        this.n = n;
        this.p = p;

        constructDistribution(random);
    }

    public Multinomial(){}

    @Override
    protected void constructDistribution(RandomGenerator random) {
    }


    @GeneratorInfo(name = "Multinomial", verbClause = "has", narrativeName = "multinomial distribution",
            category = GeneratorCategory.PRIOR,
            description = "The multinomial probability distribution.")

    @Override
    public RandomVariable<Integer[]> sample() {
        //org.apache.mahout.math.random.Multinomial multinomial = new org.apache.mahout.math.random.Multinomial();
        Double[] q1 = new Double[this.p.value().length];
        double cum_prob = 1.0;
        for (int i = 0; i < this.p.value().length; i++) {
            if (p.value()[i] == 0.0)
                q1[i] = 0.0;
            else {
                q1[i] = this.p.value()[i] / cum_prob;
                if (q1[i] > 1.0){q1[i] = 1.0;}
                cum_prob = cum_prob - this.p.value()[i];
                if (cum_prob < 0){ cum_prob = 0;}
            }
        }
        q = new Value<>("q", q1);
        int sampleSize = n.value();
        Integer[] result = new Integer[p.value().length];
        for (int i = 0; i < p.value().length-1; i++) {
            Value<Double> pro = new Value<Double>("pro", this.q.value()[i]);
            Value<Integer> sampleS = new Value<>("sample", sampleSize);
            Binomial binomial = new Binomial(pro, sampleS);
            int rand = binomial.sample().value();
            result[i] = rand;
            sampleSize -= rand;

            if (sampleSize == 0) {
                Arrays.fill(result, i + 1, result.length, 0);
                break;
            }
        }

        result[p.value().length-1] = sampleSize;
        return new RandomVariable<>(null, result, this);
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
                n = value;
                break;
            case DistributionConstants.pParamName:
                p = value;
                break;
            default:
                throw new RuntimeException("Unrecognised parameter name: " + paramName);
        }

//        super.setParam(paramName, value); // constructDistribution
    }

    public String toString() {
        return getName();
    }
}
