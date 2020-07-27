package lphy.toroidalDiffusion;

import beast.core.BEASTInterface;
import lphy.evolution.tree.TimeTree;
import lphy.evolution.tree.TimeTreeNode;
import lphy.core.StringDoubleArrayMap;
import lphy.core.distributions.Utils;
import lphy.graphicalModel.GenerativeDistribution;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.RandomVariable;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.types.DoubleArrayValue;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author Alexei Drummond
 */
public class PhyloWrappedBivariateDiffusion implements GenerativeDistribution<Map<String, Double[]>> {

    boolean anglesInRadians = true;

    // ANGLES IN RADIANS FOR THIS IMPLEMENTATIONS
    double MAX_ANGLE_VALUE = Math.PI * 2.0;

    Value<TimeTree> tree;
    Value<Double[]> mu;
    Value<Double[]> sigma;
    Value<Double[]> alpha;
    Value<Double[]> y;
    RandomGenerator random;

    String treeParamName;
    String muParamName;
    String sigmaParamName;
    String alphaParamName;
    String y0RateParam;

    public PhyloWrappedBivariateDiffusion(@ParameterInfo(name = "tree", description = "the time tree.") Value<TimeTree> tree,
                                          @ParameterInfo(name = "mu", description = "the mean of the stationary distribution.") Value<Double[]> mu,
                                          @ParameterInfo(name = "sigma", description = "the two variance terms.") Value<Double[]> sigma,
                                          @ParameterInfo(name = "alpha", description = "the three drift terms.") Value<Double[]> alpha,
                                          @ParameterInfo(name = "y", description = "the value of multivariate traits at the root.") Value<Double[]> y) {
        this.tree = tree;
        this.mu = mu;
        this.sigma = sigma;
        this.alpha = alpha;
        this.y = y;
        this.random = Utils.getRandom();

        treeParamName = getParamName(0);
        muParamName = getParamName(1);
        sigmaParamName = getParamName(2);
        alphaParamName = getParamName(3);
        y0RateParam = getParamName(4);
    }

    @Override
    public SortedMap<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        map.put(treeParamName, tree);
        map.put(muParamName, mu);
        map.put(sigmaParamName, sigma);
        map.put(alphaParamName, alpha);
        map.put(y0RateParam, y);
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(treeParamName)) tree = value;
        else if (paramName.equals(muParamName)) mu = value;
        else if (paramName.equals(sigmaParamName)) sigma = value;
        else if (paramName.equals(alphaParamName)) alpha = value;
        else if (paramName.equals(y0RateParam)) y = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);
    }

    public RandomVariable<Map<String, Double[]>> sample() {

        SortedMap<String, Integer> idMap = new TreeMap<>();
        fillIdMap(tree.value().getRoot(), idMap);

        Map<String, Double[]> tipValues = new StringDoubleArrayMap();

        WrappedBivariateDiffusion wrappedBivariateDiffusion = new WrappedBivariateDiffusion();

        wrappedBivariateDiffusion.setParameters(mu.value(), alpha.value(), sigma.value());

        traverseTree(tree.value().getRoot(), y, tipValues, wrappedBivariateDiffusion, idMap);

        return new RandomVariable<>("x", tipValues, this);
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

    private void traverseTree(TimeTreeNode node, Value<Double[]> nodeState, Map<String, Double[]> tipValues, WrappedBivariateDiffusion diffusion, Map<String, Integer> idMap) {
        if (node.isLeaf()) {
            tipValues.put(node.getId(), nodeState.value());
        } else {
            for (TimeTreeNode child : node.getChildren()) {

                double branchLength = node.getAge() - child.getAge();

                Double[] newValue = getNewValue(nodeState.value(), diffusion, branchLength);

                DoubleArrayValue ns = new DoubleArrayValue(null, newValue);

                traverseTree(child, ns, tipValues, diffusion, idMap);
            }
        }
    }

    Double[] getNewValue(Double[] oldValue, WrappedBivariateDiffusion diffusion, double branchLength) {

        Double[] newValues = new Double[oldValue.length];

        diffusion.setParameters(branchLength);
        for (int i = 0; i < oldValue.length; i += 2) {

            double[][] samples = diffusion.sampleByRejection(oldValue[i], oldValue[i + 1], 1);

            newValues[i] = samples[0][0];
            newValues[i + 1] = samples[0][1];
        }
        return newValues;
    }

}