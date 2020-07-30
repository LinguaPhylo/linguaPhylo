package lphy.evolution.birthdeath;

import lphy.core.distributions.Utils;
import lphy.evolution.tree.TimeTree;
import lphy.evolution.tree.TimeTreeNode;
import lphy.graphicalModel.*;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.*;

/**
 * A Birth-death tree generative distribution
 */
public class BirthDeathSamplingTree implements GenerativeDistribution<TimeTree> {

    final String birthRateParamName;
    final String deathRateParamName;
    final String rhoParamName;
    final String rootAgeParamName;
    private Value<Double> birthRate;
    private Value<Double> deathRate;
    private Value<Double> rho;
    private Value<Double> rootAge;

    private List<TimeTreeNode> activeNodes;

    RandomGenerator random;

    public BirthDeathSamplingTree(@ParameterInfo(name = "lambda", description = "per-lineage birth rate.") Value<Double> birthRate,
                                  @ParameterInfo(name = "mu", description = "per-lineage death rate.") Value<Double> deathRate,
                                  @ParameterInfo(name = "rho", description = "the sampling proportion.") Value<Double> rho,
                                  @ParameterInfo(name = "rootAge", description = "the number of taxa.") Value<Double> rootAge
                          ) {

        this.birthRate = birthRate;
        this.deathRate = deathRate;
        this.rho = rho;
        this.rootAge = rootAge;
        this.random = Utils.getRandom();

        birthRateParamName = getParamName(0);
        deathRateParamName = getParamName(1);
        rhoParamName = getParamName(2);
        rootAgeParamName = getParamName(3);

        activeNodes = new ArrayList<>();
    }


    @GeneratorInfo(name="BirthDeathSampling", description="The Birth-death-sampling tree distribution over tip-labelled time trees.<br>" +
            "Conditioned on root age.")
    public RandomVariable<TimeTree> sample() {

        BirthDeathTree birthDeathTree = new BirthDeathTree(birthRate, deathRate, rootAge);
        RandomVariable<TimeTree> fullTree = birthDeathTree.sample();

        RhoSampleTree rhoSampleTree = new RhoSampleTree(fullTree, rho);

        return rhoSampleTree.sample();
    }

    private int singleChildNodeCount(TimeTreeNode node) {
        int count = 0;
        if (node.getChildCount() == 1) {
            count += 1;
        }
        for (TimeTreeNode child : node.getChildren()) {
            count += singleChildNodeCount(child);
        }
        return count;
    }

    private TimeTreeNode removeTwoDegreeNodes(TimeTreeNode node, TimeTreeNode root) {
        if (node.getChildCount() == 1) {

            if (!node.isRoot()) {
                node.getParent().removeChild(node);
            }
            TimeTreeNode grandChild = node.getChildren().get(0);
            node.removeChild(grandChild);
            if (!node.isRoot()) {
                node.getParent().addChild(grandChild);
            } else {
                root = grandChild;
            }
            return removeTwoDegreeNodes(grandChild, root);
        } else {

            List<TimeTreeNode> children = new ArrayList<>();
            children.addAll(node.getChildren());

            for (TimeTreeNode child : children) {
                removeTwoDegreeNodes(child, root);
            }
        }
        return root;
    }

    private boolean removeAllMarked(TimeTreeNode node) {

        boolean found = false;
        if (!node.isLeaf()) {
            for (TimeTreeNode child : node.getChildren()) {
                removeAllMarked(child);
            }

            Set<TimeTreeNode> nodesToRemove = new HashSet<>();
            for (TimeTreeNode child : node.getChildren()) {
                if (markedForRemoval(child)) {
                    nodesToRemove.add(child);
                    found = true;
                }
            }
            for (TimeTreeNode child : nodesToRemove) {
                node.removeChild(child);
            }
            if (node.getChildCount() == 0) {
                markForRemoval(node);
                found = true;
            }
        }
        return found;
    }

    private boolean markedForRemoval(TimeTreeNode node) {
        Object remove = node.getMetaData("remove");
        return (remove instanceof Boolean && (Boolean)remove);
    }


    private void markForRemoval(TimeTreeNode node) {
        node.setMetaData("remove", true);
    }

    private void doBirth(List<TimeTreeNode> activeNodes, double age, int[] nextnum, TimeTree tree) {
        TimeTreeNode parent = activeNodes.remove(random.nextInt(activeNodes.size()));
        parent.setAge(age);
        TimeTreeNode child1 = new TimeTreeNode("" + nextnum[0], tree);
        nextnum[0] += 1;
        TimeTreeNode child2 = new TimeTreeNode("" + nextnum[0], tree);
        nextnum[0] += 1;
        child1.setAge(age);
        child2.setAge(age);
        parent.addChild(child1);
        parent.addChild(child2);
        activeNodes.add(child1);
        activeNodes.add(child2);
    }

    private void doDeath(List<TimeTreeNode> activeNodes, double age) {
        TimeTreeNode deadNode = activeNodes.remove(random.nextInt(activeNodes.size()));
        deadNode.setAge(age);
        markForRemoval(deadNode);
    }


    @Override
    public double logDensity(TimeTree timeTree) {

        throw new UnsupportedOperationException("Not implemented!");
    }

    @Override
    public SortedMap<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        map.put(birthRateParamName, birthRate);
        map.put(deathRateParamName, deathRate);
        map.put(rhoParamName, rho);
        map.put(rootAgeParamName, rootAge);
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(birthRateParamName)) birthRate = value;
        else if (paramName.equals(deathRateParamName)) deathRate = value;
        else if (paramName.equals(rhoParamName)) rho = value;
        else if (paramName.equals(rootAgeParamName)) rootAge = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);
    }

    public String toString() {
        return getName();
    }
}
