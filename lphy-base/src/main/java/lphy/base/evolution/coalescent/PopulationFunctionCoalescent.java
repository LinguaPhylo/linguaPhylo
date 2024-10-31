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

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class PopulationFunctionCoalescent extends TaxaConditionedTreeGenerator {
    public Value<PopulationFunction> popFunc;

    public static final String popFuncParamName = "popFunc";

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
        TimeTree tree = new TimeTree();

        List<TimeTreeNode> activeNodes = createLeafTaxa(tree);

        double time = 0.0;

        while (activeNodes.size() > 1) {
            int lineageCount = activeNodes.size();

            PopulationFunction pf = popFunc.value();

            double interval = 0;
            // Use the Utils.getSimulatedInterval method to calculate the time interval for the next coalescent event
            interval = Utils.getSimulatedInterval(pf, lineageCount, time);

            // Update the current time, plus the newly calculated time interval
            time += interval;

            // Randomly select two nodes to coalescent
            TimeTreeNode a = drawRandomNode(activeNodes);
            TimeTreeNode b = drawRandomNode(activeNodes);

            // Create a new parent node and update the list of active nodes
            TimeTreeNode parent = new TimeTreeNode(time, new TimeTreeNode[] {a, b});
            activeNodes.add(parent);

            // Remove two coalescent nodes from the active node list
            activeNodes.remove(a);
            activeNodes.remove(b);
        }

        // Set the root node of the tree
        tree.setRoot(activeNodes.get(0));

        return new RandomVariable<>("\u03C8", tree, this);


    }

// constant coalescent code below:
//
//    public RandomVariable<TimeTree> sample() {
//        TimeTree tree = new TimeTree();
//
//        List<TimeTreeNode> activeNodes = createLeafTaxa(tree);
//
//        double time = 0.0;
//        double theta = popFunc.value().getTheta(time); // wrong
//
//        while (activeNodes.size() > 1) {
//            int k = activeNodes.size();
//
//            TimeTreeNode a = drawRandomNode(activeNodes);
//            TimeTreeNode b = drawRandomNode(activeNodes);
//
//            double rate = (k * (k - 1.0))/(theta * 2.0);
//
//            // random exponential variate
//            double x = - Math.log(random.nextDouble()) / rate;
//            time += x;
//
//            TimeTreeNode parent = new TimeTreeNode(time, new TimeTreeNode[] {a, b});
//            activeNodes.add(parent);
//        }
//
//        tree.setRoot(activeNodes.get(0));
//
//
//
//        return new RandomVariable<>("\u03C8", tree, this);
//    }


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

