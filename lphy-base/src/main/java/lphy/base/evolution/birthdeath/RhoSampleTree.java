package lphy.base.evolution.birthdeath;

import lphy.base.evolution.EvolutionConstants;
import lphy.base.evolution.tree.TimeTree;
import lphy.base.evolution.tree.TimeTreeNode;
import lphy.base.evolution.tree.TimeTreeUtils;
import lphy.core.model.GenerativeDistribution;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.ValueUtils;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.system.RandomUtils;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static lphy.base.evolution.birthdeath.BirthDeathConstants.rhoParamName;

/**
 * A birth-death tree sampled from a larger tree by selecting tips at time zero with probability rho.
 */
public class RhoSampleTree implements GenerativeDistribution<TimeTree> {

    private Value<TimeTree> tree;
    private Value<Number> rho;

    RandomGenerator random;

    public RhoSampleTree(@ParameterInfo(name = EvolutionConstants.treeParamName, description = "the full tree to sample") Value<TimeTree> tree,
                         @ParameterInfo(name = rhoParamName, description = "the probability that each tip at time zero is sampled") Value<Number> rho) {

        this.tree = tree;
        this.rho = rho;
        this.random = RandomUtils.getRandom();
    }


    @GeneratorInfo(name = "RhoSampleTree",
            category = GeneratorCategory.BD_TREE,
            description = "A tree sampled from a larger tree by selecting tips at time zero with probability rho.<br>" +
            "Conditioned on root age.")
    public RandomVariable<TimeTree> sample() {

        TimeTree sampleTree = new TimeTree(tree.value());
        double p = ValueUtils.doubleValue(rho);

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
            TimeTreeUtils.markNodeAndDirectAncestors(tip);
        }

        TimeTreeUtils.removeUnmarkedNodes(sampleTree);

        TimeTreeNode newRoot = TimeTreeUtils.getFirstNonSingleChildNode(sampleTree);
        if (!newRoot.isRoot()) {
            newRoot.getParent().removeChild(newRoot);
        }

        TimeTreeUtils.removeSingleChildNodes(newRoot, true);

        sampleTree.setRoot(newRoot, true);

        return new RandomVariable<>("\u03C8", sampleTree, this);
    }

    @Override
    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(EvolutionConstants.treeParamName, tree);
            put(rhoParamName, rho);
        }};
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(EvolutionConstants.treeParamName)) tree = value;
        else if (paramName.equals(rhoParamName)) rho = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);
    }

    public String toString() {
        return getName();
    }
}
