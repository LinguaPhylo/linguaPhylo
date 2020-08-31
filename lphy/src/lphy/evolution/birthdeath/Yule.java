package lphy.evolution.birthdeath;

import lphy.core.distributions.Utils;
import lphy.evolution.tree.TaxaConditionedTreeGenerator;
import lphy.evolution.tree.TimeTree;
import lphy.evolution.tree.TimeTreeNode;
import lphy.graphicalModel.*;

import java.util.*;

import static lphy.graphicalModel.ValueUtils.doubleValue;

/**
 * A Yule tree generative distribution
 */
public class Yule extends TaxaConditionedTreeGenerator {

    private final String birthRateParamName;
    private final String rootAgeParamName;
    private Value<Number> birthRate;
    private Value<Number> rootAge;

    private List<TimeTreeNode> activeNodes;

    public Yule(@ParameterInfo(name = "birthRate", description = "per-lineage birth rate, possibly scaled to mutations or calendar units.") Value<Number> birthRate,
                @ParameterInfo(name = "n", description = "the number of taxa.", optional=true) Value<Integer> n,
                @ParameterInfo(name = "taxa", description = "a string array of taxa id or a taxa object (e.g. dataframe, alignment or tree)", optional=true) Value taxa,
                @ParameterInfo(name = "rootAge", description = "the root age to be conditioned on. optional.", optional=true) Value<Number> rootAge) {

        super(n, taxa);

        this.birthRate = birthRate;
        this.rootAge = rootAge;
        this.random = Utils.getRandom();

        birthRateParamName = getParamName(0);
        nParamName = getParamName(1);
        taxaParamName = getParamName(2);
        rootAgeParamName = getParamName(3);

        checkTaxaParameters(true);

        activeNodes = new ArrayList<>(2 * n());
    }

    @GeneratorInfo(name = "Yule", description = "The Yule tree distribution over tip-labelled time trees. Will be conditional on the root age if one is provided.")
    public RandomVariable<TimeTree> sample() {

        TimeTree tree = new TimeTree();
        activeNodes.clear();

        createLeafNodes(tree, activeNodes);

        double time = 0.0;
        double lambda = doubleValue(birthRate);

        double[] times = new double[activeNodes.size() - 1];

        if (rootAge == null) {
            int k = activeNodes.size();
            for (int i = 0; i < times.length; i++) {
                double totalRate = lambda * (double) k;

                // random exponential variate
                double x = -Math.log(random.nextDouble()) / totalRate;
                time += x;
                times[i] = time;
                k -= 1;
            }
        } else {
            double t = doubleValue(rootAge);
            for (int i = 0; i < times.length-1; i++) {
                times[i] = yuleInternalQ(random.nextDouble(), lambda, t);
            }
            times[times.length-1] = t;
            Arrays.sort(times);
        }


        for (int i = 0; i < times.length; i++) {

            TimeTreeNode a = drawRandomNode(activeNodes);
            TimeTreeNode b = drawRandomNode(activeNodes);
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
        SortedMap<String, Value> map = super.getParams();
        map.put(birthRateParamName, birthRate);
        if (rootAge != null) map.put(rootAgeParamName, rootAge);
        return map;
    }

    public Value<Number> getBirthRate() {
        return birthRate;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(birthRateParamName)) birthRate = value;
        else if (paramName.equals(rootAgeParamName)) rootAge = value;
        else super.setParam(paramName, value);
    }

    public String toString() {
        return getName();
    }
}