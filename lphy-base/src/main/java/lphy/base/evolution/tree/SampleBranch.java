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

public class SampleBranch extends ParametricDistribution<TimeTreeBranch> {
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

    @GeneratorInfo(name = "SampleBranch", description = "Randomly sample a branch among the branches at a given age in the given tree. The function would be deterministic when there is only one branch at the given age.")
    @Override
    public RandomVariable<TimeTreeBranch> sample() {
        // get parameters
        TimeTree tree = getTree().value();
        Double age = getAge().value();

        // get all the branches
        List<TimeTreeBranch> branches = tree.getBranches();

        // initialise a list to store branches with list
        List<TimeTreeBranch> filteredBranches = new ArrayList<>();

        // get the branches at age
        for (TimeTreeBranch branch : branches){
            double parentAge = branch.getParentNode().getAge();
            double childAge = branch.getChildNode().getAge();
            if (childAge >= age && age <= parentAge){
                filteredBranches.add(branch);
            }
        }

        if (filteredBranches.isEmpty()){
            throw new RuntimeException("There is no branches at age "+ age +  " !");
        } else {
            // randomly choose a branch in the list
            TimeTreeBranch sampledBranch = getSampledBranch(filteredBranches);
            return new RandomVariable<>(null, sampledBranch, this);
        }
    }

    private static TimeTreeBranch getSampledBranch(List<TimeTreeBranch> filteredBranches) {
        // convert the list to array
        TimeTreeBranch[] branches = filteredBranches.toArray(new TimeTreeBranch[0]);

        // get the Value<Integer> for the lower and upper boundary
        Value<Integer> lower = new Value<>("id", 0);
        Value<Integer> upper = new Value<>("id", branches.length-1);

        // get the random index for the integer in the array
        UniformDiscrete uniformDiscrete = new UniformDiscrete(lower, upper);
        RandomVariable<Integer> randomIndex = uniformDiscrete.sample();

        // get the sampled branch
        TimeTreeBranch sampledBranch = branches[randomIndex.value()];
        return sampledBranch;
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
