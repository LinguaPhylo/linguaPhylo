package lphy.base.evolution.likelihood;

import jebl.evolution.sequences.SequenceType;
import lphy.base.evolution.alignment.Alignment;
import lphy.base.evolution.alignment.SimpleAlignment;
import lphy.base.evolution.tree.TimeTree;
import lphy.base.evolution.tree.TimeTreeNode;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * An event-based (sparse) CTMC simulation extending PhyloCTMC.
 * This allows us to use any Q matrix passed to the constructor.
 */
public class SparsePhyloCTMC extends PhyloCTMC {

    // We store the differences in a Map: node -> (siteIndex -> mutatedState).
    // Each node's difference map tells which sites changed from its parent.
    private Map<TimeTreeNode, Map<Integer,Integer>> nodeDifferences;

    public SparsePhyloCTMC(
            Value<TimeTree> tree,
            Value<Number> clockRate,
            Value<Double[]> freq,
            Value<Double[][]> Q,
            Value<Double[]> siteRates,
            Value<Double[]> branchRates,
            Value<Integer> L,
            Value<SequenceType> dataType,
            Value<SimpleAlignment> rootSeq)  {
        super(tree, clockRate, freq, Q, siteRates, branchRates, L, dataType, rootSeq);
        // TODO
        if (rootSeq != null) {
            throw new UnsupportedOperationException("rootSeq is not supported !");
        }
    }

    // Simplified constructor without optional parameters
    public SparsePhyloCTMC(
            Value<TimeTree> tree,
            Value<Number> clockRate,
            Value<Double[]> freq,
            Value<Double[][]> Q,
            Value<Integer> L,
            Value<SequenceType> dataType) {
        super(tree, clockRate, freq, Q, null, null, L, dataType, null);
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
        double branchLength = clockRate.value().doubleValue() * (parent.getAge() - child.getAge());
        if (branchRates != null) {
            branchLength *= branchRates.value()[child.getIndex()];
        }

        // 2) The total expected # of events = branchLength * N * mu
        //    We'll compute mu from the Q matrix.
        Double[][] Qm = getQ();
        double mu = computeMeanOffDiagonalRate(Qm);
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
        if (node == null) {
            // fallback: sample from root freq
            return sampleFromRootFreq();
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
            description = "")
    @Override
    public RandomVariable<Alignment> sample() {
        // 1) run the sparse simulation
        simulateSparse();

        // 2) create an alignment using the tree and sequence type
        int length = getSiteCount();
        SequenceType dt = getDataType();
        // TODO
        Alignment alignment = new SimpleAlignment(idMap, length, dt);

        // 3) fill in the alignment using the simulated sparse differences
        TimeTreeNode root = tree.value().getRoot();
        for (String taxonName : idMap.keySet()) {
            TimeTreeNode node = findNodeById(root, taxonName);
            if (node != null) {
                int nodeIndex = node.getLeafIndex();
                for (int site = 0; site < length; site++) {
                    int state = getEffectiveState(node, site);
                    alignment.setState(nodeIndex, site, state);
                }
            }
        }

        return new RandomVariable<>("D", alignment, this);
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
}