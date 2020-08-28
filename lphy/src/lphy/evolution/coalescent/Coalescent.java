package lphy.evolution.coalescent;

import lphy.evolution.Taxa;
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
public class Coalescent implements GenerativeDistribution<TimeTree> {

    private final String thetaParamName;
    private final String nParamName;
    private final String taxaParamName;
    private Value<Double> theta;
    private Value<Integer> n;
    private Value taxa;

    RandomGenerator random;

    public Coalescent(@ParameterInfo(name = "theta", description = "effective population size, possibly scaled to mutations or calendar units.") Value<Double> theta,
                      @ParameterInfo(name = "n", description = "the number of taxa.", optional=true) Value<Integer> n,
                      @ParameterInfo(name = "taxa", description = "a string array of taxa id or a taxa object (e.g. dataframe, alignment or tree)", optional=true) Value taxa) {
        this.theta = theta;
        this.n = n;
        this.taxa = taxa;

        thetaParamName = getParamName(0);
        nParamName = getParamName(1);
        taxaParamName = getParamName(2);

        if (taxa == null && n == null) {
            throw new IllegalArgumentException("At least one of " + nParamName + ", " + taxaParamName + " must be specified.");

        }

        if (taxa != null && n != null) {
            if (taxaLength(taxa) != n.value()) {
                throw new IllegalArgumentException(nParamName + " and " + taxaParamName + " are incompatible.");
            }
        }

        this.random = Utils.getRandom();

    }

    private int taxaLength(Value taxa) {
        if (taxa.value() instanceof String[]) return ((String[])taxa.value()).length;
        if (taxa.value() instanceof Taxa) return ((Taxa)taxa.value()).ntaxa();
        throw new IllegalArgumentException(taxaParamName + " must be of type String[] or Taxa.");
    }

    @GeneratorInfo(name="Coalescent", description="The Kingman coalescent distribution over tip-labelled time trees.")
    public RandomVariable<TimeTree> sample() {

        TimeTree tree = new TimeTree();

        List<TimeTreeNode> activeNodes = new ArrayList<>();

        int ntaxa = n();

        String[] taxaNames = null;
        if (taxa != null) taxaNames = taxaNames();

        for (int i = 0; i < ntaxa; i++) {
            TimeTreeNode node = new TimeTreeNode(taxa == null ? i+"" : taxaNames[i], tree);
            System.out.println(node.getId());
            node.setLeafIndex(i);
            activeNodes.add(node);
        }

        double time = 0.0;
        double theta = this.theta.value();

        while (activeNodes.size() > 1) {
            int k = activeNodes.size();

            TimeTreeNode a = activeNodes.remove(random.nextInt(activeNodes.size()));
            TimeTreeNode b = activeNodes.remove(random.nextInt(activeNodes.size()));

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

    private String[] taxaNames() {
        String[] taxaNames = new String[n()];
        if (taxa == null) {
            for (int i = 0; i < taxaNames.length; i++) {
                taxaNames[i] = i + "";
            }
        } else if (taxa.value() instanceof String[]) {
            return (String[])taxa.value();
        } else if (taxa.value() instanceof Taxa) {
            return ((Taxa)taxa.value()).getTaxa();
        }
        throw new IllegalArgumentException(taxaParamName + " must be of type String[] or Taxa.");
    }

    private int n() {
        if (n != null) return n.value();
        return taxaLength(taxa);
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
        SortedMap<String, Value> map = new TreeMap<>();
        map.put(thetaParamName, theta);
        if (n != null) map.put(nParamName, n);
        if (taxa != null) map.put(taxaParamName, taxa);
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(thetaParamName)) theta = value;
        else if (paramName.equals(nParamName)) n = value;
        else if (paramName.equals(taxaParamName)) taxa = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);
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

        Random random = new Random();

        Value<Double> thetaExpPriorRate = new Value<>("r", 20.0);
        Exp exp = new Exp(thetaExpPriorRate);

        RandomVariable<Double> theta = exp.sample("\u0398");
        Value<Integer> n = new Value<>("n", 20);

        Coalescent coalescent = new Coalescent(theta, n, null);

        RandomVariable<TimeTree> g = coalescent.sample();

    }

    public String toString() {
        return getName();
    }

    public Value<Double> getTheta() {
        return theta;
    }

}
