package lphy.evolution.coalescent;

import lphy.evolution.tree.TimeTree;
import lphy.evolution.tree.TimeTreeNode;
import lphy.core.distributions.Utils;
import lphy.graphicalModel.*;
import org.apache.commons.math3.random.RandomGenerator;

import java.sql.Time;
import java.util.*;

/**
 * A Kingman coalescent tree generative distribution for serially sampled data
 */
public class SerialCoalescent implements GenerativeDistribution<TimeTree> {

    private final String thetaParamName;
    private final String agesParamName;
    private final String taxaAgesParamName;
    private Value<Double> theta;
    private Value<Double[]> ages;
    private Value<Map<String, Double>> taxaAges;

    RandomGenerator random;

    public SerialCoalescent(@ParameterInfo(name = "theta", description = "effective population size, possibly scaled to mutations or calendar units.") Value<Double> theta,
                            @ParameterInfo(name = "ages", description = "an array of leaf node ages.", optional=true) Value<Double[]> ages,
                            @ParameterInfo(name = "taxaAges", description = "an array of leaf node ages.", optional=true) Value<Map<String, Double>> taxaAges) {
        this.theta = theta;
        this.ages = ages;
        this.taxaAges = taxaAges;
        this.random = Utils.getRandom();

        thetaParamName = getParamName(0);
        agesParamName = getParamName(1);
        taxaAgesParamName = getParamName(2);

        int c = (ages == null ? 1 : 0) + (taxaAges == null ? 1 : 0);

        if (c != 1) {
            throw new IllegalArgumentException("Exactly one of " + agesParamName + " and " + taxaAgesParamName + " must be specified in " + getName());
        }
    }

    @GeneratorInfo(name="Coalescent", description="The serially sampled Kingman coalescent distribution over tip-labelled time trees.")
    public RandomVariable<TimeTree> sample() {

        TimeTree tree = new TimeTree();

        List<TimeTreeNode> leafNodes = createLeafTaxa(tree);
        List<TimeTreeNode> activeNodes = new ArrayList<>();
        List<TimeTreeNode> leavesToBeAdded = new ArrayList<>();

        double time = 0.0;

        for (TimeTreeNode leaf : leafNodes) {
            if (leaf.getAge() <= time) {
                activeNodes.add(leaf);
            } else {
                leavesToBeAdded.add(leaf);
            }
        }
        leavesToBeAdded.sort(
                (o1, o2) -> Double.compare(o2.getAge(), o1.getAge())); // REVERSE ORDER - youngest age at end of list

        double theta = this.theta.value();

        while ((activeNodes.size() + leavesToBeAdded.size()) > 1) {
            int k = activeNodes.size();

            if (k == 1) {
                time = leavesToBeAdded.get(leavesToBeAdded.size()-1).getAge();
            } else {

                // draw next time;
                double rate = (k * (k - 1.0)) / (theta * 2.0);
                double x = -Math.log(random.nextDouble()) / rate;
                time += x;

                if (leavesToBeAdded.size() > 0 && time > leavesToBeAdded.get(leavesToBeAdded.size()-1).getAge()) {
                    time = leavesToBeAdded.get(leavesToBeAdded.size()-1).getAge();
                } else {

                    // do coalescence
                    TimeTreeNode a = activeNodes.remove(random.nextInt(activeNodes.size()));
                    TimeTreeNode b = activeNodes.remove(random.nextInt(activeNodes.size()));

                    TimeTreeNode parent = new TimeTreeNode(time, new TimeTreeNode[]{a, b});
                    activeNodes.add(parent);
                }
            }

            while (leavesToBeAdded.size() > 0 && leavesToBeAdded.get(leavesToBeAdded.size()-1).getAge() == time) {
                TimeTreeNode youngest = leavesToBeAdded.remove(leavesToBeAdded.size()-1);
                activeNodes.add(youngest);
            }
        }

        tree.setRoot(activeNodes.get(0));

        return new RandomVariable<>("\u03C8", tree, this);
    }

    private List<TimeTreeNode> createLeafTaxa(TimeTree tree) {
        List<TimeTreeNode> leafNodes = new ArrayList<>();

        if (ages != null) {

            Double[] leafAges = ages.value();

            for (int i = 0; i < leafAges.length; i++) {
                TimeTreeNode node = new TimeTreeNode(i+"", tree);
                node.setAge(leafAges[i]);
                leafNodes.add(node);
            }
            return leafNodes;

        } else if (taxaAges != null) {

            Map<String, Double> leafTaxaAges = taxaAges.value();

            for (Map.Entry<String, Double> entry : leafTaxaAges.entrySet()) {
                TimeTreeNode node = new TimeTreeNode(entry.getKey(), tree);
                node.setAge(entry.getValue());
                leafNodes.add(node);
            }
            return leafNodes;

        } else throw new RuntimeException("Expected either " + agesParamName + " or " + taxaAgesParamName);
    }

    @Override
    public double logDensity(TimeTree timeTree) {

        // TODO!

        return 0.0;
    }

    @Override
    public SortedMap<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        map.put(thetaParamName, theta);
        if (ages != null) map.put(agesParamName, ages);
        if (taxaAges != null) map.put(taxaAgesParamName, taxaAges);
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(thetaParamName)) theta = value;
        else if (paramName.equals(agesParamName)) ages = value;
        else if (paramName.equals(taxaAgesParamName)) taxaAges = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);
    }

    public String toString() {
        return getName();
    }

    public Value<Double> getTheta() {
        return theta;
    }
}
