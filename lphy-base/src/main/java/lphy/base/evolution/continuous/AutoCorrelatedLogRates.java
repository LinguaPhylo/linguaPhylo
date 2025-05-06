package lphy.base.evolution.continuous;

import lphy.base.distribution.ParametricDistribution;
import lphy.base.evolution.tree.TimeTree;
import lphy.base.evolution.tree.TimeTreeNode;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.*;

public class AutoCorrelatedLogRates extends ParametricDistribution<Double[]> {

    public static final String TREE = "tree";
    public static final String SIGMA2 = "sigma2";
    public static final String ROOT_LOG_RATE = "rootLogRate";
    public static final String NODE_LOG_RATES = "nodeLogRates";

    protected Value<TimeTree> tree;
    protected Value<Double> sigma2;
    protected Value<Double> rootLogRate;
    protected Value<Double[]> nodeLogRates;

    protected int nNodes;
    protected int rootIndex;
    protected int[] nodeIndexMapping;
    protected int nNonRootNodes;

    public AutoCorrelatedLogRates(
            @ParameterInfo(name = TREE, description = "") Value<TimeTree> tree,
            @ParameterInfo(name = SIGMA2, description = "") Value<Double> sigma2,
            @ParameterInfo(name = ROOT_LOG_RATE, description = "") Value<Double> rootLogRate,
            @ParameterInfo(name = NODE_LOG_RATES, description = "", optional = true) Value<Double[]> nodeLogRates
    ) {
        super();
        this.tree = tree;
        this.sigma2 = sigma2;
        this.rootLogRate = rootLogRate;
        this.nodeLogRates = nodeLogRates;
        constructDistribution(random);
    }

    @Override
    protected void constructDistribution(RandomGenerator random) {
        TimeTree t = tree.value();
        TimeTreeNode root = t.getRoot();
        rootIndex = root.getIndex();

        List<TimeTreeNode> allNodes = new ArrayList<>();
        collectAllNodes(root, allNodes);
        nNodes = allNodes.size();

        int maxIdx = -1;
        for (TimeTreeNode nd : allNodes) {
            maxIdx = Math.max(maxIdx, nd.getIndex());
        }
        nodeIndexMapping = new int[maxIdx + 1];
        Arrays.fill(nodeIndexMapping, -1);

        int idx = 0;
        for (TimeTreeNode nd : allNodes) {
            int i = nd.getIndex();
            if (i == rootIndex) {
                nodeIndexMapping[i] = -1;
            } else {
                nodeIndexMapping[i] = idx;
                idx++;
            }
        }
        nNonRootNodes = idx;
    }

    @Override
    public Map<String, Value> getParams() {
        Map<String, Value> map = new TreeMap<>();
        map.put(TREE, tree);
        map.put(SIGMA2, sigma2);
        map.put(ROOT_LOG_RATE, rootLogRate);
        if (nodeLogRates != null) {
            map.put(NODE_LOG_RATES, nodeLogRates);
        }
        return map;
    }

    @Override
    public void setParam(String paramName, Value<?> value) {
        switch(paramName) {
            case TREE -> tree = (Value<TimeTree>) value;
            case SIGMA2 -> sigma2 = (Value<Double>) value;
            case ROOT_LOG_RATE -> rootLogRate = (Value<Double>) value;
            case NODE_LOG_RATES -> nodeLogRates = (Value<Double[]>) value;
            default -> throw new RuntimeException("Unrecognized param: " + paramName);
        }
        super.setParam(paramName, value);
    }

    @GeneratorInfo(
            name = "AutoCorrelatedLogRates",
            verbClause = "are assumed to evolve under a parent-child Brownian increment",
            narrativeName = "auto-correlated log-rates",
            category = GeneratorCategory.PHYLO_LIKELIHOOD,
            examples = {"autoCorrelatedClock.lphy"},
            description = """
      This parametric distribution generates node-specific log-rates by a Brownian increment process
      along the given time tree. The root node has a specified log-rate, and each child node's log-rate
      is drawn from Normal( parentLogRate, sigma^2 * dt ), where dt is the time between parent and child.
      This leads to an auto-correlated relaxation of the molecular clock across lineages.
      """)


    @Override
    public RandomVariable<Double[]> sample() {
        TimeTree t = tree.value();
        TimeTreeNode root = t.getRoot();
        Double[] arr = new Double[nNonRootNodes];
        double vRoot = rootLogRate.value();
        sampleNodeRecursively(root, vRoot, arr);
        return new RandomVariable<>(null, arr, this);
    }

    private void sampleNodeRecursively(TimeTreeNode parent, double vPar, Double[] arr) {
        for (TimeTreeNode child : parent.getChildren()) {
            double dt = parent.getAge() - child.getAge();
            if (dt < 0) {
                dt = 0.0;
            }
            double var = sigma2.value() * dt;
            NormalDistribution dist = new NormalDistribution(
                    random, vPar, Math.sqrt(var),
                    NormalDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY
            );
            double vChild = dist.sample();

            int childIdx = child.getIndex();
            int mapIndex = nodeIndexMapping[childIdx];
            arr[mapIndex] = vChild;

            sampleNodeRecursively(child, vChild, arr);
        }
    }

    @Override
    public double logDensity(Double[] x) {
        TimeTree t = tree.value();
        TimeTreeNode root = t.getRoot();
        double vRoot = rootLogRate.value();
        return logDensityRecursively(root, vRoot, x);
    }

    private double logDensityRecursively(TimeTreeNode parent, double vPar, Double[] x) {
        double s2 = sigma2.value();
        double sumLog = 0.0;
        for (TimeTreeNode child : parent.getChildren()) {
            int childIdx = child.getIndex();
            int mapIndex = nodeIndexMapping[childIdx];
            double vChi = x[mapIndex];

            double dt = parent.getAge() - child.getAge();
            if (dt < 0) {
                dt = 0.0;
            }
            double var = s2 * dt;
            if (var <= 0) {
                return Double.NEGATIVE_INFINITY;
            }
            double diff = vChi - vPar;
            double term = -0.5 * (Math.log(2.0 * Math.PI * var) + (diff * diff) / var);
            sumLog += term;

            sumLog += logDensityRecursively(child, vChi, x);
        }
        return sumLog;
    }

    private void collectAllNodes(TimeTreeNode node, List<TimeTreeNode> nodeList){
        nodeList.add(node);
        for (TimeTreeNode c : node.getChildren()) {
            collectAllNodes(c, nodeList);
        }
    }

    public Value<TimeTree> getTree() {
        return tree;
    }

    public Value<Double> getSigma2() {
        return sigma2;
    }

    public Value<Double> getRootLogRate() {
        return rootLogRate;
    }

    public Value<Double[]> getNodeLogRates() {
        return nodeLogRates;
    }
}
