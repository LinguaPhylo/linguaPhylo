package james.core;

import james.TimeTree;
import james.TimeTreeNode;
import james.core.distributions.Utils;
import james.graphicalModel.*;
import james.graphicalModel.types.DoubleValue;

import java.util.*;

/**
 * Created by adru001 on 2/02/20.
 */
public class JCPhyloCTMC implements GenerativeDistribution<Alignment> {

    Value<TimeTree> tree;
    Value<Integer> L;
    Value<Integer> dim;
    Value<Double> clockRate;
    Value<Double[]> branchRates;
    Random random;

    String treeParamName;
    String muParamName;
    String LParamName;
    String dimParamName;

    int[][] sequences;


    public JCPhyloCTMC(@ParameterInfo(name = "tree", description = "the time tree.") Value<TimeTree> tree,
                       @ParameterInfo(name = "mu", description = "the clock rate.") Value<Double> mu,
                       @ParameterInfo(name = "L", description = "the length of the alignment to generate.") Value<Integer> L,
                       @ParameterInfo(name = "dim", description = "the number of possible states in the markov chain.") Value<Integer> dim) {
        this.tree = tree;
        this.clockRate = mu;
        this.L = L;
        this.dim = dim;
        this.random = Utils.getRandom();

        treeParamName = getParamName(0);
        muParamName = getParamName(1);
        LParamName = getParamName(2);
        dimParamName = getParamName(3);

        sequences = new int[tree.value().getNodeCount()][];
    }

    public RandomVariable<Alignment> sample() {

        SortedMap<String, Integer> idMap = new TreeMap<>();
        fillIdMap(tree.value().getRoot(), idMap);

        GenerativeDistribution<Integer> rootDistribution =
                new DiscreteDistribution(new Value<>("rootProb", new double[] {0.25, 0.25, 0.25, 0.25}),random);


        Alignment alignment = new Alignment(tree.value().n(), L.value(), idMap);

        for (int i = 0; i < L.value(); i++) {
            Value<Integer> rootState = rootDistribution.sample();
            traverseTree(tree.value().getRoot(), rootState, alignment, i);
        }

        return new RandomVariable<>("D", alignment, this);
    }

    private void fillIdMap(TimeTreeNode node, SortedMap<String, Integer> idMap) {
        if (node.isLeaf()) {
            Integer i = idMap.get(node.getId());
            if (i == null) {
                int nextValue = 0;
                for (Integer j : idMap.values()) {
                    if (j>=nextValue) nextValue = j+1;
                }
                idMap.put(node.getId(), nextValue);
            }
        } else {
            for (TimeTreeNode child : node.getChildren()) {
                fillIdMap(child, idMap);
            }
        }
    }

    private void traverseTree(TimeTreeNode node, Value<Integer> nodeState, Alignment alignment, int pos) {
        if (node.isLeaf()) {
            alignment.setState(node.getId(), pos, nodeState.value());
        } else {
            for (TimeTreeNode child : node.getChildren()) {
                double rate = clockRate.value();

                JukesCantorCTMC jc = new JukesCantorCTMC(nodeState, new DoubleValue("d", (node.getAge()-child.getAge())*rate), dim, random);
                traverseTree(child, jc.sample(), alignment, pos);
            }
        }
    }

    @Override
    public SortedMap<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        map.put(treeParamName, tree);
        map.put(muParamName, clockRate);
        map.put(LParamName, L);
        map.put(dimParamName, dim);
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(treeParamName)) tree = value;
        else if (paramName.equals(muParamName)) clockRate = value;
        else if (paramName.equals(LParamName)) L = value;
        else if (paramName.equals(dimParamName)) dim = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);
    }

    public Value<TimeTree> getTimeTree() {
        return tree;
    }
}
