package lphy.evolution.coalescent;

import lphy.core.functions.ARange;
import lphy.evolution.Taxa;
import lphy.evolution.Taxon;
import lphy.evolution.tree.TimeTree;
import lphy.evolution.tree.TimeTreeNode;
import lphy.core.distributions.Utils;
import lphy.graphicalModel.*;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.*;

import static lphy.core.distributions.DistributionConstants.nParamName;
import static lphy.evolution.coalescent.CoalescentConstants.thetaParamName;
import static lphy.evolution.tree.TaxaConditionedTreeGenerator.taxaParamName;

/**
 * A Kingman coalescent tree generative distribution conditional on a species tree with a specified population size on each species branch.
 */
public class MultispeciesCoalescent implements GenerativeDistribution {

    public static final String SParamName = "S";
    public static final String numLociParamName = "numLoci";
    private Value<Double[]> theta;
    private Value<Integer[]> n;
    private Value<Integer> numLoci;
    private Value<Taxa> taxa;
    private Value<TimeTree> S;

    RandomGenerator random;

    public final static String separator = "_";

    Taxa geneTreeTaxa = null;

    public MultispeciesCoalescent(@ParameterInfo(name = thetaParamName, description = "effective population sizes, one for each species (both extant and ancestral).") Value<Double[]> theta,
                                  @ParameterInfo(name = nParamName, description = "the number of sampled taxa in the gene tree for each extant species.", optional = true) Value<Integer[]> n,
                                  @ParameterInfo(name = taxaParamName, description = "the taxa for the gene tree, with species to define the mapping.", optional = true) Value<Taxa> taxa,
                                  @ParameterInfo(name = numLociParamName, description = "the number of loci the simulate. Default is 1", optional = true) Value<Integer> numLoci,
                                  @ParameterInfo(name = SParamName, description = "the species tree. ") Value<TimeTree> S) {
        this.theta = theta;
        this.n = n;
        this.numLoci = numLoci;
        this.taxa = taxa;
        this.S = S;
        this.random = Utils.getRandom();

        if (n != null) {
            List<TimeTreeNode> extant = S.value().getExtantNodes();
            if (extant.size() != n.value().length)
                throw new IllegalArgumentException("Length of n must be equal to the number of extant taxa in species tree provided");
        }
    }

    @GeneratorInfo(name = "MultispeciesCoalescent",
            description = "The Kingman coalescent distribution within each branch of species tree gives rise to a distribution over gene trees conditional on the species tree. " +
                    "The (optional) taxa object provides for non-trivial mappings from individuals to species, and not all species have to have representatives. " +
                    "The (optional) numLoci parameter can be used to produce more than one gene tree from this distribution.")
    public RandomVariable sample() {

        int locusCount = getLocusCount();

        geneTreeTaxa = createGeneTreeTaxa();

        if (locusCount == 1) return new RandomVariable<>(null, simulateGeneTree(), this);

        List<RandomVariable> randomVariables = new ArrayList<>();
        for (int i = 0; i < locusCount; i++) {
            randomVariables.add(new RandomVariable(null, simulateGeneTree(), this));
        }
        return new VectorizedRandomVariable(null, randomVariables, this);
    }

    private int getLocusCount() {
        if (numLoci == null) return 1;
        return numLoci.value();
    }

    private TimeTree simulateGeneTree() {
        TimeTree geneTree = new TimeTree(geneTreeTaxa);

        Map<String, List<TimeTreeNode>> activeNodes = new TreeMap<>();

        createActiveNodes(activeNodes, geneTree);

        List<TimeTreeNode> root = doSpeciesTreeBranch(S.value().getRoot(), activeNodes, theta.value());

        if (root.size() != 1) {
            throw new RuntimeException("Returned multiple gene roots from " + S.value().getRoot());
        }

        geneTree.setRoot(root.get(0));

        return geneTree;
    }

    public Taxa getGeneTreeTaxa() {
        return geneTreeTaxa;
    }

    public Taxa createGeneTreeTaxa() {

        List<Taxon> taxonList = new ArrayList<>();
        Taxon[] taxonArray = new Taxon[0];

        if (n != null) {

            List<TimeTreeNode> extant = S.value().getExtantNodes();
            if (numericIds(extant)) {
                extant.sort(Comparator.comparingInt(o -> Integer.parseInt(o.getId())));
            } else {
                extant.sort(Comparator.comparing(TimeTreeNode::getId));
            }

            int i = 0;
            for (TimeTreeNode node : extant) {
                for (int k = 0; k < n.value()[i]; k++) {
                    taxonList.add(new Taxon(node.getId() + separator + k, node.getId()+"", node.getAge()));
                }
            }
            taxonArray = taxonList.toArray(taxonArray);
        } else if (taxa != null) {
            taxonArray = taxa.value().getTaxonArray();
        } else {
            Taxa speciesTreeTaxa = S.value().getTaxa();
            for (Taxon speciesTaxon : speciesTreeTaxa.getTaxonArray()) {
                if (speciesTaxon.isExtant()) {
                    taxonList.add(new Taxon(speciesTaxon.getName() + "_0", speciesTaxon.getName(), speciesTaxon.getAge()));
                }
            }
            taxonArray = taxonList.toArray(taxonArray);
        }

        return new Taxa.Simple(taxonArray);
    }

    private boolean numericIds(List<TimeTreeNode> nodes) {
        for (TimeTreeNode node : nodes) {
            try {
                int id = Integer.parseInt(node.getId());
            } catch (NumberFormatException nfe) {
                return false;
            }
        }
        return true;
    }

    private void createActiveNodes(Map<String, List<TimeTreeNode>> activeNodes, TimeTree geneTree) {
        for (Taxon taxon : geneTreeTaxa.getTaxonArray()) {
            List<TimeTreeNode> taxaInSp = activeNodes.computeIfAbsent(taxon.getSpecies(), k -> new ArrayList<>());
            taxaInSp.add(new TimeTreeNode(taxon, geneTree));
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
            if (activeNodes == null) activeNodes = new ArrayList<>();
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

    public double logDensity(Object timeTreeObject) {

        // TODO
        return 0.0;
    }

    @Override
    public SortedMap<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        map.put(thetaParamName, theta);
        if (n != null) map.put(nParamName, n);
        if (taxa != null) map.put(taxaParamName, taxa);
        if (numLoci != null) map.put(numLociParamName, numLoci);
        map.put(SParamName, S);
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        switch (paramName) {
            case thetaParamName:
                theta = value;
                break;
            case nParamName:
                n = value;
                break;
            case taxaParamName:
                taxa = value;
                break;
            case SParamName:
                S = value;
                break;
            case numLociParamName:
                numLoci = value;
                break;
            default:
                throw new RuntimeException("Unrecognised parameter name: " + paramName);
        }
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
