package james;

import james.core.distributions.Exp;
import james.core.distributions.Utils;
import james.graphicalModel.*;

import java.io.PrintWriter;
import java.util.*;

/**
 * A Yule tree generative distribution
 */
public class Yule implements GenerativeDistribution<TimeTree> {

    private final String birthRateParamName;
    private final String nParamName;
    private Value<Double> birthRate;
    private Value<Integer> n;

    Random random;

    private Exp exp;


    public Yule(@ParameterInfo(name = "birthRate", description = "per-lineage birth rate, possibly scaled to mutations or calendar units.") Value<Double> birthRate,
                @ParameterInfo(name = "n", description = "the number of taxa.") Value<Integer> n) {
        this.birthRate = birthRate;
        this.n = n;
        this.random = Utils.getRandom();

        birthRateParamName = getParamName(0);
        nParamName = getParamName(1);

        exp = new Exp(new Value<>("rate", birthRate.value()));
    }

    @GenerativeDistributionInfo(description="The Yule tree distribution over tip-labelled time trees.")
    public RandomVariable<TimeTree> sample() {

        TimeTree tree = new TimeTree();

        List<TimeTreeNode> activeNodes = new ArrayList<>();

        for (int i = 0; i < n.value(); i++) {
            activeNodes.add(new TimeTreeNode(i + "", tree));
        }

        double time = 0.0;
        double birthRate = this.birthRate.value();

        while (activeNodes.size() > 1) {
            int k = activeNodes.size();

            TimeTreeNode a = activeNodes.remove(random.nextInt(activeNodes.size()));
            TimeTreeNode b = activeNodes.remove(random.nextInt(activeNodes.size()));

            double totalRate = birthRate * (double)k;

            exp.setRate(totalRate);
            time += exp.sample().value();

            TimeTreeNode parent = new TimeTreeNode(time, new TimeTreeNode[]{a, b});
            activeNodes.add(parent);
        }

        tree.setRoot(activeNodes.get(0));

        return new RandomVariable<>("\u03C8", tree, this);
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
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(birthRateParamName)) birthRate = value;
        else if (paramName.equals(nParamName)) n = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);
    }

    public String toString() {
        return getName();
    }
}
