package lphy.evolution.coalescent;

import lphy.evolution.Taxa;
import lphy.evolution.Taxon;
import lphy.evolution.tree.TimeTree;
import lphy.evolution.tree.TimeTreeNode;
import lphy.core.distributions.Utils;
import lphy.graphicalModel.*;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.*;

/**
 * A Kingman coalescent tree generative distribution conditional on a species tree with a specified population size on each species branch.
 */
public class MultispeciesCoalescent implements GenerativeDistribution<TimeTree> {

    private final String thetaParamName;
    private final String nParamName;
    private final String taxaParamName;
    private final String SParamName;
    private Value<Double[]> theta;
    private Value<Integer[]> n;
    private Value<Taxa> taxa;
    private Value<TimeTree> S;

    RandomGenerator random;

    public final static String separator = "_";

    public MultispeciesCoalescent(@ParameterInfo(name = "theta", description = "effective population sizes, one for each species (both extant and ancestral).") Value<Double[]> theta,
                                  @ParameterInfo(name = "n", description = "the number of sampled taxa in the gene tree for each extant species.", optional=true) Value<Integer[]> n,
                                  @ParameterInfo(name = "taxa", description = "the taxa for the gene tree, with species to define the mapping.", optional=true) Value<Taxa> taxa,
                                  @ParameterInfo(name = "S", description = "the species tree. ") Value<TimeTree> S) {
        this.theta = theta;
        this.n = n;
        this.taxa = taxa;
        this.S = S;
        this.random = Utils.getRandom();

        thetaParamName = getParamName(0);
        nParamName = getParamName(1);
        taxaParamName = getParamName(2);
        SParamName = getParamName(3);
    }

    @GeneratorInfo(name = "MultispeciesCoalescent", description = "The Kingman coalescent distribution within each branch of species tree gives rise to a distribution over gene trees conditional on the species tree.")
    public RandomVariable<TimeTree> sample() {

        TimeTree geneTree = new TimeTree();

        Map<String,List<TimeTreeNode>> activeNodes = new TreeMap<>();

        createActiveNodes(activeNodes, geneTree);



        List<TimeTreeNode> root = doSpeciesTreeBranch(S.value().getRoot(), activeNodes, theta.value());

        if (root.size() != 1) throw new RuntimeException();

        geneTree.setRoot(root.get(0));

        return new RandomVariable<>("geneTree", geneTree, this);
    }

    private void createActiveNodes(Map<String,List<TimeTreeNode>> activeNodes, TimeTree geneTree) {

        if (n != null) {
            int i = 0;
            for (int sp = 0; sp < n.value().length; sp++) {
                List<TimeTreeNode> taxaInSp = new ArrayList<>();
                activeNodes.put(sp+"", taxaInSp);
                for (int k = 0; k < n.value()[sp]; k++) {
                    TimeTreeNode node = new TimeTreeNode(sp + separator + k, geneTree);

                    // set age to the age of the species.
                    node.setAge(S.value().getNodeByIndex(sp).getAge());

                    // set node index to i
                    node.setLeafIndex(i);

                    taxaInSp.add(node);
                    i += 1;
                }
            }
            System.out.println("gene tree has " + i + " nodes");
        } else if (taxa != null) {
            Taxon[] taxonArray = taxa.value().getTaxa();

            for (Taxon taxon : taxonArray) {
                List<TimeTreeNode> taxaInSp = activeNodes.get(taxon.getSpecies());
                if (taxaInSp == null) {
                    taxaInSp = new ArrayList<>();
                    activeNodes.put(taxon.getSpecies(), taxaInSp);
                }
                taxaInSp.add(new TimeTreeNode(taxon, geneTree));
            }
        }
    }

    private List<TimeTreeNode> doSpeciesTreeBranch(TimeTreeNode spNode, Map<String, List<TimeTreeNode>> allLeafActiveNodes, Double[] allThetas) {

        List<TimeTreeNode> activeNodes;
        if (!spNode.isLeaf()) {
            activeNodes = new ArrayList<>();
            for (TimeTreeNode child : spNode.getChildren()) {
                activeNodes.addAll(doSpeciesTreeBranch(child, allLeafActiveNodes, allThetas));
            }
        } else {
            activeNodes = allLeafActiveNodes.get(spNode.getId());
        }

        double time = spNode.getAge();
        double theta = allThetas[spNode.getIndex()];

        while (activeNodes.size() > 1 && (spNode.getParent() == null || time < spNode.getParent().getAge())) {

            int k = activeNodes.size();
            double rate = (k * (k - 1.0)) / (theta * 2.0);

            // random exponential variate
            double x = -Math.log(random.nextDouble()) / rate;
            time += x;

            if (spNode.getParent() == null || time < spNode.getParent().getAge()) {
                TimeTreeNode a = activeNodes.remove(random.nextInt(activeNodes.size()));
                TimeTreeNode b = activeNodes.remove(random.nextInt(activeNodes.size()));
                TimeTreeNode parent = new TimeTreeNode(time, new TimeTreeNode[]{a, b});
                activeNodes.add(parent);
            }
        }

        return activeNodes;
    }

    @Override
    public double logDensity(TimeTree timeTree) {

        // TODO
        return 0.0;
    }

    @Override
    public SortedMap<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        map.put(thetaParamName, theta);
        if (n != null) map.put(nParamName, n);
        if (taxa != null)  map.put(taxaParamName, taxa);
        map.put(SParamName, S);
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(thetaParamName)) theta = value;
        else if (paramName.equals(nParamName)) n = value;
        else if (paramName.equals(taxaParamName)) taxa = value;
        else if (paramName.equals(SParamName)) S = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);
    }

    public String toString() {
        return getName();
    }

    public Value<TimeTree> getSpeciesTree() {
        return S;
    }

    public Value<Double[]> getPopulationSizes() {
        return theta;
    }

    public Value<Integer[]> getN() {
        return n;
    }

}
