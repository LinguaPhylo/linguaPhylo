package lphy.base.evolution.likelihood;

import jebl.evolution.sequences.SequenceType;
import lphy.base.evolution.alignment.Alignment;
import lphy.base.evolution.eigensystem.*;
import lphy.base.evolution.tree.TimeTree;
import lphy.base.evolution.tree.TimeTreeNode;
import lphy.core.logger.LoggerUtils;
import lphy.core.model.GenerativeDistribution;
import lphy.core.model.Value;
import lphy.core.model.ValueUtils;
import lphy.core.simulator.RandomUtils;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;

import java.util.*;

/**
 * Extract Alexei's code shared by {@link PhyloCTMC} and {@link PhyloCTMCSiteModel}
 * for all kinds of PhyloCTMC.
 * Created by Alexei Drummond on 2/02/20.
 * @author Alexei Drummond
 */
public abstract class AbstractPhyloCTMC implements GenerativeDistribution<Alignment> {

    public static final String treeParamName = "tree";
    public static final String muParamName = "mu";
    public static final String rootFreqParamName = "freq";
    public static final String branchRatesParamName = "branchRates";
    public static final String LParamName = "L";
    public static final String dataTypeParamName = "dataType";
    public static final String rootSeqParamName = "root";

    protected Value<TimeTree> tree;
    protected Value<Number> clockRate;
    // root freqs input
    protected Value<Double[]> freq;
    protected Value<Double[]> branchRates;
    protected Value<Integer> L;
    protected Value<SequenceType> dataType;
    protected RandomGenerator random;

    // these are all initialized in setup method.
    protected Value<Double[]> rootFreqs;
    protected SortedMap<String, Integer> idMap = new TreeMap<>();
    protected double[][] transProb;
    private EigenDecomposition decomposition;
    protected EigenSystem complexEigenSystem;
    protected EigenDecompositionExt complexDecomposition;
    private double[][] Ievc;
    private double[][] Evec;
    private double[][] iexp;
    private double[] Eval;


    public AbstractPhyloCTMC(Value<TimeTree> tree, Value<Number> clockRate, Value<Double[]> freq,
                             Value<Double[]> branchRates, Value<Integer> l, Value<SequenceType> dataType) {
        this.tree = tree;
        this.clockRate = clockRate;

        // root frequencies should sum to 1
        if (freq != null) {
            this.freq = freq;
            if (ValueUtils.sum(freq) != 1)
                throw new IllegalArgumentException("root frequencies should sum to 1 !" + freq);
        }

        this.branchRates = branchRates;
        this.L = l;
        this.dataType = dataType;

        this.random = RandomUtils.getRandom();

        Double[] treeBranchRates = tree.value().getBranchRates();

        if (treeBranchRates != null && treeBranchRates.length > 0) {
            if (this.branchRates != null) { // have branchRates from input but tree also has branch rates
                LoggerUtils.log.warning("PhyloCTMC has branchRates from input parameter and tree has branch rates, " +
                        "default to using input parameter branchRates.");
            } else { // if tree has branch rates, then use them
                this.branchRates = new Value<>("branchRates", treeBranchRates);
            }
        }
        //        checkCompatibilities();
    }

    //+++ protected methods +++//

    // check parameters compatibility
    protected abstract void checkCompatibilities();

    // return site count
    protected abstract int getSiteCount();

    // return Q matrix
    protected abstract Double[][] getQ();

    // flags when asymmetric rate matrix used
    protected boolean canReturnComplexDiagonalization() {
        return false;
    }

    // shared code in setup()
    protected void computePAndRootFreqs() {
        idMap.clear();
        // if internal nodes have id, then simulate sequences,
        // otherwise only sequences on tips.
        fillIdMap(tree.value(), idMap);

        Double[][] Qm = getQ();
        if (Qm == null)
            throw new IllegalArgumentException("matrix Q[][] must be provided !");
        // Q matrix row/column length
        final int numStates = Qm.length;

        transProb = new double[numStates][numStates];
        iexp = new double[numStates][numStates];

        // regular method
        if (!canReturnComplexDiagonalization()) {
            double[][] primitive = new double[numStates][numStates];
            for (int i = 0; i < numStates; i++) {
                for (int j = 0; j < numStates; j++) {
                    primitive[i][j] = Qm[i][j];
                }
            }
            Array2DRowRealMatrix Qmatrix = new Array2DRowRealMatrix(primitive);

            decomposition = new EigenDecomposition(Qmatrix);
            Eval = decomposition.getRealEigenvalues();
            Ievc = new double[numStates][numStates];

            // Eigen vectors
            Evec = new double[numStates][numStates];
            for (int i = 0; i < numStates; i++) {
                RealVector evec = decomposition.getEigenvector(i);
                for (int j = 0; j < numStates; j++) {
                    Evec[j][i] = evec.getEntry(j);
                }
            }

            luinverse(Evec, Ievc, numStates);
        } else {
            getComplexEigen(numStates, Qm);
        }

        rootFreqs = freq;
        if (rootFreqs == null) {
            rootFreqs = computeEquilibrium(transProb);
        }
    }

    protected void traverseTree(TimeTreeNode node, int nodeState, Alignment alignment,
                                int pos, double[][] transProb, double clockRate, double siteRate) {

        if (node.isLeaf() || (node.isSingleChildNonOrigin() && node.getId() != null)) {
            alignment.setState(node.getLeafIndex(), pos, nodeState); // no ambiguous state
        }
        List<TimeTreeNode> children = node.getChildren();
        for (TimeTreeNode child : children) {
            double branchLength = siteRate * clockRate * (node.getAge() - child.getAge());

            if (branchRates != null) {
                branchLength *= branchRates.value()[child.getIndex()];
            }

            getTransitionProbabilities(branchLength, transProb);
            // draw state from Q
            int state = drawState(transProb[nodeState]);

            traverseTree(child, state, alignment, pos, transProb, clockRate, siteRate);
        }
    }

    //+++ public and getter +++//

    // setup() before sample()
    public void setup() {
        // overwrite the default if more setup
        computePAndRootFreqs();
    }

    public Value<Double[]> getBranchRates() {
        return branchRates;
    }

    public Value<Number> getClockRate() {
        return clockRate;
    }

    public Value<TimeTree> getTree() {
        return tree;
    }

    public SequenceType getDataType() {
        if (dataType == null) return SequenceType.NUCLEOTIDE;
        return dataType.value();
    }

    // make public for unit test
    public void getTransitionProbabilities(double branchLength, double[][] transProbs) {
        // regular method
        if (!canReturnComplexDiagonalization()) {
            int i, j, k;
            double temp;

            final int numStates = transProbs.length; // getQ().length ?
            // inverse Eigen vectors
            // Eigen values
            for (i = 0; i < numStates; i++) {
                temp = FastMath.exp(branchLength * Eval[i]);
                for (j = 0; j < numStates; j++) {
                    iexp[i][j] = Ievc[i][j] * temp;
                }
            }

            for (i = 0; i < numStates; i++) {
                for (j = 0; j < numStates; j++) {
                    temp = 0.0;
                    for (k = 0; k < numStates; k++) {
                        temp += Evec[i][k] * iexp[k][j];
                    }
                    transProbs[i][j] = FastMath.abs(temp);
                }
            }
        } else getComplexTransitionProbabilities(branchLength, transProbs);
    }

    //+++ private methods +++//

    private Value<Double[]> computeEquilibrium(double[][] transProb) {
        getTransitionProbabilities(100, transProb);
        Double[] freqs = new Double[transProb.length];
        for (int i = 0; i < freqs.length; i++) {
            freqs[i] = transProb[0][i];
            for (int j = 1; j < freqs.length; j++) {
                if (Math.abs(transProb[0][i] - transProb[j][i]) > 1e-5) {
                    System.out.println("WARNING: branch length used to get equilibrium distribution was not long enough!");
                }

            }
        }
        if (canReturnComplexDiagonalization()) {
            LoggerUtils.log.warning("Complex eigen decomposition methods being used.");
        }

        return new Value<>(null, freqs);
    }

    private boolean isValidIntegers(Map<String, Integer> idMap) {
        Collection<Integer> indices = idMap.values();

        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;

        for (Integer i : indices) {
            if (i < min) min = i;
            if (i > max) max = i;
        }

        return (min == 0) && (max == idMap.size()-1);
    }

    private int nextValue(SortedMap<String, Integer> idMap) {
        int nextValue = 0;
        for (Integer j : idMap.values()) {
            if (j >= nextValue) nextValue = j + 1;
        }
        return nextValue;
    }

    private void fillIdMap(TimeTree tree, SortedMap<String, Integer> idMap) {
        fillIdMap(tree.getRoot(), idMap, true);
        if (!isValidIntegers(idMap)) {
            idMap.clear();
            fillIdMap(tree.getRoot(), idMap, false);
        }
    }

    private void fillIdMap(TimeTreeNode node, SortedMap<String, Integer> idMap, boolean parseIntegers) {
        // if internal nodes have id, then simulate sequences, otherwise only sequences on tips.
        if (node.isLeaf() || node.getId() != null) {

            Integer id = idMap.get(node.getId());
            if (id == null) {
                if (parseIntegers) {
                    try {
                        id = Integer.parseInt(node.getId());
                    } catch (NumberFormatException ignored) {
                        id = nextValue(idMap);
                    }
                } else {
                    id = nextValue(idMap);
                }
                idMap.put(node.getId(), id);
                node.setLeafIndex(id);
            } else {
                node.setLeafIndex(id);
            }
        }
        for (TimeTreeNode child : node.getChildren()) {
            fillIdMap(child, idMap, parseIntegers);
        }

    }

    private int drawState(double[] p) {
        double U = random.nextDouble();
        double totalP = p[0];
        if (U <= totalP) return 0;
        for (int i = 1; i < p.length; i++) {
            totalP += p[i];
            if (U <= totalP) return i;
        }
        if (Math.abs(totalP - 1.0) < 1e-6) return p.length - 1;
        throw new RuntimeException("p vector should add to 1.0 but adds to " + totalP +  " instead.");
    }

    // gets complex eigensystem with colt dependency
    private void getComplexEigen(int numStates, Double[][] Qm) {
        complexEigenSystem = new ComplexColtEigenSystem(numStates);
        complexDecomposition = complexEigenSystem.decomposeMatrix(Qm);
        Eval = complexDecomposition.getEigenValues();

        Evec = new double[numStates][numStates];
        Ievc = new double[numStates][numStates];
        double[] evec = complexDecomposition.getEigenVectors();
        double[] ievc = complexDecomposition.getInverseEigenVectors();
        int x = 0;
        for (int i = 0; i < numStates; i++) {
            for (int j = 0; j < numStates; j++) {
                Evec[i][j] = evec[x];
                Ievc[i][j] = ievc[x];
                x++;
            }
        }
    }

    // nonreversible Q matrix eigen decomposition
    private void getComplexTransitionProbabilities(double branchLength, double[][] transProbs) {
        final int numStates = transProbs.length; // getQ().length ?
        double temp;
        double[] Ievc2 = new double[numStates * numStates];
        double[] Evec2 = new double[numStates * numStates];

        double[] EvalImag = new double[numStates];
        System.arraycopy(Eval, numStates, EvalImag, 0, numStates);
        int k = 0;
        for (int i = 0; i < numStates; i++) {
            for (int j = 0; j < numStates; j++) {
                Ievc2[k] = Ievc[i][j];
                Evec2[k] = Evec[i][j];
                k++;
            }
        }
        for (int i = 0; i < numStates; i++) {

            if (EvalImag[i] == 0) {
                // 1x1 block
                temp = Math.exp(branchLength * Eval[i]);
                for (int j = 0; j < numStates; j++) {
                    iexp[i][j] = Ievc2[i * numStates + j] * temp;
                }
            } else {
                // 2x2 conjugate block
                // If A is 2x2 with complex conjugate pair eigenvalues a +/- bi, then
                // exp(At) = exp(at)*( cos(bt)I + \frac{sin(bt)}{b}(A - aI)).
                int i2 = i + 1;
                double b = EvalImag[i];
                double expat = Math.exp(branchLength * Eval[i]);
                double expatcosbt = expat * Math.cos(branchLength * b);
                double expatsinbt = expat * Math.sin(branchLength * b);

                for (int j = 0; j < numStates; j++) {
                    iexp[i][j] = expatcosbt * Ievc2[i * numStates + j] +
                            expatsinbt * Ievc2[i2 * numStates + j];
                    iexp[i2][j] = expatcosbt * Ievc2[i2 * numStates + j] -
                            expatsinbt * Ievc2[i * numStates + j];
                }
                i++; // processed two conjugate rows
            }
        }

        for (int i = 0; i < numStates; i++) {
            for (int j = 0; j < numStates; j++) {
                temp = 0.0;
                for (k = 0; k < numStates; k++) {
                    temp += Evec2[i * numStates + k] * iexp[k][j];
                }
                transProbs[i][j] = Math.abs(temp);
            }
        }

    }


    private static double EPSILON = 2.220446049250313E-16;

    private static void luinverse(double[][] inmat, double[][] imtrx, int size) throws IllegalArgumentException {
        int i, j, k, l, maxi = 0, idx, ix, jx;
        double sum, tmp, maxb, aw;
        int[] index;
        double[] wk;
        double[][] omtrx;


        index = new int[size];
        omtrx = new double[size][size];

        /* copy inmat to omtrx */
        for (i = 0; i < size; i++) {
            for (j = 0; j < size; j++) {
                omtrx[i][j] = inmat[i][j];
            }
        }

        wk = new double[size];
        aw = 1.0;
        for (i = 0; i < size; i++) {
            maxb = 0.0;
            for (j = 0; j < size; j++) {
                if (Math.abs(omtrx[i][j]) > maxb) {
                    maxb = Math.abs(omtrx[i][j]);
                }
            }
            if (maxb == 0.0) {
                /* Singular matrix */
                System.err.println("Singular matrix encountered");
                throw new IllegalArgumentException("Singular matrix");
            }
            wk[i] = 1.0 / maxb;
        }
        for (j = 0; j < size; j++) {
            for (i = 0; i < j; i++) {
                sum = omtrx[i][j];
                for (k = 0; k < i; k++) {
                    sum -= omtrx[i][k] * omtrx[k][j];
                }
                omtrx[i][j] = sum;
            }
            maxb = 0.0;
            for (i = j; i < size; i++) {
                sum = omtrx[i][j];
                for (k = 0; k < j; k++) {
                    sum -= omtrx[i][k] * omtrx[k][j];
                }
                omtrx[i][j] = sum;
                tmp = wk[i] * Math.abs(sum);
                if (tmp >= maxb) {
                    maxb = tmp;
                    maxi = i;
                }
            }
            if (j != maxi) {
                for (k = 0; k < size; k++) {
                    tmp = omtrx[maxi][k];
                    omtrx[maxi][k] = omtrx[j][k];
                    omtrx[j][k] = tmp;
                }
                aw = -aw;
                wk[maxi] = wk[j];
            }
            index[j] = maxi;
            if (omtrx[j][j] == 0.0) {
                omtrx[j][j] = EPSILON;
            }
            if (j != size - 1) {
                tmp = 1.0 / omtrx[j][j];
                for (i = j + 1; i < size; i++) {
                    omtrx[i][j] *= tmp;
                }
            }
        }
        for (jx = 0; jx < size; jx++) {
            for (ix = 0; ix < size; ix++) {
                wk[ix] = 0.0;
            }
            wk[jx] = 1.0;
            l = -1;
            for (i = 0; i < size; i++) {
                idx = index[i];
                sum = wk[idx];
                wk[idx] = wk[i];
                if (l != -1) {
                    for (j = l; j < i; j++) {
                        sum -= omtrx[i][j] * wk[j];
                    }
                } else if (sum != 0.0) {
                    l = i;
                }
                wk[i] = sum;
            }
            for (i = size - 1; i >= 0; i--) {
                sum = wk[i];
                for (j = i + 1; j < size; j++) {
                    sum -= omtrx[i][j] * wk[j];
                }
                wk[i] = sum / omtrx[i][i];
            }
            for (ix = 0; ix < size; ix++) {
                imtrx[ix][jx] = wk[ix];
            }
        }
        wk = null;
        index = null;
        omtrx = null;
    }

}
