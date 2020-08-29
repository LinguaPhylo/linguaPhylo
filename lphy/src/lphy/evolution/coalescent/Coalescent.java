package lphy.evolution.coalescent;

import lphy.evolution.Taxa;
import lphy.evolution.tree.TaxaConditionedTreeGenerator;
import lphy.evolution.tree.TimeTree;
import lphy.evolution.tree.TimeTreeNode;
import lphy.core.distributions.Exp;
import lphy.core.distributions.Utils;
import lphy.graphicalModel.*;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.*;

/**
 * A Kingman coalescent tree generative distribution
 */
public class Coalescent extends TaxaConditionedTreeGenerator {

    private final String thetaParamName;
    private Value<Double> theta;

    RandomGenerator random;

    public Coalescent(@ParameterInfo(name = "theta", description = "effective population size, possibly scaled to mutations or calendar units.") Value<Double> theta,
                      @ParameterInfo(name = "n", description = "the number of taxa. Provide this or taxa.", optional=true) Value<Integer> n,
                      @ParameterInfo(name = "taxa", description = "a string array of taxa id or a taxa object (e.g. dataframe, alignment or tree). Provide this or n.", optional=true) Value taxa) {

        super(n, taxa);

        this.theta = theta;

        thetaParamName = getParamName(0);
        nParamName = getParamName(1);
        taxaParamName = getParamName(2);

        this.random = Utils.getRandom();
        checkTaxaParameters(true);
    }

    @GeneratorInfo(name="Coalescent", description="The Kingman coalescent distribution over tip-labelled time trees.")
    public RandomVariable<TimeTree> sample() {

        TimeTree tree = new TimeTree();

        List<TimeTreeNode> activeNodes = new ArrayList<>();

        createLeafNodes(tree, activeNodes);

        double time = 0.0;
        double theta = this.theta.value();

        while (activeNodes.size() > 1) {
            int k = activeNodes.size();

            TimeTreeNode a = drawRandomNode(activeNodes);
            TimeTreeNode b = drawRandomNode(activeNodes);

            double rate = (k * (k - 1.0))/(theta * 2.0);

            // random exponential variate
            double x = - Math.log(random.nextDouble()) / rate;
            time += x;

            TimeTreeNode parent = new TimeTreeNode(time, new TimeTreeNode[] {a, b});
            activeNodes.add(parent);
        }

        tree.setRoot(activeNodes.get(0));

        return new RandomVariable<>("\u03C8", tree, this);
    }

    @Override
    public double logDensity(TimeTree timeTree) {

        double[] ages = getInternalNodeAges(timeTree, null);
        Arrays.sort(ages);
        double age = 0;
        int k = timeTree.n();
        double logDensity = 0;
        double theta = this.theta.value();

        for (double age1 : ages) {
            double interval = age1 - age;

            logDensity -= k * (k - 1) * interval / (2 * theta);

            age = age1;
            k -= 1;
        }

        logDensity -= (timeTree.n() - 1) * Math.log(theta);

        return logDensity;
    }

    @Override
    public SortedMap<String, Value> getParams() {
        SortedMap<String, Value> map = super.getParams();
        map.put(thetaParamName, theta);
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(thetaParamName)) theta = value;
        else super.setParam(paramName, value);
    }

    private double[] getInternalNodeAges(TimeTree timeTree, double[] ages) {
        if (ages == null) ages = new double[timeTree.n() - 1];
        if (ages.length != timeTree.n() - 1)
            throw new IllegalArgumentException("Ages array size must one more than the number of internal nodes in the tree.");
        int i = 0;
        for (TimeTreeNode node : timeTree.getNodes()) {
            if (!node.isLeaf()) {
                ages[i] = node.getAge();
                i += 1;
            }
        }
        return ages;
    }

    public static void main(String[] args) {

        Value<Double> thetaExpPriorRate = new Value<>("r", 20.0);
        Exp exp = new Exp(thetaExpPriorRate);

        RandomVariable<Double> theta = exp.sample("\u0398");
        Value<Integer> n = new Value<>("n", 20);

        Coalescent coalescent = new Coalescent(theta, n, null);

        RandomVariable<TimeTree> g = coalescent.sample();
    }

    public Value<Double> getTheta() {
        return theta;
    }
}