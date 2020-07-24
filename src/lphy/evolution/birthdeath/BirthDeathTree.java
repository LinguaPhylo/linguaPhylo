package lphy.evolution.birthdeath;

import beast.core.BEASTInterface;
import lphy.core.distributions.Utils;
import lphy.evolution.tree.TimeTree;
import lphy.evolution.tree.TimeTreeNode;
import lphy.graphicalModel.*;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.*;

/**
 * A Birth-death tree generative distribution
 */
public class BirthDeathTree implements GenerativeDistribution<TimeTree> {

    final String birthRateParamName;
    final String deathRateParamName;
    final String rootAgeParamName;
    private Value<Double> birthRate;
    private Value<Double> deathRate;
    private Value<Double> rootAge;

    private List<TimeTreeNode> activeNodes;

    RandomGenerator random;

    public BirthDeathTree(@ParameterInfo(name = "lambda", description = "per-lineage birth rate.") Value<Double> birthRate,
                          @ParameterInfo(name = "mu", description = "per-lineage death rate.") Value<Double> deathRate,
                          @ParameterInfo(name = "rootAge", description = "the number of taxa.") Value<Double> rootAge
                          ) {

        this.birthRate = birthRate;
        this.deathRate = deathRate;
        this.rootAge = rootAge;
        this.random = Utils.getRandom();

        birthRateParamName = getParamName(0);
        deathRateParamName = getParamName(1);
        rootAgeParamName = getParamName(2);

        activeNodes = new ArrayList<>();
    }


    @GeneratorInfo(name="BirthDeath", description="The Birth-death-sampling tree distribution over tip-labelled time trees.<br>" +
            "Conditioned on root age.")
    public RandomVariable<TimeTree> sample() {

        boolean success = false;
        TimeTree tree = new TimeTree();
        TimeTreeNode root = null;
        while (!success) {
            activeNodes.clear();

            root = new TimeTreeNode(0 + "", tree);
            root.setAge(rootAge.value());

            activeNodes.add(root);

            double time = root.getAge();
            double birthRate = this.birthRate.value();
            double deathRate = this.deathRate.value();

            int[] nextNum = {1};
            doBirth(activeNodes, time, nextNum, tree);

            while (time > 0.0 && activeNodes.size() > 0) {
                int k = activeNodes.size();

                double totalRate = (birthRate + deathRate) * (double) k;

                // random exponential variate
                double x = -Math.log(random.nextDouble()) / totalRate;
                time -= x;

                if (time < 0) break;


                double U = random.nextDouble();
                if (U < birthRate / (birthRate + deathRate)) {
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
    public SortedMap<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        map.put(birthRateParamName, birthRate);
        map.put(deathRateParamName, deathRate);
        map.put(rootAgeParamName, rootAge);
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(birthRateParamName)) birthRate = value;
        else if (paramName.equals(deathRateParamName)) deathRate = value;
        else if (paramName.equals(rootAgeParamName)) rootAge = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);
    }

    public String toString() {
        return getName();
    }

    public BEASTInterface toBEAST(BEASTInterface value, Map beastObjects) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + ".toBEAST not implemented yet!");
    }
}
