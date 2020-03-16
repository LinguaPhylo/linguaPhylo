package lphy;

import lphy.core.distributions.Exp;
import lphy.core.distributions.Utils;
import lphy.graphicalModel.*;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.*;

/**
 * A Kingman coalescent tree generative distribution
 */
public class SerialCoalescent implements GenerativeDistribution<TimeTree> {

    private final String thetaParamName;
    private final String agesParamName;
    private Value<Double> theta;
    private Value<Double[]> ages;

    RandomGenerator random;

    public SerialCoalescent(@ParameterInfo(name = "theta", description = "effective population size, possibly scaled to mutations or calendar units.") Value<Double> theta,
                            @ParameterInfo(name = "ages", description = "an array of leaf node ages.") Value<Double[]> ages) {
        this.theta = theta;
        this.ages = ages;
        this.random = Utils.getRandom();

        thetaParamName = getParamName(0);
        agesParamName = getParamName(1);
    }

    @GeneratorInfo(name="Coalescent", description="The serially sampled Kingman coalescent distribution over tip-labelled time trees.")
    public RandomVariable<TimeTree> sample() {

        TimeTree tree = new TimeTree();

        List<TimeTreeNode> activeNodes = new ArrayList<>();
        List<TimeTreeNode> leavesToBeAdded = new ArrayList<>();

        Double[] agesArray = ages.value();

        double time = 0.0;
        
        for (int i = 0; i < agesArray.length; i++) {

            TimeTreeNode leaf = new TimeTreeNode(i+"", tree);
            leaf.setAge(agesArray[i]);

            if (agesArray[i] <= time) {
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

    @Override
    public double logDensity(TimeTree timeTree) {

        // TODO!

        return 0.0;
    }

    @Override
    public SortedMap<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        map.put(thetaParamName, theta);
        map.put(agesParamName, ages);
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(thetaParamName)) theta = value;
        else if (paramName.equals(agesParamName)) ages = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);
    }

    private double[] getInternalNodeAges(TimeTree timeTree, double[] ages) {
        if (ages == null) ages = new double[timeTree.n() - 1];
        if (ages.length != timeTree.n() - 1)
            throw new IllegalArgumentException("Ages array size must be equal to the number of internal nodes in the tree + 1.");
        int i = 0;
        for (TimeTreeNode node : timeTree.getNodes()) {
            if (!node.isLeaf()) {
                ages[i] = node.getAge();
                i += 1;
            }
        }
        return ages;
    }

    public String toString() {
        return getName();
    }
}
