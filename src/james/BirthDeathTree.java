package james;

import james.core.distributions.Utils;
import james.graphicalModel.*;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.*;

/**
 * A Yule tree generative distribution
 */
public class BirthDeathTree implements GenerativeDistribution<TimeTree> {

    private final String birthRateParamName;
    private final String deathRateParamName;
    private final String rhoParamName;
    private final String rootAgeParamName;
    private Value<Double> birthRate;
    private Value<Double> deathRate;
    private Value<Double> rho;
    private Value<Double> rootAge;

    private List<TimeTreeNode> activeNodes;

    RandomGenerator random;

    public BirthDeathTree(@ParameterInfo(name = "lambda", description = "per-lineage birth rate.") Value<Double> birthRate,
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


    @GenerativeDistributionInfo(name="BirthDeath", description="The Birth-death-sampling tree distribution over tip-labelled time trees.<br>" +
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

            System.out.println("activeLineages.size= " + activeNodes.size());

            int numToRemove = 0;
            int numToKeep = 0;
            for (TimeTreeNode node : activeNodes) {
                node.setAge(0.0);
                if (random.nextDouble() > rho.value()) {
                    markForRemoval(node);
                    numToRemove += 1;
                } else {
                    numToKeep += 1;
                }
            }
            System.out.println("numToRemove= " + numToRemove);
            System.out.println("numToKeep= " + numToKeep);

            removeAllMarked(root);

            success = (numToKeep > 0);
        }

        tree.setRoot(root);
        System.out.println("tree.n()=" + tree.n());

        return new RandomVariable<>("\u03C8", tree, this);
    }

    private void removeAllMarked(TimeTreeNode node) {

        if (!node.isLeaf()) {
            for (TimeTreeNode child : node.getChildren()) {
                removeAllMarked(child);
            }

            Set<TimeTreeNode> nodesToRemove = new HashSet<>();
            for (TimeTreeNode child : node.getChildren()) {
                if (markedForRemoval(child)) nodesToRemove.add(child);
            }
            for (TimeTreeNode child : nodesToRemove) {
                node.removeChild(child);
            }
            if (node.getChildCount() == 0) {
                markForRemoval(node);
            }
        }
    }

    private boolean markedForRemoval(TimeTreeNode node) {
        Object remove = node.getMetaData("remove");
        return (remove != null && remove instanceof Boolean && (Boolean)remove);
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
