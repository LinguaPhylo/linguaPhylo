package lphy.base.evolution.birthdeath;

import lphy.base.distributions.DistributionConstants;
import lphy.base.evolution.Taxa;
import lphy.base.evolution.tree.TaxaConditionedTreeGenerator;
import lphy.base.evolution.tree.TimeTree;
import lphy.base.evolution.tree.TimeTreeNode;
import lphy.core.model.components.*;
import org.apache.commons.math3.util.FastMath;

import java.util.List;
import java.util.Map;

/**
 * A Birth-death tree generative distribution
 */
@Citation(value="Tanja Stadler, Ziheng Yang (2013) Dating Phylogenies with Sequentially Sampled Tips, Systematic Biology, 62(5):674–688",
        title = "Dating Phylogenies with Sequentially Sampled Tips",
        DOI="10.1093/sysbio/syt030",
        authors = {"Stadler", "Yang"}, year=2013)
public class BirthDeathSerialSamplingTree extends TaxaConditionedTreeGenerator {

    private Value<Number> birthRate;
    private Value<Number> deathRate;
    private Value<Number> psiVal;
    private Value<Number> rhoVal;
    private Value<Number> rootAge;

    private double c1;
    private double c2;
    private double gt;

    public BirthDeathSerialSamplingTree(@ParameterInfo(name = BirthDeathConstants.lambdaParamName, description = "per-lineage birth rate.") Value<Number> birthRate,
                                        @ParameterInfo(name = BirthDeathConstants.muParamName, description = "per-lineage death rate.") Value<Number> deathRate,
                                        @ParameterInfo(name = BirthDeathConstants.rhoParamName, description = "proportion of extant taxa sampled.") Value<Number> rhoVal,
                                        @ParameterInfo(name = BirthDeathConstants.psiParamName, description = "per-lineage sampling-through-time rate.") Value<Number> psiVal,
                                        @ParameterInfo(name = DistributionConstants.nParamName, description = "the number of taxa. optional.", optional = true) Value<Integer> n,
                                        @ParameterInfo(name = TaxaConditionedTreeGenerator.taxaParamName, description = "Taxa object", optional = true) Value<Taxa> taxa,
                                        @ParameterInfo(name = TaxaConditionedTreeGenerator.agesParamName, description = "an array of leaf node ages.", optional = true) Value<Double[]> ages,
                                        @ParameterInfo(name = BirthDeathConstants.rootAgeParamName, description = "the age of the root.") Value<Number> rootAge) {

        super(n, taxa, ages);

        this.birthRate = birthRate;
        this.deathRate = deathRate;
        this.rhoVal = rhoVal;
        this.psiVal = psiVal;
        this.rootAge = rootAge;
        this.ages = ages;

        checkTaxaParameters(false);
    }

    @GeneratorInfo(name = "BirthDeathSerialSampling",
            category = GeneratorCategory.BD_TREE, examples = {"simpleBirthDeathSerial.lphy"},
            description = "A tree of extant species and those sampled through time, which is conceptually embedded in a full species tree produced by a speciation-extinction (birth-death) branching process.<br>" +
            "Conditioned on root age and on number of taxa and their ages (Stadler and Yang, 2013).")
    public RandomVariable<TimeTree> sample() {

        double lambda = ValueUtils.doubleValue(birthRate);
        double mu = ValueUtils.doubleValue(deathRate);
        double rho = ValueUtils.doubleValue(rhoVal);
        double psi = ValueUtils.doubleValue(psiVal);
        double tmrca = ValueUtils.doubleValue(rootAge);

        // calculate the constants in the simulating functions
        c1 = Math.sqrt(Math.pow(lambda - mu - psi, 2.0) + 4.0 * lambda * psi);
        c2 = -(lambda - mu - 2.0 * lambda * rho - psi) / c1;
        gt = 1.0 / (FastMath.exp(-c1 * tmrca) * (1.0 - c2) + (1.0 + c2));

        TimeTree tree = randomTreeTopology();
        tree.getRoot().setAge(tmrca);
        drawDivTimes(tree);

        constructTree(tree);

        return new RandomVariable<>("\u03C8", tree, this);
    }

    private TimeTree randomTreeTopology() {
        TimeTree tree = new TimeTree(getTaxa());
        List<TimeTreeNode> activeNodes = createLeafTaxa(tree);

        while (activeNodes.size() > 1) {
            TimeTreeNode a = drawRandomNode(activeNodes);
            TimeTreeNode b = drawRandomNode(activeNodes);
            TimeTreeNode parent = new TimeTreeNode(Math.max(a.getAge(), b.getAge()), new TimeTreeNode[]{a, b});
            activeNodes.add(parent);
        }

        tree.setRoot(activeNodes.get(0));
        return tree;
    }

    /*
     * This method traverses the tree from left to right (inorder)
     * and returns the order of index for internal node
     */
    private int traverseTree(TimeTreeNode node, int i, int[] index) {
        if (!node.isLeaf()) {
            i = traverseTree(node.getChild(0), i, index);
            index[i] = node.getIndex();
            i += 1;
            i = traverseTree(node.getChild(1), i, index);
        }
        return i;
    }

    private void drawDivTimes(TimeTree tree) {
        // index of leaf nodes
        int k;
        int[] index = new int[tree.n() - 1];
        traverseTree(tree.getRoot(), 0, index);

        // iterate internal nodes except the root
        for (int j : index) {
            if (j != tree.getRoot().getIndex()) {
                // step1: get z^* in Equation (4) in Stadler and Yang 2013
                // find tip on the left
                for (k = tree.getNodeByIndex(j).getChild(1).getIndex(); k >= tree.n(); k = tree.getNodeByIndex(k).getChild(0).getIndex())
                    ;
                double z0 = tree.getNodeByIndex(k).getAge();
                // find tip on the right
                for (k = tree.getNodeByIndex(j).getChild(0).getIndex(); k >= tree.n(); k = tree.getNodeByIndex(k).getChild(1).getIndex())
                    ;
                double z1 = tree.getNodeByIndex(k).getAge();
                double zstar = Math.max(z0, z1);

                // step2
                // calculate 1/g(z*)
                double gzstar = 1.0 / (FastMath.exp(-c1 * zstar) * (1.0 - c2) + (1.0 + c2));
                // a2 = (1/g(t_mrca)) - (1/g(z^*))
                double a2 = gt - gzstar;

                // step4
                // the constant part in the integral, which is H(zstar) and H is CDF of divergence times
                double constantChildren = 1.0 / (a2 * (((1 - c2) * Math.exp(-c1 * zstar)) + (1.0 + c2)));

                // step5: drawn a random number of Uniform(0,1)
                double y = random.nextDouble();

                // calculate the inverse function, i.e. H^(-1)
                double x;
                x = Math.log((1.0 / (a2 * (y + constantChildren) * (1.0 - c2))) - ((1.0 + c2) / (1.0 - c2))) / (-c1);

                // set the simulated divergence time
                tree.getNodeByIndex(j).setAge(x);
            }
        }
    }

    private void constructTree(TimeTree tree) {

        List<TimeTreeNode> nodes = tree.getNodes();
        // collect heights
        final double[] heights = new double[nodes.size()];
        final int[] reverseOrder = new int[nodes.size()];
        collectHeights(tree.getRoot(), heights, reverseOrder, 0);

        TimeTreeNode root = constructTree(nodes, heights, reverseOrder, 0, heights.length, new boolean[heights.length]);
        tree.setRoot(root);
    }

    private TimeTreeNode constructTree(List<TimeTreeNode> nodes, final double[] heights, final int[] reverseOrder, final int from, final int to, final boolean[] hasParent) {
        //nodeIndex = maxIndex(heights, 0, heights.length);
        int nodeIndex = -1;
        double max = Double.NEGATIVE_INFINITY;

        // finding the oldest internal node between from and to
        for (int j = from; j < to; j++) {
            if (max < heights[j] && !nodes.get(reverseOrder[j]).isLeaf()) {
                max = heights[j];
                nodeIndex = j;
            }
        }
        if (nodeIndex < 0) {
            return null;
        }

        // the oldest internal node between from and to
        final TimeTreeNode node = nodes.get(reverseOrder[nodeIndex]);

        // oldest node to the left that does not yet have a parent assigned by reconstructTree recursion
        int left = -1;
        max = Double.NEGATIVE_INFINITY;
        for (int j = from; j < nodeIndex; j++) {
            if (max < heights[j] && !hasParent[j]) {
                max = heights[j];
                left = j;
            }
        }

        // oldest node to the right that does not yet have a parent assigned by reconstructTree recursion
        int right = -1;
        max = Double.NEGATIVE_INFINITY;
        for (int j = nodeIndex + 1; j < to; j++) {
            if (max < heights[j] && !hasParent[j]) {
                max = heights[j];
                right = j;
            }
        }

        node.setLeft(nodes.get(reverseOrder[left]));
        node.setRight(nodes.get(reverseOrder[right]));
        if (node.getLeft().isLeaf()) {
            heights[left] = Double.NEGATIVE_INFINITY;
        }
        if (node.getRight().isLeaf()) {
            heights[right] = Double.NEGATIVE_INFINITY;
        }
        hasParent[left] = true;
        hasParent[right] = true;
        heights[nodeIndex] = Double.NEGATIVE_INFINITY;

        constructTree(nodes, heights, reverseOrder, from, nodeIndex, hasParent);
        constructTree(nodes, heights, reverseOrder, nodeIndex, to, hasParent);
        return node;
    }

    private int collectHeights(final TimeTreeNode node, final double[] heights, final int[] reverseOrder, int current) {

        if (node.isLeaf()) {
            heights[current] = node.getAge();
            reverseOrder[current] = node.getIndex();
            current++;
        } else {
            current = collectHeights(node.getLeft(), heights, reverseOrder, current);
            heights[current] = node.getAge();
            reverseOrder[current] = node.getIndex();
            current++;
            current = collectHeights(node.getRight(), heights, reverseOrder, current);
        }
        return current;
    }

    @Override
    public double logDensity(TimeTree timeTree) {
        throw new UnsupportedOperationException("Not implemented!");
    }

    @Override
    public Map<String, Value> getParams() {
        Map<String, Value> map = super.getParams();
        map.put(BirthDeathConstants.lambdaParamName, birthRate);
        map.put(BirthDeathConstants.muParamName, deathRate);
        map.put(BirthDeathConstants.rhoParamName, rhoVal);
        map.put(BirthDeathConstants.psiParamName, psiVal);
        map.put(BirthDeathConstants.rootAgeParamName, rootAge);
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        switch (paramName) {
            case BirthDeathConstants.lambdaParamName:
                birthRate = value;
                break;
            case BirthDeathConstants.muParamName:
                deathRate = value;
                break;
            case BirthDeathConstants.rhoParamName:
                rhoVal = value;
                break;
            case BirthDeathConstants.psiParamName:
                psiVal = value;
                break;
            case BirthDeathConstants.rootAgeParamName:
                rootAge = value;
                break;
            default:
                super.setParam(paramName, value);
                break;
        }
    }

    public Value<Number> getBirthRate() {
        return birthRate;
    }

    public Value<Number> getDeathRate() {
        return deathRate;
    }

    public Value<Number> getRho() {
        return rhoVal;
    }

    public Value<Number> getPsi() {
        return psiVal;
    }

    public Value<Number> getRootAge() {
        return rootAge;
    }
}