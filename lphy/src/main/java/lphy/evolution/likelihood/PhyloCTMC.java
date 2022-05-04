package lphy.evolution.likelihood;

import jebl.evolution.sequences.SequenceType;
import lphy.core.distributions.Categorical;
import lphy.core.distributions.Utils;
import lphy.evolution.alignment.Alignment;
import lphy.evolution.alignment.SimpleAlignment;
import lphy.evolution.tree.TimeTree;
import lphy.evolution.tree.TimeTreeNode;
import lphy.graphicalModel.*;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import static lphy.graphicalModel.ValueUtils.doubleValue;

/**
 * Created by Alexei Drummond on 2/02/20.
 */
@Citation(
        value="Felsenstein, J. (1981). Evolutionary trees from DNA sequences: a maximum likelihood approach. Journal of molecular evolution, 17(6), 368-376.",
        title = "Evolutionary trees from DNA sequences: a maximum likelihood approach",
        year = 1981,
        authors = {"Felsenstein"},
        DOI="https://doi.org/10.1007/BF01734359")
public class PhyloCTMC implements GenerativeDistribution<Alignment> {

    Value<TimeTree> tree;
    Value<Number> clockRate;
    Value<Double[]> freq;
    Value<Double[][]> Q;
    Value<Double[]> siteRates;
    Value<Double[]> branchRates;
    Value<Integer> L;
    Value<SequenceType> dataType;
    RandomGenerator random;

    public static final String treeParamName = "tree";
    public static final String muParamName = "mu";
    public static final String rootFreqParamName = "freq";
    public static final String QParamName = "Q";
    public static final String siteRatesParamName = "siteRates";
    public static final String branchRatesParamName = "branchRates";
    public static final String LParamName = "L";
    public static final String dataTypeParamName = "dataType";

    final int numStates; // Q matrix row/column length

    // these are all initialized in setup method.
    private EigenDecomposition decomposition;
    private double[][] Ievc;
    private double[][] Evec;
    private Value<Double[]> rootFreqs;
    private SortedMap<String, Integer> idMap = new TreeMap<>();
    private double[][] transProb;
    private double[][] iexp;
    private double[] Eval;

    public PhyloCTMC(@ParameterInfo(name = treeParamName, verb = "on", narrativeName = "phylogenetic time tree", description = "the time tree.") Value<TimeTree> tree,
                     @ParameterInfo(name = muParamName, narrativeName = "molecular clock rate", description = "the clock rate. Default value is 1.0.", optional = true) Value<Number> mu,
                     @ParameterInfo(name = rootFreqParamName, description = "the root probabilities. Optional parameter. If not specified then first row of e^{100*Q) is used.", optional = true) Value<Double[]> rootFreq,
                     @ParameterInfo(name = QParamName, narrativeName="instantaneous rate matrix", description = "the instantaneous rate matrix.") Value<Double[][]> Q,
                     @ParameterInfo(name = siteRatesParamName, description = "a rate for each site in the alignment. Site rates are assumed to be 1.0 otherwise.",  optional = true) Value<Double[]> siteRates,
                     @ParameterInfo(name = branchRatesParamName, description = "a rate for each branch in the tree. Branch rates are assumed to be 1.0 otherwise.", optional = true) Value<Double[]> branchRates,
                     @ParameterInfo(name = LParamName, narrativeName="length", description = "length of the alignment", optional = true) Value<Integer> L,
                     @ParameterInfo(name = dataTypeParamName, description = "the data type used for simulations, default to nucleotide", optional = true) Value<SequenceType> dataType) {

        this.tree = tree;
        this.Q = Q;
        this.freq = rootFreq;
        this.clockRate = mu;
        this.siteRates = siteRates;
        this.branchRates = branchRates;
        this.L = L;

        numStates = Q.value().length;
        this.random = Utils.getRandom();
        iexp = new double[numStates][numStates];

        checkCompatibilities();
    }

    private int checkCompatibilities() {
        // check L and siteRates compatibility
        if (L != null && siteRates != null && L.value() != siteRates.value().length) {
            throw new RuntimeException(LParamName + " and " + siteRatesParamName + " have incompatible values!");
        }

        if (L != null) return L.value();
        if (siteRates != null) return siteRates.value().length;
        throw new RuntimeException("One of " + LParamName + " or " + siteRatesParamName + " must be specified.");
    }

    @Override
    public SortedMap<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        map.put(treeParamName, tree);
        if (clockRate != null) map.put(muParamName, clockRate);
        if (freq != null) map.put(rootFreqParamName, freq);
        map.put(QParamName, Q);
        if (siteRates != null) map.put(siteRatesParamName, siteRates);
        if (branchRates != null) map.put(branchRatesParamName, branchRates);
        if (L != null) map.put(LParamName, L);
        if (dataType != null) map.put(dataTypeParamName, dataType);
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(treeParamName)) tree = value;
        else if (paramName.equals(muParamName)) clockRate = value;
        else if (paramName.equals(rootFreqParamName)) freq = value;
        else if (paramName.equals(QParamName)) Q = value;
        else if (paramName.equals(siteRatesParamName)) siteRates = value;
        else if (paramName.equals(branchRatesParamName)) branchRates = value;
        else if (paramName.equals(LParamName)) L = value;
//        else if (paramName.equals(stateNamesParamName)) stateNames = value;
        else if (paramName.equals(dataTypeParamName)) dataType = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);
    }

    private void setup() {
        idMap.clear();
        fillIdMap(tree.value().getRoot(), idMap);

        transProb = new double[numStates][numStates];

        double[][] primitive = new double[numStates][numStates];
        for (int i = 0; i < numStates; i++) {
            for (int j = 0; j < numStates; j++) {
                primitive[i][j] = Q.value()[i][j];
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

        rootFreqs = freq;
        if (rootFreqs == null) {
            rootFreqs = computeEquilibrium(transProb);
        }
    }

    @GeneratorInfo(name = "PhyloCTMC", verbClause = "is assumed to have evolved under",
            narrativeName = "phylogenetic continuous time Markov process",
            category = GeneratorCategory.STOCHASTIC_PROCESS,
            description = "The phylogenetic continuous-time Markov chain distribution. A sequence is simulated for every leaf node, and every direct ancestor node with an id." +
            "(The sampling distribution that the phylogenetic likelihood is derived from.)")
    public RandomVariable<Alignment> sample() {
        setup();

        // default to nuc
        SequenceType dt = SequenceType.NUCLEOTIDE;

        if (dataType != null) dt = dataType.value();

        int length = checkCompatibilities();

        SimpleAlignment a = new SimpleAlignment(idMap, length, dt);

        double mu = (this.clockRate == null) ? 1.0 : doubleValue(clockRate);

        for (int i = 0; i < length; i++) {
            int rootState = Categorical.sample(rootFreqs.value(), random);
            traverseTree(tree.value().getRoot(), rootState, a, i, transProb, mu,
                    (siteRates == null) ? 1.0 : siteRates.value()[i]);
        }

        return new RandomVariable<>("D", a, this);
    }

    public Value<Double[]> getSiteRates() {
        return siteRates;
    }

    public Value<Double[]> getBranchRates() {
        return branchRates;
    }

    public Value<Number> getClockRate() {
        return clockRate;
    }

    public Value<Double[][]> getQ() {
        return Q;
    }

    public Value<TimeTree> getTree() {
        return tree;
    }

    public SequenceType getDataType() {
        // default to nuc
        if (dataType == null) return SequenceType.NUCLEOTIDE;
        return dataType.value();
    }

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

        return new Value<>("freq", freqs);
    }

    private void fillIdMap(TimeTreeNode node, SortedMap<String, Integer> idMap) {
        if (node.isLeaf() || node.getId() != null) {
            Integer i = idMap.get(node.getId());
            if (i == null) {
                int nextValue = 0;
                for (Integer j : idMap.values()) {
                    if (j >= nextValue) nextValue = j + 1;
                }
                idMap.put(node.getId(), nextValue);
                node.setLeafIndex(nextValue);
            } else {
                node.setLeafIndex(i);
            }
        }
        for (TimeTreeNode child : node.getChildren()) {
            fillIdMap(child, idMap);
        }

    }

    private void traverseTree(TimeTreeNode node, int nodeState, SimpleAlignment alignment, int pos, double[][] transProb, double clockRate, double siteRate) {

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

    private int drawState(double[] p) {
        double U = random.nextDouble();
        double totalP = p[0];
        if (U <= totalP) return 0;
        for (int i = 1; i < p.length; i++) {
            totalP += p[i];
            if (U <= totalP) return i;
        }
        throw new RuntimeException("p vector doesn't add to 1.0!");
    }

    private void getTransitionProbabilities(double branchLength, double[][] transProbs) {

        int i, j, k;
        double temp;

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
