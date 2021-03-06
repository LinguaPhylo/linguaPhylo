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
    private Value<Number> originAge;

    private List<TimeTreeNode> activeNodes;

    RandomGenerator random;

    private static final int MAX_ATTEMPTS = 1000;

    public FullBirthDeathTree(@ParameterInfo(name = lambdaParamName, description = "per-lineage birth rate.") Value<Number> birthRate,
                              @ParameterInfo(name = muParamName, description = "per-lineage death rate.") Value<Number> deathRate,
                              @ParameterInfo(name = rootAgeParamName, description = "the age of the root of the tree (only one of rootAge and originAge may be specified).", optional=true) Value<Number> rootAge,
                              @ParameterInfo(name = originAgeParamName, description = "the age of the origin of the tree  (only one of rootAge and originAge may be specified).", optional=true) Value<Number> originAge) {

        this.birthRate = birthRate;
        this.deathRate = deathRate;
        this.rootAge = rootAge;
        this.originAge = originAge;
        this.random = Utils.getRandom();

        if (rootAge != null && originAge != null) throw new IllegalArgumentException("Only one of rootAge and originAge may be specified!");
        if (rootAge == null && originAge == null) throw new IllegalArgumentException("One of rootAge and originAge must be specified!");

        activeNodes = new ArrayList<>();
    }


    @GeneratorInfo(name = "FullBirthDeath", description = "A birth-death tree with both extant and extinct species.<br>" +
            "Conditioned on age of root or origin.")
    public RandomVariable<TimeTree> sample() {

        boolean success = false;
        TimeTree tree = new TimeTree();
        TimeTreeNode root = null;

        double lambda = doubleValue(birthRate);
        double mu = doubleValue(deathRate);

        int attempts = 0;

        while (!success && attempts < MAX_ATTEMPTS) {
            activeNodes.clear();

            root = new TimeTreeNode((String)null, tree);
            if (rootAge != null) {
                root.setAge(doubleValue(rootAge));
            } else {
                root.setAge(doubleValue(originAge));
            }


            double time = root.getAge();

            if (rootAge != null) {
                activeNodes.add(root);
                doBirth(activeNodes, time, tree);
            } else {
                TimeTreeNode origin = root;
                root = new TimeTreeNode((String)null, tree);
                root.setAge(origin.getAge());
                origin.addChild(root);
                activeNodes.add(root);
                root = root.getParent();
            }

            while (time > 0.0 && activeNodes.size() > 0) {
                int k = activeNodes.size();

                double totalRate = (lambda + mu) * (double) k;

                // random exponential variate
                double x = -Math.log(random.nextDouble()) / totalRate;
                time -= x;

                if (time < 0) break;

                double U = random.nextDouble();
                if (U < lambda / (lambda + mu)) {
                    doBirth(activeNodes, time, tree);
                } else {
                    doDeath(activeNodes, time);
                }
            }

            int number = 0;
            for (TimeTreeNode node : activeNodes) {
                node.setAge(0.0);
                node.setId(number+"");
                number += 1;
            }

            success = activeNodes.size() > 0;
            attempts += 1;
        }

        if (!success) {
            throw new RuntimeException("Failed to simulated FullBirthDeathTree after " + MAX_ATTEMPTS + " attempts.");
        }

        tree.setRoot(root, true);

        return new RandomVariable<>(null, tree, this);
    }

    private void doBirth(List<TimeTreeNode> activeNodes, double age, TimeTree tree) {
        TimeTreeNode parent = activeNodes.remove(random.nextInt(activeNodes.size()));
        parent.setAge(age);
        TimeTreeNode child1 = new TimeTreeNode((String)null, tree);
        TimeTreeNode child2 = new TimeTreeNode((String)null, tree);
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
            if (rootAge != null) put(rootAgeParamName, rootAge);
            if (originAge != null) put(originAgeParamName, originAge);
        }};
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(lambdaParamName)) birthRate = value;
        else if (paramName.equals(muParamName)) deathRate = value;
        else if (paramName.equals(rootAgeParamName)) rootAge = value;
        else if (paramName.equals(originAgeParamName)) originAge = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);
    }

    public String toString() {
        return getName();
    }

}
