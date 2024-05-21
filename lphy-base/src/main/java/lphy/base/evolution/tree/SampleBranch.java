package lphy.base.evolution.tree;

import lphy.base.distribution.ParametricDistribution;
import lphy.base.distribution.UniformDiscrete;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.simulator.RandomUtils;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.*;

public class SampleBranch extends ParametricDistribution<TimeTreeNode> {
    Value<TimeTree> tree;
    Value<Double> age;
    public static final String treeParamName = "tree";
    public static final String ageParaName = "age";
    // use the random generator in this class
    protected RandomGenerator random;

    public SampleBranch(@ParameterInfo(name = treeParamName, description = "the full tree to sample branch from.") Value<TimeTree> tree,
                        @ParameterInfo(name = ageParaName, description = "the age that branch would sample at.") Value<Double> age){
        if (tree == null) throw new IllegalArgumentException("The tree cannot be null!");
        if (age == null) throw new IllegalArgumentException("The age should be specified!");
        setParam(treeParamName, tree);
        setParam(ageParaName, age);
        this.tree = tree;
        this.age = age;
        this.random = RandomUtils.getRandom();
    }
    @Override
    protected void constructDistribution(RandomGenerator random) {
    }

    @GeneratorInfo(name = "SampleBranch", description = "Randomly sample a branch among the branches at a given age in the given tree, representing by the node attached to this branch. The function would be deterministic when there is only one branch at the given age. The branch is represented by the node under it.")
    @Override
    public RandomVariable<TimeTreeNode> sample() {
        // get parameters
        TimeTree tree = getTree().value();
        Double age = getAge().value();

        // get all the nodes
        List<TimeTreeNode> nodes = tree.getNodes();

        // initialise a list to store nodes with list
        List<TimeTreeNode> filteredNodes = new ArrayList<>();

        // get the nodes at age
        for (TimeTreeNode node : nodes){
            if (!node.isRoot()) {
                double nodeAge = node.getAge();
                double parentAge = node.getParent().getAge();
                if (nodeAge >= age && age <= parentAge) {
                    filteredNodes.add(node);
                }
            }
        }

        if (filteredNodes.isEmpty()){
            throw new RuntimeException("There is no branches at age "+ age +  " !");
        } else {
            // randomly choose a branch in the list
            TimeTreeNode sampledNode = getSampledNode(filteredNodes);
            return new RandomVariable<>(null, sampledNode, this);
        }
    }

    private static TimeTreeNode getSampledNode(List<TimeTreeNode> filteredNodes) {
        // convert the list to array
        TimeTreeNode[] nodes = filteredNodes.toArray(new TimeTreeNode[0]);

        // get the Value<Integer> for the lower and upper boundary
        Value<Integer> lower = new Value<>("id", 0);
        Value<Integer> upper = new Value<>("id", nodes.length-1);

        // get the random index for the integer in the array
        UniformDiscrete uniformDiscrete = new UniformDiscrete(lower, upper);
        RandomVariable<Integer> randomIndex = uniformDiscrete.sample();

        // get the sampled branch
        return nodes[randomIndex.value()];
    }

    @Override
    public Map<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        if (tree != null) map.put(treeParamName, tree);
        if (age != null) map.put(ageParaName, age);
        return map;
    }

    public Value<TimeTree> getTree(){
        return getParams().get(treeParamName);
    }

    public Value<Double> getAge(){
        return getParams().get(ageParaName);
    }
}
