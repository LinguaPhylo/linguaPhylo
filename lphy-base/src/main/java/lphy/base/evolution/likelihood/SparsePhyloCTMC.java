package lphy.base.evolution.likelihood;

import jebl.evolution.sequences.SequenceType;
import lphy.base.distribution.Categorical;
import lphy.base.evolution.CellPosition;
import lphy.base.evolution.Taxa;
import lphy.base.evolution.alignment.Alignment;
import lphy.base.evolution.alignment.SimpleAlignment;
import lphy.base.evolution.alignment.VariantStyleAlignment;
import lphy.base.evolution.tree.TimeTree;
import lphy.base.evolution.tree.TimeTreeNode;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.ValueUtils;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.*;

/**
 * An event-based (sparse) CTMC simulation extending PhyloCTMC.
 * This allows us to use any Q matrix passed to the constructor.
 */
public class SparsePhyloCTMC extends PhyloCTMC {

    // We store the differences in a Map: node -> (siteIndex -> mutatedState).
    // Each node's difference map tells which sites changed from its parent.
    private Map<TimeTreeNode, Map<Integer,Integer>> nodeDifferences;
    private Set<Integer> changedSites = new HashSet<>();
    VariantStyleAlignment alignment;

    public SparsePhyloCTMC(
            @ParameterInfo(name = AbstractPhyloCTMC.treeParamName, verb = "on", narrativeName = "phylogenetic time tree", description = "the time tree.") Value<TimeTree> tree,
            @ParameterInfo(name = AbstractPhyloCTMC.muParamName, narrativeName = "molecular clock rate", description = "the clock rate. Default value is 1.0.", optional = true) Value<Number> mu,
            @ParameterInfo(name = AbstractPhyloCTMC.rootFreqParamName, verb = "are", narrativeName = "root frequencies", description = "the root probabilities. Optional parameter. If not specified then first row of e^{100*Q) is used.", optional = true) Value<Double[]> rootFreq,
            @ParameterInfo(name = QParamName, narrativeName= "instantaneous rate matrix", description = "the instantaneous rate matrix.") Value<Double[][]> Q,
            @ParameterInfo(name = siteRatesParamName, description = "a rate for each site in the alignment. Site rates are assumed to be 1.0 otherwise.",  optional = true) Value<Double[]> siteRates,
            @ParameterInfo(name = AbstractPhyloCTMC.branchRatesParamName, description = "a rate for each branch in the tree. Original branch rates are used if rates not given. Branch rates are assumed to be 1.0 otherwise.", optional = true) Value<Double[]> branchRates,
            @ParameterInfo(name = AbstractPhyloCTMC.LParamName, narrativeName= "alignment length",
                    description = "length of the alignment", optional = true) Value<Integer> L,
            @ParameterInfo(name = AbstractPhyloCTMC.dataTypeParamName, description = "the data type used for simulations, default to nucleotide",
                    narrativeName = "data type used for simulations", optional = true) Value<SequenceType> dataType,
            @ParameterInfo(name = AbstractPhyloCTMC.rootSeqParamName, narrativeName="root sequence", description = "root sequence, defaults to root sequence generated from equilibrium frequencies.", optional = true) Value<Alignment> rootSeq) {
        super(tree, mu, rootFreq, Q, siteRates, branchRates, L, dataType, rootSeq);

        if (rootSeq != null) {
            this.rootSeq = rootSeq;
        }
    }

    // ======================================================================
    // ============== Sparse (event-based) simulation code ===================
    // ======================================================================

    /**
     * This is the main entry point for the user to do a sparse simulation.
     * We store the difference maps in nodeDifferences so that we can later 
     * reconstruct the entire sequence for leaves if needed.
     */
    public void simulateSparse() {
        // 1) Make sure everything is consistent and Q is built
        setup();

        // 2) Clear our difference map structure
        nodeDifferences = new HashMap<>();

        // 3) Mark root differences as empty: by default the root has no changes from itself
        TimeTreeNode root = tree.value().getRoot();
        nodeDifferences.put(root, Collections.emptyMap());
        // 4) Recursively traverse children
        for (TimeTreeNode child : root.getChildren()) {
            simulateBranchSparse(root, child);
        }
    }

    /**
     * Recursively simulate from parent to child:
     *   - draw Poisson(# of mutation events)
     *   - for each event, pick site, sample new state, record difference
     *   - properly handle cases where mutations might revert to original states
     */
    private void simulateBranchSparse(TimeTreeNode parent, TimeTreeNode child) {
        // 1) Branch length scaled by clockRate & branchRates
        double branchLength;

        if (clockRate != null){
            branchLength = clockRate.value().doubleValue() * (parent.getAge() - child.getAge());
        } else {
            branchLength = parent.getAge() - child.getAge();
        }

        if (branchRates != null) {
            branchLength *= branchRates.value()[child.getIndex()];
        }

        // 2) The total expected # of events = branchLength * N * mu
        //    We'll compute mu from the Q matrix.
        Double[][] Qm = getQ();
        double mu = (this.clockRate == null) ? computeMeanOffDiagonalRate(Qm) : ValueUtils.doubleValue(clockRate);

        int N = getSiteCount();
        double lambda = branchLength * mu * N;

        // 3) Draw Poisson(lambda)
        RandomGenerator rng = random;
        PoissonDistribution pois = new PoissonDistribution(rng, lambda,
                PoissonDistribution.DEFAULT_EPSILON,
                PoissonDistribution.DEFAULT_MAX_ITERATIONS);
        int K = pois.sample();

        // 4) Build child's difference map
        Map<Integer,Integer> childDiffs = new HashMap<>();

        // 5) For each mutation event
        for (int e = 0; e < K; e++) {
            // pick a site at random (uniform)
            int siteIndex = rng.nextInt(N);

            // Find the parent's effective state for this site
            int parentState = getEffectiveState(parent, siteIndex);
            
            // Find the current state for this site in this branch (if previously mutated)
            int currentState;

            if (childDiffs.containsKey(siteIndex)) {
                // Use the latest state from previous mutations in this branch
                currentState = childDiffs.get(siteIndex);
            } else {
                // If this is the first mutation on this site in this branch,
                // use the parent's state
                currentState = parentState;
            }

            // Sample new state from Q using the current state
            int newState = sampleNewState(currentState, Qm);
            
            // Only record the difference if:
            // 1. This is a new mutation in this branch, and the state is different from parent's state
            // 2. OR, this is a follow-up mutation in this branch that changed the state
            if (newState != currentState) {
                // If the new state happens to match the parent's original state and there were 
                // previous mutations in this branch, we need to decide if we should remove this entry
                if (newState == parentState && childDiffs.containsKey(siteIndex)) {
                    // Remove the entry since effectively there's no change from parent
                    childDiffs.remove(siteIndex);
                } else {
                    // Otherwise record the new state
                    childDiffs.put(siteIndex, newState);
                }
            }
            // If newState == currentState, we don't record anything as there's no actual change
        }

        nodeDifferences.put(child, childDiffs);
        changedSites.addAll(childDiffs.values());

        // 6) Recurse
        for (TimeTreeNode grandChild : child.getChildren()) {
            simulateBranchSparse(child, grandChild);
        }
    }

    /**
     * Climb up the ancestor chain until we find a difference entry for 'siteIndex',
     * or we reach the root. If nobody had a difference, sample from rootFreq (once).
     */
    private int getEffectiveState(TimeTreeNode node, int siteIndex) {
        // if node is root
        if (rootSeq == null && node == null) {
            // fallback: sample from root freq
            int rootState = sampleFromRootFreq();
            // set root sequence
            rootSeq.value().setState(rootSeq.value().length()-1, siteIndex, rootState);

            return rootState;
        }

        if (rootSeq != null && node == tree.value().getRoot()) {
            return rootSeq.value().getState(rootSeq.value().length()-1, siteIndex);
        }

        Map<Integer,Integer> diffs = nodeDifferences.get(node);
        if (diffs == null) {
            // no map means we haven't visited this node yet or something is off
            // but let's keep going up
            return getEffectiveState(node.getParent(), siteIndex);
        }
        Integer st = diffs.get(siteIndex);
        if (st != null) {
            return st;
        } else {
            // check parent's differences
            return getEffectiveState(node.getParent(), siteIndex);
        }
    }

    /**
     * Sample a new state from the parentState's off-diagonal rates in the Q matrix.
     */
    private int sampleNewState(int parentState, Double[][] Qm) {
        int numStates = Qm.length;
        double sumOffDiag = 0.0;
        for (int s = 0; s < numStates; s++) {
            if (s != parentState) {
                sumOffDiag += Qm[parentState][s];
            }
        }
        double r = random.nextDouble() * sumOffDiag;
        double cumulative = 0.0;
        for (int s = 0; s < numStates; s++) {
            if (s == parentState) continue;
            cumulative += Qm[parentState][s];
            if (r <= cumulative) return s;
        }
        // fallback if rounding errors occur
        return numStates - 1;
    }

    /**
     * If we get all the way up the ancestor chain to the root and never see a difference,
     * that means no mutation from the root's "unknown" state. 
     * We sample from rootFreq once.
     */
    private int sampleFromRootFreq() {
        Double[] rf = rootFreqs.value();
        double u = random.nextDouble();
        double c = 0;
        for (int i = 0; i < rf.length; i++) {
            c += rf[i];
            if (u < c) {
                return i;
            }
        }
        return rf.length - 1;
    }

    /**
     * Compute the average off-diagonal rate from the Q matrix (row sums of off-diagonal entries).
     * This is used as "mu" in Poisson(lambda) = lambda = branchLength * mu * N.
     */
    private double computeMeanOffDiagonalRate(Double[][] Qm) {
        int numStates = Qm.length;
        double sum = 0.0;
        int count = 0;
        for (int i = 0; i < numStates; i++) {
            for (int j = 0; j < numStates; j++) {
                if (i != j) {
                    sum += Qm[i][j];
                    count++;
                }
            }
        }
        return (count > 0 ? sum / count : 0.0);
    }

    // ======================================================================
    // =========== Example: Reconstruct leaf sequences if desired ============
    // ======================================================================

    /**
     * If you want the final leaf sequence, you can do:
     */
    public int[] reconstructLeafSequence(TimeTreeNode leaf) {
        int nSites = getSiteCount();
        int[] leafSeq = new int[nSites];
        for (int site = 0; site < nSites; site++) {
            leafSeq[site] = getEffectiveState(leaf, site);
        }
        return leafSeq;
    }

    // ======================================================================
    // =========== Overriding sample() to produce an Alignment ==============
    // ======================================================================

    /**
     * Override the sample method from PhyloCTMC to use our sparse simulation
     */
    @GeneratorInfo(name = "SparsePhyloCTMC", verbClause = "is assumed to have evolved under",
            narrativeName = "phylogenetic continuous time Markov process",
            category = GeneratorCategory.PHYLO_LIKELIHOOD,
            description = "Simulate random number of mutations on branches along the tree. Variable sites and root sequence are stored to return the alignment.  ")
    @Override
    public RandomVariable<Alignment> sample() {
        // 1) run the sparse simulation
        simulateSparse();

        // 2) create a variant storing map with root sequence and variants
        // initialise the root sequence if it's not given
        int length = getSiteCount();

        // default to nucleotide
        SequenceType dt = SequenceType.NUCLEOTIDE;
        if (dataType != null) dt = dataType.value();

        if (rootSeq == null) {
            rootSeq = new Value<>("rootSeq", new SimpleAlignment(Taxa.createTaxa(idMap.get(tree.value().getRoot())), length, dt));
        }

        // initialise the variantStore map
        Map<CellPosition, Integer> variantStore = new HashMap<>();
        alignment = new VariantStyleAlignment(idMap, rootSeq.value(), variantStore);

        // 3) fill in the alignment using the simulated sparse differences
        TimeTreeNode root = tree.value().getRoot();
        int taxonIndex = 0;

        for (String taxonName : idMap.keySet()) {
            TimeTreeNode node = findNodeById(root, taxonName);
            if (node != null) {
                // fill by each site
                for (int site = 0; site < length; site++) {
                    // add parent site mutations first
                    TimeTreeNode tempNode = node.getParent();

                    // find the youngest parent that has a mutation on this site
                    while (tempNode != null && tempNode != root && // while node exist and is not root and node has differences
                            (!nodeDifferences.containsKey(tempNode) || nodeDifferences.get(tempNode).get(site) == null)) {
                        tempNode = tempNode.getParent();
                    }

                    // if a parent mutation exists, assign the effective state
                    if (tempNode != null && tempNode != root) {
                        int state = nodeDifferences.get(tempNode).get(site);
                        alignment.setState(taxonIndex, site, state);
                    } // else, skip

                    // map the mutations on the tip
                    if (nodeDifferences.containsKey(node) && nodeDifferences.get(node).containsKey(site)) {
                        int state = nodeDifferences.get(node).get(site);
                        alignment.setState(taxonIndex, site, state);
                    }
                }
            }
            taxonIndex++;
        }

        Alignment a = mapAlignment(alignment);

        return new RandomVariable<>("D", a, this);
    }

    public static Alignment mapAlignment(VariantStyleAlignment alignment) {
        Alignment a = new SimpleAlignment(alignment.getTaxa(), alignment.nchar(), alignment.getSequenceType());
        for (int i = 0; i < alignment.length(); i++) {
            for (int j = 0; j < alignment.nchar(); j++) {
                a.setState(i,j, alignment.getState(i,j));
            }
        }
        return a;
    }

    /**
     * Helper method to find a node by ID in the tree
     */
    private TimeTreeNode findNodeById(TimeTreeNode node, String id) {
        if (id.equals(node.getId())) {
            return node;
        }
        for (TimeTreeNode child : node.getChildren()) {
            TimeTreeNode found = findNodeById(child, id);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    // for unit test
    public Map<TimeTreeNode, Map<Integer,Integer>> getNodeDifferences(){
        return nodeDifferences;
    }

    public VariantStyleAlignment getAlignment() {
        return alignment;
    }

}