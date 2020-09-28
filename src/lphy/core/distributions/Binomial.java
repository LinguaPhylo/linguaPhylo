package lphy.core.distributions;

import lphy.graphicalModel.*;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class Binomial implements GenerativeDistribution<Integer> {

    private final String pParamName;
    private final String nParamName;
    private Value<Double> p;
    private Value<Integer> n;

    private RandomGenerator random;

    public Binomial(@ParameterInfo(name="prob", description="the probability of a success.", type=Double.class) Value<Double> p,
                    @ParameterInfo(name="n", description="number of trials.", type=Integer.class) Value<Integer> n) {
        this.p = p;
        this.n = n;
        this.random = Utils.getRandom();
        pParamName = getParamName(0);
        nParamName = getParamName(1);
    }

    @GeneratorInfo(name="Binomial", description="The binomial distribution of x successes in n trials given probability p of success of a single trial.")
    public RandomVariable<Integer> sample() {

        BinomialDistribution binomial = new BinomialDistribution(n.value(), p.value());
        return new RandomVariable<>("x", binomial.sample(), this);
    }

    public double density(Integer i) {
        BinomialDistribution binomial = new BinomialDistribution(n.value(), p.value());
        return binomial.probability(i);
    }

    @Override
    public Map<String,Value> getParams() {

        SortedMap<String, Value> map = new TreeMap<>();
        map.put(pParamName, p);
        map.put(nParamName, n);
        return map;
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
