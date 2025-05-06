package lphy.base.evolution.continuous;

import lphy.base.evolution.tree.TimeTree;
import lphy.base.evolution.tree.TimeTreeNode;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.ParameterInfo;

import java.util.*;

/**
 * AutoCorrelatedClock:
 * A DeterministicFunction that calculates each branch's average rate E(Z)
 * using the parent's and child's log-rates under a Brownian bridging formula.
 */
public class AutoCorrelatedClock extends DeterministicFunction<Double[]> {

    public static final String TREE = "tree";
    public static final String NODE_LOG_RATES = "nodeLogRates";
    public static final String ROOT_LOG_RATE = "rootLogRate";
    public static final String SIGMA2 = "sigma2";
    public static final String MEAN_RATE = "meanRate";
    public static final String NORMALIZE = "normalize";
    public static final String TAYLOR_ORDER = "taylorOrder";

    protected Value<TimeTree> tree;
    protected Value<Double[]> nodeLogRates;
    protected Value<Double> rootLogRate;
    protected Value<Double> sigma2;
    protected Value<Double> meanRate;
    protected Value<Boolean> normalize;
    protected Value<Integer> taylorOrder;

    protected int nNodes;
    protected int rootIndex;
    protected int[] nodeIndexMapping;

    public AutoCorrelatedClock(
            @ParameterInfo(name = TREE, description = "") Value<TimeTree> tree,
            @ParameterInfo(name = NODE_LOG_RATES, description = "") Value<Double[]> nodeLogRates,
            @ParameterInfo(name = ROOT_LOG_RATE, description = "") Value<Double> rootLogRate,
            @ParameterInfo(name = SIGMA2, description = "") Value<Double> sigma2,
            @ParameterInfo(name = MEAN_RATE, description = "", optional = true) Value<Double> meanRate,
            @ParameterInfo(name = NORMALIZE, description = "", optional = true) Value<Boolean> normalize,
            @ParameterInfo(name = TAYLOR_ORDER, description = "", optional = true) Value<Integer> taylorOrder
    ){
        this.tree = tree;
        this.nodeLogRates = nodeLogRates;
        this.rootLogRate = rootLogRate;
        this.sigma2 = sigma2;
        this.meanRate = meanRate;
        this.normalize = normalize;
        this.taylorOrder = taylorOrder;
        initMapping();
    }

    private void initMapping(){
        TimeTree t = tree.value();
        TimeTreeNode root = t.getRoot();
        rootIndex = root.getIndex();

        List<TimeTreeNode> all = new ArrayList<>();
        collectAllNodes(root, all);
        nNodes = all.size();

        nodeIndexMapping = new int[nNodes];
        int idx = 0;
        for(TimeTreeNode nd : all){
            int i = nd.getIndex();
            if(i == rootIndex){
                nodeIndexMapping[i] = -1;
            } else {
                nodeIndexMapping[i] = idx;
                idx++;
            }
        }
    }

    @Override
    public Map<String, Value> getParams() {
        Map<String,Value> map = new TreeMap<>();
        map.put(TREE, tree);
        map.put(NODE_LOG_RATES, nodeLogRates);
        map.put(ROOT_LOG_RATE, rootLogRate);
        map.put(SIGMA2, sigma2);
        if(meanRate != null) {
            map.put(MEAN_RATE, meanRate);
        }
        if(normalize != null) {
            map.put(NORMALIZE, normalize);
        }
        if(taylorOrder != null) {
            map.put(TAYLOR_ORDER, taylorOrder);
        }
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        switch(paramName){
            case TREE -> tree = value;
            case NODE_LOG_RATES -> nodeLogRates = value;
            case ROOT_LOG_RATE -> rootLogRate = value;
            case SIGMA2 -> sigma2 = value;
            case MEAN_RATE -> meanRate = value;
            case NORMALIZE -> normalize = value;
            case TAYLOR_ORDER -> taylorOrder = value;
            default -> throw new RuntimeException("Unknown param: " + paramName);
        }
    }

    @Override
    public Value<Double[]> apply() {
        TimeTree t = tree.value();
        double[] branchRates = new double[nNodes];
        Arrays.fill(branchRates, 0.0);

        Double[] nrArray = nodeLogRates.value();
        double vRoot = rootLogRate.value();
        double phi = sigma2.value();
        double globalRate = (meanRate != null ? meanRate.value() : 1.0);
        boolean doNorm = (normalize != null ? normalize.value() : false);
        int order = (taylorOrder != null ? taylorOrder.value() : 10);

        List<TimeTreeNode> all = new ArrayList<>();
        collectAllNodes(t.getRoot(), all);

        for(TimeTreeNode node : all){
            if(!node.isRoot()){
                TimeTreeNode parent = node.getParent();
                double vPar = parent.isRoot() ? vRoot :
                        nrArray[nodeIndexMapping[parent.getIndex()]];
                double vChi = nrArray[nodeIndexMapping[node.getIndex()]];

                double rPar = Math.exp(vPar);
                double rChi = Math.exp(vChi);

                double dt = parent.getAge() - node.getAge();
                if(dt < 0.0) {
                    dt = 0.0;
                }
                double eZ = computeMeanZ(rPar, rChi, dt, phi, order);
                branchRates[node.getIndex()] = eZ * globalRate;
            } else {
                branchRates[node.getIndex()] = 0.0;
            }
        }

        if(doNorm){
            double sumRate = 0.0, sumTime = 0.0;
            for(TimeTreeNode node : all){
                if(!node.isRoot()){
                    double dt = node.getParent().getAge() - node.getAge();
                    if(dt < 0) dt = 0;
                    sumRate += branchRates[node.getIndex()] * dt;
                    sumTime += dt;
                }
            }
            if(sumRate > 0){
                double scale = sumTime / sumRate;
                for(TimeTreeNode node : all){
                    branchRates[node.getIndex()] *= scale;
                }
            }
        }

        Double[] out = new Double[nNodes];
        for(int i = 0; i < nNodes; i++){
            out[i] = branchRates[i];
        }
        return new Value<>(out, this);
    }

    private double computeMeanZ(double r0, double rt, double t, double phi, int order) {
        if(t <= 0.0) {
            return r0;
        }
        return MeanZCalculator.computeMeanZ(r0, rt, t, phi, order);
    }

    private void collectAllNodes(TimeTreeNode node, List<TimeTreeNode> out){
        out.add(node);
        for(TimeTreeNode c : node.getChildren()){
            collectAllNodes(c, out);
        }
    }

    public Value<TimeTree> getTree() {
        return tree;
    }

    public Value<Double[]> getNodeLogRates() {
        return nodeLogRates;
    }

    public Value<Double> getSigma2() {
        return sigma2;
    }

    public Value<Double> getRootLogRate() {
        return rootLogRate;
    }

    public Value<Double> getMeanRate() {
        return meanRate;
    }

    public Value<Boolean> getNormalize() {
        return normalize;
    }

    public Value<Integer> getTaylorOrder() {
        return taylorOrder;
    }
}
