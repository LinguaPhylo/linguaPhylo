package lphy.core.distributions;

import lphy.graphicalModel.*;
import org.apache.commons.math3.distribution.BinomialDistribution;

import java.util.Map;
import java.util.TreeMap;

import static lphy.core.distributions.DistributionConstants.nParamName;
import static lphy.core.distributions.DistributionConstants.pParamName;

public class Binomial implements GenerativeDistribution<Integer> {

    private Value<Double> p;
    private Value<Integer> n;

    public Binomial(@ParameterInfo(name = pParamName, description = "the probability of a success.") Value<Double> p,
                    @ParameterInfo(name = nParamName, description = "number of trials.") Value<Integer> n) {
        this.p = p;
        this.n = n;
    }

    @GeneratorInfo(name = "Binomial", narrativeName = "binomial distribution",
            description = "The binomial distribution of x successes in n trials given probability p of success of a single trial.")
    public RandomVariable<Integer> sample() {

        BinomialDistribution binomial = new BinomialDistribution(Utils.getRandom(), n.value(), p.value());
        return new RandomVariable<>(null, binomial.sample(), this);
    }

    public double density(Integer i) {
        BinomialDistribution binomial = new BinomialDistribution(Utils.getRandom(), n.value(), p.value());
        return binomial.probability(i);
    }

    @Override
    public Map<String, Value> getParams() {

        return new TreeMap<>() {{
            put(pParamName, p);
            put(nParamName, n);
        }};
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(pParamName)) p = value;
        else if (paramName.equals(nParamName)) n = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);
    }

    public String toString() {
        return getName();
    }
}
