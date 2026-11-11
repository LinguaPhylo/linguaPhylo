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

import java.util.*;

/**
 * An event-based (sparse) CTMC simulation extending PhyloCTMC.
 * Simulates the same process as {@link PhyloCTMC} (same distribution of the alignment),
 * but only stores the sites that change on each branch.
 * <p>
 * Uses uniformization: with Λ = max_i(-Q_ii), events on a branch occur as a Poisson process
 * with rate Λ * clockRate * branchRate * siteRate per site, and each event moves a site from
 * state i to j with probability (I + Q/Λ)_ij (which may be a "virtual" self-jump).
 * This is exact for any Q, including non-reversible Q and Q with unequal exit rates.
 * <p>
 * The random number stream differs from PhyloCTMC, so the same seed gives a different
 * (but identically distributed) alignment.
 */
public class SparsePhyloCTMC extends PhyloCTMC {

    // node -> (siteIndex -> state): sites whose state on this node differs from its parent.
    private Map<TimeTreeNode, Map<Integer, Integer>> nodeDifferences;
    private VariantStyleAlignment alignment;
    // the root sequence used in the last simulation (given, or sampled from rootFreqs)
    private Alignment simRoot;

    // uniformized jump chain: cumulative jump probabilities per row of (I + Q/Λ)
    private double uniformRate;
    private double[][] jumpCDF;
    // cumulative site rates for choosing the site of an event, null if all site rates are 1
    private double[] siteRateCDF;

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
            @ParameterInfo(name = AbstractPhyloCTMC.rootSeqParamName, narrativeName="root sequence", description = "root sequence, defaults to root sequence generated from the root frequencies.", optional = true) Value<Alignment> rootSeq) {
        super(tree, mu, rootFreq, Q, siteRates, branchRates, L, dataType, rootSeq);
    }

    // ======================================================================
    // ============== Sparse (event-based) simulation code ===================
    // ======================================================================

    /**
     * Run the sparse simulation. Fills nodeDifferences (per-branch changes) and simRoot.
     */
    public void simulateSparse() {
        setup(); // idMap, rootFreqs

        final int nSites = getSiteCount();
        simRoot = getOrSampleRoot(nSites);

        Double[][] Qm = getQ();
        buildJumpChain(Qm);
        buildSiteRateCDF(nSites);

        nodeDifferences = new HashMap<>();
        TimeTreeNode root = tree.value().getRoot();
        nodeDifferences.put(root, Collections.emptyMap());

        // states differing from the root sequence along the current path, root -> node
        Map<Integer, Integer> pathFromRoot = new HashMap<>();
        for (TimeTreeNode child : root.getChildren()) {
            simulateBranchSparse(root, child, pathFromRoot);
        }
    }

    private Alignment getOrSampleRoot(int nSites) {
        if (rootSeq != null) {
            Alignment r = rootSeq.value();
            if (r.ntaxa() != 1)
                throw new IllegalArgumentException("The root sequence alignment must contain exactly one sequence, but has " + r.ntaxa());
            return r;
        }
        SequenceType dt = (dataType != null) ? dataType.value() : SequenceType.NUCLEOTIDE;
        Alignment r = new SimpleAlignment(Taxa.createTaxa(1), nSites, dt);
        Double[] freqs = rootFreqs.value();
        for (int i = 0; i < nSites; i++)
            r.setState(0, i, Categorical.sample(freqs, random));
        return r;
    }

    private void buildJumpChain(Double[][] Qm) {
        final int n = Qm.length;
        uniformRate = 0.0;
        for (int i = 0; i < n; i++)
            uniformRate = Math.max(uniformRate, -Qm[i][i]);

        jumpCDF = new double[n][n];
        for (int i = 0; i < n; i++) {
            double c = 0.0;
            for (int j = 0; j < n; j++) {
                double p = (uniformRate > 0) ? Qm[i][j] / uniformRate : 0.0;
                if (i == j) p += 1.0;
                c += p;
                jumpCDF[i][j] = c;
            }
        }
    }

    private void buildSiteRateCDF(int nSites) {
        if (siteRates == null) {
            siteRateCDF = null;
            return;
        }
        Double[] r = siteRates.value();
        siteRateCDF = new double[nSites];
        double c = 0.0;
        for (int i = 0; i < nSites; i++) {
            c += r[i];
            siteRateCDF[i] = c;
        }
    }

    private double totalSiteRate(int nSites) {
        return (siteRateCDF == null) ? nSites : siteRateCDF[nSites - 1];
    }

    private int pickSite(int nSites) {
        if (siteRateCDF == null) return random.nextInt(nSites);
        double u = random.nextDouble() * siteRateCDF[nSites - 1];
        int idx = Arrays.binarySearch(siteRateCDF, u);
        if (idx < 0) idx = -idx - 1;
        // skip sites with zero rate (equal cumulative values)
        while (idx < nSites - 1 && siteRateCDF[idx] <= u) idx++;
        return Math.min(idx, nSites - 1);
    }

    private int jump(int state) {
        double[] cdf = jumpCDF[state];
        double u = random.nextDouble() * cdf[cdf.length - 1];
        for (int j = 0; j < cdf.length; j++) {
            if (u < cdf[j]) return j;
        }
        return state;
    }

    /**
     * Simulate the branch parent -> child, then recurse.
     * @param pathFromRoot states that differ from the root sequence at the parent;
     *                     restored to its original content before returning.
     */
    private void simulateBranchSparse(TimeTreeNode parent, TimeTreeNode child, Map<Integer, Integer> pathFromRoot) {
        final int nSites = getSiteCount();

        // branch length in expected substitutions per unit site rate, as in AbstractPhyloCTMC.traverseTree
        double mu = (clockRate == null) ? 1.0 : ValueUtils.doubleValue(clockRate);
        double branchLength = mu * (parent.getAge() - child.getAge());
        if (branchRates != null)
            branchLength *= branchRates.value()[child.getIndex()];

        double lambda = uniformRate * branchLength * totalSiteRate(nSites);
        int K = (lambda > 0) ? new PoissonDistribution(random, lambda,
                PoissonDistribution.DEFAULT_EPSILON, PoissonDistribution.DEFAULT_MAX_ITERATIONS).sample() : 0;

        // child's states that differ from parent's
        Map<Integer, Integer> childDiffs = new HashMap<>();
        // parent's state of each site touched on this branch, to undo pathFromRoot later
        Map<Integer, Integer> parentStates = new HashMap<>();

        for (int e = 0; e < K; e++) {
            int site = pickSite(nSites);
            int parentState = parentStates.computeIfAbsent(site, s -> stateAt(s, pathFromRoot));
            int current = childDiffs.getOrDefault(site, parentState);
            int newState = jump(current);
            if (newState == current) continue; // virtual jump
            if (newState == parentState) childDiffs.remove(site);
            else childDiffs.put(site, newState);
        }
        nodeDifferences.put(child, childDiffs);

        // update the path to the child
        Map<Integer, Integer> undo = new HashMap<>();
        for (Map.Entry<Integer, Integer> d : childDiffs.entrySet()) {
            undo.put(d.getKey(), pathFromRoot.get(d.getKey())); // may be null
            pathFromRoot.put(d.getKey(), d.getValue());
        }

        for (TimeTreeNode grandChild : child.getChildren())
            simulateBranchSparse(child, grandChild, pathFromRoot);

        // restore the parent's path
        for (Map.Entry<Integer, Integer> u : undo.entrySet()) {
            if (u.getValue() == null) pathFromRoot.remove(u.getKey());
            else pathFromRoot.put(u.getKey(), u.getValue());
        }
    }

    private int rootState(int site) {
        return simRoot.getState(0, site);
    }

    private int stateAt(int site, Map<Integer, Integer> pathFromRoot) {
        Integer s = pathFromRoot.get(site);
        return (s != null) ? s : rootState(site);
    }

    /**
     * State of a site at a node: the youngest change on the path from the root, else the root state.
     */
    private int getEffectiveState(TimeTreeNode node, int site) {
        for (TimeTreeNode n = node; n != null; n = n.getParent()) {
            Map<Integer, Integer> diffs = nodeDifferences.get(n);
            if (diffs != null) {
                Integer s = diffs.get(site);
                if (s != null) return s;
            }
        }
        return rootState(site);
    }

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

    @GeneratorInfo(name = "SparsePhyloCTMC", verbClause = "is assumed to have evolved under",
            narrativeName = "phylogenetic continuous time Markov process",
            category = GeneratorCategory.PHYLO_LIKELIHOOD,
            description = "Simulate random number of mutations on branches along the tree. Variable sites and root sequence are stored to return the alignment.  ")
    @Override
    public RandomVariable<Alignment> sample() {
        simulateSparse();

        alignment = new VariantStyleAlignment(idMap, simRoot, new HashMap<>());

        SequenceType dt = (dataType != null) ? dataType.value() : simRoot.getSequenceType();
        final int nSites = getSiteCount();
        Alignment a = new SimpleAlignment(idMap, nSites, dt);

        // same set of output nodes and rows as AbstractPhyloCTMC.traverseTree
        fillOutput(tree.value().getRoot(), new HashMap<>(), a, nSites);

        return new RandomVariable<>("D", a, this);
    }

    private void fillOutput(TimeTreeNode node, Map<Integer, Integer> pathFromRoot, Alignment a, int nSites) {
        Map<Integer, Integer> path = pathFromRoot;
        Map<Integer, Integer> diffs = nodeDifferences.get(node);
        if (diffs != null && !diffs.isEmpty()) {
            path = new HashMap<>(pathFromRoot);
            path.putAll(diffs);
        }

        if (node.isLeaf() || (node.isSingleChildNonOrigin() && node.getId() != null)) {
            int row = node.getLeafIndex();
            for (int site = 0; site < nSites; site++)
                a.setState(row, site, rootState(site));
            for (Map.Entry<Integer, Integer> d : path.entrySet()) {
                a.setState(row, d.getKey(), d.getValue());
                alignment.setState(node.getId(), d.getKey(), d.getValue());
            }
        }

        for (TimeTreeNode child : node.getChildren())
            fillOutput(child, path, a, nSites);
    }

    /**
     * Copy a {@link VariantStyleAlignment} into a {@link SimpleAlignment}, matching rows by taxon name.
     */
    public static Alignment mapAlignment(VariantStyleAlignment alignment) {
        Alignment a = new SimpleAlignment(alignment.getTaxa(), alignment.nchar(), alignment.getSequenceType());
        String[] names = alignment.getTaxaNames();
        for (String name : names) {
            int row = a.indexOfTaxon(name);
            for (int j = 0; j < alignment.nchar(); j++) {
                a.setState(row, j, alignment.getState(name, j));
            }
        }
        return a;
    }

    // for unit test
    public Map<TimeTreeNode, Map<Integer,Integer>> getNodeDifferences(){
        return nodeDifferences;
    }

    public VariantStyleAlignment getAlignment() {
        return alignment;
    }

}
