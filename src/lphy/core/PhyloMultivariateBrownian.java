package lphy.core;

import lphy.evolution.alignment.ContinuousCharacterData;
import lphy.evolution.tree.TimeTree;
import lphy.evolution.tree.TimeTreeNode;
import lphy.core.distributions.Utils;
import lphy.graphicalModel.*;
import lphy.graphicalModel.types.DoubleArrayValue;
import org.apache.commons.math3.distribution.MultivariateNormalDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by adru001 on 2/02/20.
 */
public class PhyloMultivariateBrownian implements GenerativeDistribution<ContinuousCharacterData> {

    Value<TimeTree> tree;
    Value<Double[][]> diffusionMatrix;
    Value<Double[]> y;
    RandomGenerator random;

    String treeParamName;
    String diffusionRateParamName;
    String y0RateParam;

    public PhyloMultivariateBrownian(@ParameterInfo(name = "tree", description = "the time tree.") Value<TimeTree> tree,
                                     @ParameterInfo(name = "diffusionMatrix", description = "the multivariate diffusion rates.") Value<Double[][]> diffusionRate,
                                     @ParameterInfo(name = "y0", description = "the value of multivariate traits at the root.") Value<Double[]> y) {
        this.tree = tree;
        this.diffusionMatrix = diffusionRate;
        this.y = y;
        this.random = Utils.getRandom();

        treeParamName = getParamName(0);
        diffusionRateParamName = getParamName(1);
        y0RateParam = getParamName(2);
    }

    @Override
    public SortedMap<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        map.put(treeParamName, tree);
        map.put(diffusionRateParamName, diffusionMatrix);
        map.put(y0RateParam, y);
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(treeParamName)) tree = value;
        else if (paramName.equals(diffusionRateParamName)) diffusionMatrix = value;
        else if (paramName.equals(y0RateParam)) y = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);
    }

    public RandomVariable<ContinuousCharacterData> sample() {

        SortedMap<String, Integer> idMap = new TreeMap<>();
        fillIdMap(tree.value().getRoot(), idMap);

        Map<String, Double[]> tipValues = new StringDoubleArrayMap();

        traverseTree(tree.value().getRoot(), y, tipValues, diffusionMatrix.value(), idMap);

        Double[][] contData = new Double[tree.value().ntaxa()][y.value().length];
        for (Map.Entry<String, Double[]> entry : tipValues.entrySet()) {
            contData[tree.value().indexOfTaxon(entry.getKey())] = entry.getValue();
        }

        return new RandomVariable<>("x", new ContinuousCharacterData(tree.value(), contData), this);
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

    private void traverseTree(TimeTreeNode node, Value<Double[]> nodeState, Map<String, Double[]> tipValues, Double[][] diffusionMatrix, Map<String, Integer> idMap) {
        if (node.isLeaf()) {
            tipValues.put(node.getId(), nodeState.value());
        } else {
            for (TimeTreeNode child : node.getChildren()) {

                double branchLength = node.getAge() - child.getAge();

                Double[] newValue = getNewValue(nodeState.value(), diffusionMatrix, branchLength);

                DoubleArrayValue ns = new DoubleArrayValue(null, newValue);

                traverseTree(child, ns, tipValues, diffusionMatrix, idMap);
            }
        }
    }

    Double[] getNewValue(Double[] oldValue, Double[][] diffusionMatrix, double branchLength) {
        double[] means = new double[oldValue.length];
        double[][] covariances = new double[diffusionMatrix.length][diffusionMatrix[0].length];
        for (int i = 0; i < covariances.length; i++) {
            means[i] = oldValue[i];
            for (int j = 0; j < covariances.length; j++) {
                covariances[i][j] = diffusionMatrix[i][j] * branchLength;
            }
        }

        MultivariateNormalDistribution mvn = new MultivariateNormalDistribution(means, covariances);
        return handleBoundaries(mvn.sample());

    }

    protected Double[] handleBoundaries(double[] rawValues) {

        Double[] ns2 =  new Double[rawValues.length];
        for (int i = 0; i < rawValues.length; i++) {ns2[i] = rawValues[i];}

        return ns2;
    }
}
