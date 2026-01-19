package lphy.base.evolution.coalescent;


import lphy.base.distribution.DistributionConstants;
import lphy.base.evolution.Taxa;
import lphy.base.evolution.tree.TaxaConditionedTreeGenerator;
import lphy.base.evolution.tree.TimeTree;
import lphy.base.evolution.tree.TimeTreeNode;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.annotation.Citation;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

import java.util.*;

public class PopulationFunctionCoalescent extends TaxaConditionedTreeGenerator {
    public Value<PopulationFunction> popFunc;

    public static final String popFuncParamName = "popFunc";

    private static final double PARENT_AGE_EPS = 1e-12;

    /**
     * Constructs a coalescent model with specified population function, number of taxa, taxa object, and leaf node ages.
     * This constructor initializes the coalescent model with necessary parameters and checks for parameter consistency.
     *
     * @param popFunc The population size function, defining how population size changes over time.
     * @param n Optional. The number of taxa.
     * @param taxa Optional. The taxa object, could be a Taxa object or an array of taxa.
     * @param ages Optional. An array representing the ages of leaf nodes.
     */

    @Citation(value = "Norton, L. (1988). A Gompertzian Model of Human Breast Cancer Growth. Cancer Research",
            title = "A Gompertzian Model of Human Breast Cancer Growth",
            authors = {"Norton, L"}, year = 1988)
//"popFunc" instead of  "CoalescentConstants.thetaParamName"
    public PopulationFunctionCoalescent(@ParameterInfo(name = popFuncParamName, narrativeName = "population size function.", description = "the population size.") Value<PopulationFunction> popFunc,
                                        @ParameterInfo(name = DistributionConstants.nParamName, description = "number of taxa.", optional = true) Value<Integer> n,
                                        @ParameterInfo(name = TaxaConditionedTreeGenerator.taxaParamName, description = "Taxa object, (e.g. Taxa or Object[])", optional = true) Value<Taxa> taxa,
                                        @ParameterInfo(name = TaxaConditionedTreeGenerator.agesParamName, description = "an array of leaf node ages.", optional = true) Value<Double[]> ages) {

        super(n, taxa, ages);
        this.popFunc = popFunc;
        this.ages = ages;
        super.checkTaxaParameters(true);
        checkDimensions();
    }


    /**
     * Checks the dimensions of the input parameters to ensure they are consistent.
     * Throws IllegalArgumentException if the number of theta values does not match the number of taxa minus one.
     */

    private void checkDimensions() {
        boolean success = true;
        if (n != null && n.value() != n()) {
            success = false;
        }
        if (ages != null && ages.value().length != n()) {
            success = false;
        }
        if (!success) {
            throw new IllegalArgumentException("The number of theta values must be exactly one less than the number of taxa!");
        }
    }


    /**
     * Samples a coalescent tree based on Kingman's coalescent process and a population function with the possibility of serially sampled data.
     * It uses the population function to simulate the intervals between coalescent events and constructs the tree.
     *
     * @return A RandomVariable object encapsulating the simulated TimeTree.
     */
    @GeneratorInfo(name = "CoalescentPopFunc", narrativeName = "Kingman's coalescent tree prior with a population function",
            category = GeneratorCategory.COAL_TREE, examples = {"https://linguaphylo.github.io/tutorials/time-stamped-data/"},
            description = "The Kingman coalescent with serially sampled data. (Rodrigo and Felsenstein, 1999)")
    public RandomVariable<TimeTree> sample() {

        if (popFunc == null || popFunc.value() == null) {
            throw new IllegalStateException("popFunc is null.");
        }

        final PopulationFunction pf = popFunc.value();
        final TimeTree tree = new TimeTree();

        // Create all leaf taxa (each leaf already has its own age from Taxon/ages input)
        final List<TimeTreeNode> allLeaves = createLeafTaxa(tree);
        if (allLeaves.size() < 2) {
            throw new IllegalArgumentException("Coalescent requires at least 2 taxa.");
        }

        // Validate ages and group leaves by their sampling age
        final TreeMap<Double, List<TimeTreeNode>> leavesByAge = groupLeavesByAge(allLeaves);
        final List<Double> sampleTimes = new ArrayList<>(leavesByAge.keySet());
        Collections.sort(sampleTimes);

        // Start at the earliest sampling time
        int sampleIdx = 0;
        double time = sampleTimes.get(0);

        final List<TimeTreeNode> active = new ArrayList<>();
        active.addAll(leavesByAge.get(time));

        int remainingLeaves = allLeaves.size() - active.size();

        while (active.size() + remainingLeaves > 1) {

            final double nextSampleTime = (sampleIdx + 1 < sampleTimes.size())
                    ? sampleTimes.get(sampleIdx + 1)
                    : Double.POSITIVE_INFINITY;

            if (active.size() < 2) {
                if (!Double.isFinite(nextSampleTime)) {
                    throw new IllegalStateException("Cannot coalesce: active.size()<2 but no more samples remaining.");
                }
                time = nextSampleTime;
                sampleIdx++;
                List<TimeTreeNode> newLeaves = leavesByAge.get(time);
                active.addAll(newLeaves);
                remainingLeaves -= newLeaves.size();
                continue;
            }

            final int k = active.size();
            final double tCoal = proposeNextCoalescentTime(pf, k, time);

            if (tCoal < nextSampleTime) {
                // Coalescent happens before next sampling event
                time = tCoal;

                // Draw two DISTINCT lineages by removing from list
                final TimeTreeNode a = removeRandom(active);
                final TimeTreeNode b = removeRandom(active);

                final double minParentAge = Math.max(a.getAge(), b.getAge()) + PARENT_AGE_EPS;
                if (time <= minParentAge) time = minParentAge;

                final TimeTreeNode parent = new TimeTreeNode(time, new TimeTreeNode[]{a, b});
                active.add(parent);

            } else {
                time = nextSampleTime;
                sampleIdx++;

                List<TimeTreeNode> newLeaves = leavesByAge.get(time);
                active.addAll(newLeaves);
                remainingLeaves -= newLeaves.size();
            }
        }

        if (active.size() != 1) {
            throw new IllegalStateException("Internal error: expected exactly 1 active lineage at end, got " + active.size());
        }

        tree.setRoot(active.get(0));
        return new RandomVariable<>("\u03C8", tree, this);
    }

    /**
     * Group leaves by age. Reject negative/NaN ages because coalescent time must be >= 0 in this implementation.
     */
    private TreeMap<Double, List<TimeTreeNode>> groupLeavesByAge(List<TimeTreeNode> leaves) {
        final TreeMap<Double, List<TimeTreeNode>> map = new TreeMap<>();
        for (TimeTreeNode leaf : leaves) {
            double a = leaf.getAge();

            if (!Double.isFinite(a)) {
                throw new IllegalArgumentException("Leaf age is not finite for leaf " + leaf.getId() + ": " + a);
            }
            if (a < 0.0) {
                throw new IllegalArgumentException("Leaf age must be >= 0 for heterochronous coalescent. Leaf "
                        + leaf.getId() + " has age " + a);
            }

            // normalize -0.0 to 0.0
            if (a == 0.0) a = 0.0;

            map.computeIfAbsent(a, key -> new ArrayList<>()).add(leaf);
        }
        return map;
    }

    /**
     * Remove and return a random element from list (ensures distinct picks when called twice).
     */
    private TimeTreeNode removeRandom(List<TimeTreeNode> list) {
        int i = random.nextInt(list.size());
        return list.remove(i);
    }


    private double proposeNextCoalescentTime(PopulationFunction pf, int k, double t0) {
        double C = (k * (k - 1.0)) / 2.0;

        double u = random.nextDouble();
        while (u <= 0.0) u = random.nextDouble();
        double E = -Math.log(u);

        double targetI = pf.getIntensity(t0) + E / C;
        return pf.getInverseIntensity(targetI);
    }


    /**
     * Provides access to the parameters used in the coalescent model.
     * This method returns a map of parameter names to their values.
     *
     * @return A map of parameter names to Value objects.
     */
    @Override
    public Map<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        if (n != null) map.put(DistributionConstants.nParamName, n);
        if (taxaValue != null) map.put(taxaParamName, taxaValue);
        if (ages != null) map.put(agesParamName, ages);
        if (popFunc != null) map.put(popFuncParamName, popFunc);
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (popFuncParamName.equals(paramName)) {
            popFunc = value;
            return;
        }

        super.setParam(paramName, value);
    }


}

