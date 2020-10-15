package lphy.evolution.birthdeath;

import lphy.core.distributions.Utils;
import lphy.evolution.tree.TimeTree;
import lphy.evolution.tree.TimeTreeNode;
import lphy.graphicalModel.*;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.*;

import static lphy.evolution.birthdeath.BirthDeathConstants.rhoParamName;
import static lphy.evolution.EvolutionConstants.treeParamName;
import static lphy.evolution.tree.TimeTreeUtils.*;
import static lphy.graphicalModel.ValueUtils.doubleValue;

/**
 * A Birth-death tree generative distribution
 */
public class RhoSampleTree implements GenerativeDistribution<TimeTree> {

    private Value<TimeTree> tree;
    private Value<Number> rho;

    RandomGenerator random;

    public RhoSampleTree(@ParameterInfo(name = treeParamName, description = "the full tree to sample") Value<TimeTree> tree,
                         @ParameterInfo(name = rhoParamName, description = "the probability that each tip at time zero is sampled") Value<Number> rho) {

        this.tree = tree;
        this.rho = rho;
        this.random = Utils.getRandom();
    }


    @GeneratorInfo(name = "RhoSampleTree", description = "A tree sampled from a larger tree by selecting tips at time zero with probability rho.<br>" +
            "Conditioned on root age.")
    public RandomVariable<TimeTree> sample() {

        TimeTree sampleTree = new TimeTree(tree.value());
        double p = doubleValue(rho);

        List<TimeTreeNode> sampleTips = new ArrayList<>();

        while (sampleTips.size() == 0) {
            for (TimeTreeNode node : sampleTree.getNodes()) {
                if (node.isLeaf() && node.getAge() == 0.0 && random.nextDouble() < p) {
                    sampleTips.add(node);
                }
            }
        }
        System.out.println("Sample tree has " + sampleTips.size() + " tips.");

        for (TimeTreeNode tip : sampleTips) {
            markNodeAndDirectAncestors(tip);
        }

        removeUnmarkedNodes(sampleTree);

        TimeTreeNode newRoot = getFirstNonSingleChildNode(sampleTree);
        if (!newRoot.isRoot()) {
            newRoot.getParent().removeChild(newRoot);
        }

        removeSingleChildNodes(newRoot, true);

        sampleTree.setRoot(newRoot, true);

        return new RandomVariable<>("\u03C8", sampleTree, this);
    }

    @Override
    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(treeParamName, tree);
            put(rhoParamName, rho);
        }};
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(treeParamName)) tree = value;
        else if (paramName.equals(rhoParamName)) rho = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);
    }

    public String toString() {
        return getName();
    }
}
