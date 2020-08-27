package lphy.evolution.birthdeath;

import lphy.core.distributions.Utils;
import lphy.evolution.tree.TimeTree;
import lphy.evolution.tree.TimeTreeNode;
import lphy.graphicalModel.*;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.*;

/**
 * A Yule tree generative distribution
 */
public class Yule implements GenerativeDistribution<TimeTree> {

    private final String birthRateParamName;
    private final String nParamName;
    private final String rootAgeParamName;
    private Value<Number> birthRate;
    private Value<Integer> n;
    private Value<Double> rootAge;

    private List<TimeTreeNode> activeNodes;

    RandomGenerator random;

    public Yule(@ParameterInfo(name = "birthRate", description = "per-lineage birth rate, possibly scaled to mutations or calendar units.") Value<Number> birthRate,
                @ParameterInfo(name = "n", description = "the number of taxa.") Value<Integer> n,
                @ParameterInfo(name = "rootAge", description = "the root age to be conditioned on. optional.", optional=true) Value<Double> rootAge) {
        this.birthRate = birthRate;
        this.n = n;
        this.rootAge = rootAge;
        this.random = Utils.getRandom();

        birthRateParamName = getParamName(0);
        nParamName = getParamName(1);
        rootAgeParamName = getParamName(2);

        activeNodes = new ArrayList<>(2 * n.value());
    }

    @GeneratorInfo(name = "Yule", description = "The Yule tree distribution over tip-labelled time trees. Will be conditional on the root age if one is provided.")
    public RandomVariable<TimeTree> sample() {

        TimeTree tree = new TimeTree();
        activeNodes.clear();

        for (int i = 0; i < n.value(); i++) {
            TimeTreeNode node = new TimeTreeNode(i + "", tree);
            node.setLeafIndex(i);
            activeNodes.add(node);
        }

        double time = 0.0;
        double birthRate = this.birthRate.value().doubleValue();

        double[] times = new double[activeNodes.size() - 1];

        if (rootAge == null) {
            int k = activeNodes.size();
            for (int i = 0; i < times.length; i++) {
                double totalRate = birthRate * (double) k;

                // random exponential variate
                double x = -Math.log(random.nextDouble()) / totalRate;
                time += x;
                times[i] = time;
                k -= 1;
            }
        } else {
            double t = rootAge.value();
            for (int i = 0; i < times.length-1; i++) {
                times[i] = yuleInternalQ(random.nextDouble(), birthRate, t);
            }
            times[times.length-1] = t;
            Arrays.sort(times);
        }


        for (int i = 0; i < times.length; i++) {

            TimeTreeNode a = activeNodes.remove(random.nextInt(activeNodes.size()));
            TimeTreeNode b = activeNodes.remove(random.nextInt(activeNodes.size()));
            TimeTreeNode parent = new TimeTreeNode(times[i], new TimeTreeNode[]{a, b});
            activeNodes.add(parent);
        }

        tree.setRoot(activeNodes.get(0));

        return new RandomVariable<>("\u03C8", tree, this);
    }

    private double yuleInternalQ(double p, double lambda, double rootHeight) {
        double h = lambda;
        double t = rootHeight;
        double e1 = Math.exp(-h * t);
        double a2 = p * (1.0 - e1);
        return (1.0 / h) * Math.log(h / (h - h * a2));
    }

    @Override
    public double logDensity(TimeTree timeTree) {
        throw new UnsupportedOperationException("Not implemented!");
    }

    @Override
    public SortedMap<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        map.put(birthRateParamName, birthRate);
        map.put(nParamName, n);
        if (rootAge != null) map.put(rootAgeParamName, rootAge);
        return map;
    }

    public Value<Number> getBirthRate() {
        return birthRate;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(birthRateParamName)) birthRate = value;
        else if (paramName.equals(nParamName)) n = value;
        else if (paramName.equals(rootAgeParamName)) rootAge = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);
    }

    public String toString() {
        return getName();
    }
}
