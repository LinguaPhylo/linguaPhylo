package james.core;

import james.TimeTree;
import james.TimeTreeNode;
import james.core.distributions.Utils;
import james.graphicalModel.*;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import java.util.*;

/**
 * Created by adru001 on 2/02/20.
 */
public class PhyloBrownian implements GenerativeDistribution<List<Double>> {

    Value<TimeTree> tree;
    Value<Double> diffusionRate;
    Value<Double> y0;
    Random random;

    String treeParamName;
    String diffusionRateParamName;
    String y0RateParam;

    int numStates;

    public PhyloBrownian(@ParameterInfo(name = "tree", description = "the time tree.") Value<TimeTree> tree,
                         @ParameterInfo(name = "r", description = "the diffusion rate.") Value<Double> diffusionRate,
                         @ParameterInfo(name = "y0", description = "the value of continuous trait at the root.") Value<Double> y0) {
        this.tree = tree;
        this.diffusionRate = diffusionRate;
        this.y0 = y0;
        this.random = Utils.getRandom();

        treeParamName = getParamName(0);
        diffusionRateParamName = getParamName(1);
        y0RateParam = getParamName(2);
    }

    @Override
    public SortedMap<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        map.put(treeParamName, tree);
        map.put(diffusionRateParamName, diffusionRate);
        map.put(y0RateParam, y0);
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(treeParamName)) tree = value;
        else if (paramName.equals(diffusionRateParamName)) diffusionRate = value;
        else if (paramName.equals(y0RateParam)) y0 = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);
    }

    public RandomVariable<List<Double>> sample() {

        SortedMap<String, Integer> idMap = new TreeMap<>();
        fillIdMap(tree.value().getRoot(), idMap);

        Double[] tipValues = new Double[tree.value().n()];

        traverseTree(tree.value().getRoot(), y0, tipValues, diffusionRate.value(), idMap);


        List<Double> tipValueList = Arrays.asList(tipValues);

        return new RandomVariable<>("x", tipValueList, this);
    }

    private void fillIdMap(TimeTreeNode node, SortedMap<String, Integer> idMap) {
        if (node.isLeaf()) {
            Integer i = idMap.get(node.getId());
            if (i == null) {
                int nextValue = 0;
                for (Integer j : idMap.values()) {
                    if (j >= nextValue) nextValue = j + 1;
                }
                idMap.put(node.getId(), nextValue);
            }
        } else {
            for (TimeTreeNode child : node.getChildren()) {
                fillIdMap(child, idMap);
            }
        }
    }

    private void traverseTree(TimeTreeNode node, Value<Double> nodeState, Double[] tipValues, double diffusionRate, Map<String, Integer> idMap) {
        if (node.isLeaf()) {
            tipValues[idMap.get(node.getId())] = nodeState.value();
        } else {
            for (TimeTreeNode child : node.getChildren()) {

                double variance = diffusionRate * (node.getAge() - child.getAge());

                //TODO I don't want to do a new on every branch! Should be made efficient :)
                NormalDistribution distribution = new NormalDistribution(nodeState.value(), Math.sqrt(variance));

                double newState = distribution.sample();

                traverseTree(child, new DoubleValue("x", newState), tipValues, diffusionRate, idMap);
            }
        }
    }
}
