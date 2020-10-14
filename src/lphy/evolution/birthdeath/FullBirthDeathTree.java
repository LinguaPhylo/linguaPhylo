package lphy.evolution.birthdeath;

import lphy.core.distributions.Utils;
import lphy.evolution.tree.TimeTree;
import lphy.evolution.tree.TimeTreeNode;
import lphy.graphicalModel.*;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.*;

import static lphy.evolution.birthdeath.BirthDeathConstants.*;
import static lphy.graphicalModel.ValueUtils.doubleValue;

/**
 * A Birth-death tree generative distribution
 */
public class FullBirthDeathTree implements GenerativeDistribution<TimeTree> {

    private Value<Number> birthRate;
    private Value<Number> deathRate;
    private Value<Number> rootAge;

    private List<TimeTreeNode> activeNodes;

    RandomGenerator random;

    public FullBirthDeathTree(@ParameterInfo(name = lambdaParamName, description = "per-lineage birth rate.") Value<Number> birthRate,
                              @ParameterInfo(name = muParamName, description = "per-lineage death rate.") Value<Number> deathRate,
                              @ParameterInfo(name = rootAgeParamName, description = "the age of the tree.") Value<Number> rootAge) {

        this.birthRate = birthRate;
        this.deathRate = deathRate;
        this.rootAge = rootAge;
        this.random = Utils.getRandom();

        activeNodes = new ArrayList<>();
    }


    @GeneratorInfo(name = "FullBirthDeath", description = "A birth-death tree with both extant and extinct species.<br>" +
            "Conditioned on root age.")
    public RandomVariable<TimeTree> sample() {

        boolean success = false;
        TimeTree tree = new TimeTree();
        TimeTreeNode root = null;

        double lambda = doubleValue(birthRate);
        double mu = doubleValue(deathRate);

        while (!success) {
            activeNodes.clear();

            root = new TimeTreeNode(0 + "", tree);
            root.setAge(doubleValue(rootAge));

            activeNodes.add(root);

            double time = root.getAge();

            int[] nextNum = {1};
            doBirth(activeNodes, time, nextNum, tree);

            while (time > 0.0 && activeNodes.size() > 0) {
                int k = activeNodes.size();

                double totalRate = (lambda + mu) * (double) k;

                // random exponential variate
                double x = -Math.log(random.nextDouble()) / totalRate;
                time -= x;

                if (time < 0) break;


                double U = random.nextDouble();
                if (U < lambda / (lambda + mu)) {
                    doBirth(activeNodes, time, nextNum, tree);
                } else {
                    doDeath(activeNodes, time);
                }
            }

            for (TimeTreeNode node : activeNodes) {
                node.setAge(0.0);
            }

            System.out.println("activeLineages.size= " + activeNodes.size());

            success = activeNodes.size() > 0;
        }

        tree.setRoot(root);
        System.out.println("tree.n()=" + tree.n());
        System.out.println("tree.singleChildNodeCount()=" + tree.getSingleChildNodeCount());
        System.out.println("tree.getNodeCount()=" + tree.getNodeCount());

        return new RandomVariable<>("\u03C8", tree, this);
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
    }


    @Override
    public double logDensity(TimeTree timeTree) {

        throw new UnsupportedOperationException("Not implemented!");
    }

    @Override
    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(lambdaParamName, birthRate);
            put(muParamName, deathRate);
            put(rootAgeParamName, rootAge);
        }};
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(lambdaParamName)) birthRate = value;
        else if (paramName.equals(muParamName)) deathRate = value;
        else if (paramName.equals(rootAgeParamName)) rootAge = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);
    }

    public String toString() {
        return getName();
    }

}
