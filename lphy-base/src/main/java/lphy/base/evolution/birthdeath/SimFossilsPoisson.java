package lphy.base.evolution.birthdeath;

import lphy.base.evolution.Taxon;
import lphy.base.evolution.tree.TimeTree;
import lphy.base.evolution.tree.TimeTreeNode;
import lphy.base.math.RandomUtils;
import lphy.core.model.*;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.parser.argument.ParameterInfo;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import static lphy.base.evolution.EvolutionConstants.treeParamName;
import static lphy.base.evolution.birthdeath.BirthDeathConstants.psiParamName;

/**
 * A java implementation of sim.fossils.poisson in https://github.com/fossilsim/fossilsim/blob/master/R/sim.fossils.R
 */
public class SimFossilsPoisson implements GenerativeDistribution<TimeTree> {

    private Value<TimeTree> tree;
    private Value<Number> psi;

    RandomGenerator random;

    static final boolean generateSampledAncestorsAsLeafNodes = true;

    public SimFossilsPoisson(@ParameterInfo(name = treeParamName, description = "Tree to add simulated fossils to.") Value<TimeTree> tree,
                             @ParameterInfo(name = psiParamName, description = "The fossilization rate per unit time per lineage.") Value<Number> psi) {

        this.tree = tree;
        this.psi = psi;
        this.random = RandomUtils.getRandom();
    }


    @GeneratorInfo(name = "SimFossilsPoisson",
            category = GeneratorCategory.BD_TREE, examples = {"simFossils.lphy"},
            description = "A tree with fossils added to the given tree at rate psi.")
    public RandomVariable<TimeTree> sample() {

        double samplingRate = ValueUtils.doubleValue(psi);

        TimeTree treeCopy = new TimeTree(tree.value());

        simulateFossils(treeCopy, samplingRate);

        return new RandomVariable<>(null, treeCopy, this);
    }

    private void simulateFossils(TimeTree tree, double psi) {

        int nextFossilNumber = 0;


        for (TimeTreeNode node : tree.getNodes()) {

            if (!node.isRoot()) {
                double min = node.getAge();
                double max = node.getParent().getAge();
                double expectedFossils = (max - min) * psi;

                PoissonDistribution poissonDistribution = new PoissonDistribution(random,expectedFossils,1e-8, 100);
                int fossils = poissonDistribution.sample();
                if (fossils > 0) {
                    double[] fossilTimes = new double[fossils];
                    for (int i = 0; i < fossils; i++) {
                        fossilTimes[i] = random.nextDouble() * (max - min) + min;
                    }
                    addFossils(fossilTimes, node.getParent(), node, nextFossilNumber, tree);
                }
                nextFossilNumber += fossils;
            }
        }

        tree.setRoot(tree.getRoot(), true);
    }

    private void addFossils(double[] times, TimeTreeNode parent, TimeTreeNode child, int nextFossilNumber, TimeTree tree) {
        Arrays.sort(times);

        for (int i = times.length - 1; i >= 0; i--) {
            Taxon fossilTaxon = new Taxon("f_"+nextFossilNumber+"", times[i]);

            parent.removeChild(child);

            TimeTreeNode fossilNode;
            if (generateSampledAncestorsAsLeafNodes) {
                TimeTreeNode fossilLeafNode = new TimeTreeNode(fossilTaxon,tree);
                fossilNode = new TimeTreeNode(fossilTaxon.getAge());
                fossilNode.addChild(fossilLeafNode);
            } else {
                fossilNode = new TimeTreeNode(fossilTaxon,tree);
            }

            parent.addChild(fossilNode);
            fossilNode.addChild(child);
            parent = fossilNode;
            nextFossilNumber += 1;
        }
    }
    
    @Override
    public double logDensity(TimeTree timeTree) {
        throw new UnsupportedOperationException("Not implemented!");
    }

    @Override
    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(treeParamName, tree);
            put(psiParamName, psi);
        }};
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(treeParamName)) tree = value;
        else if (paramName.equals(psiParamName)) psi = value;
        else throw new IllegalArgumentException("Expected either " + treeParamName + " or " + psiParamName + " as parameter name but got " + paramName );
    }

    public String toString() {
        return getName();
    }
}
