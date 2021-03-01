package lphy.evolution.likelihood;

import jebl.evolution.sequences.SequenceType;
import lphy.core.distributions.Categorical;
import lphy.core.distributions.Utils;
import lphy.evolution.alignment.Alignment;
import lphy.evolution.alignment.SimpleAlignment;
import lphy.evolution.sequences.DataType;
import lphy.evolution.sequences.SequenceTypeFactory;
import lphy.evolution.sitemodel.SiteModel;
import lphy.evolution.tree.TimeTree;
import lphy.evolution.tree.TimeTreeNode;
import lphy.graphicalModel.*;
import lphy.utils.LoggerUtils;
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
 * Created by adru001 on 2/02/20.
 */
@Citation(
        value="Felsenstein, J. (1981). Evolutionary trees from DNA sequences: a maximum likelihood approach. Journal of molecular evolution, 17(6), 368-376.",
        title = "Evolutionary trees from DNA sequences: a maximum likelihood approach",
        year = 1981,
        authors = {"Felsenstein"},
        DOI="https://doi.org/10.1007/BF01734359")
public class PhyloCTMCSiteModel implements GenerativeDistribution<Alignment> {

    Value<TimeTree> tree;
    Value<Number> clockRate;
    Value<SiteModel> siteModel;
    Value<Double[]> branchRates;
    Value<Integer> L;
    Value<String> dataType;
    RandomGenerator random;
    private Value<Double[]> rootFreqs;
    Value<Double[]> freq;


    public static final String treeParamName = "tree";
    public static final String muParamName = "mu";
    public static final String rootFreqParamName = "freq";
    public static final String siteModelParamName = "siteModel";

    public static final String branchRatesParamName = "branchRates";
    public static final String LParamName = "L";
    public static final String dataTypeParamName = "dataType";

    final int numStates;

    // these are all initialized in setup method.
    private EigenDecomposition decomposition;
    private double[][] Ievc;
    private double[][] Evec;
    private SortedMap<String, Integer> idMap = new TreeMap<>();
    private double[][] transProb;
    private double[][] iexp;
    private double[] Eval;

    protected final SequenceTypeFactory sequenceTypeFactory = new SequenceTypeFactory();

    int siteCount;
    double[] finalSiteRates;
    double propInvariable;

    public PhyloCTMCSiteModel(@ParameterInfo(name = treeParamName, verb = "on", narrativeName = "phylogenetic time tree", description = "the time tree.") Value<TimeTree> tree,
                              @ParameterInfo(name = muParamName, narrativeName = "molecular clock rate", description = "the clock rate. Default value is 1.0.", optional = true) Value<Number> mu,
                              @ParameterInfo(name = rootFreqParamName, description = "the root probabilities. Optional parameter. If not specified then first row of e^{100*Q) is used.", optional = true) Value<Double[]> rootFreq,
                              @ParameterInfo(name = siteModelParamName, narrativeName = "site model", description = "the site model") Value<SiteModel> siteModel,
                              @ParameterInfo(name = branchRatesParamName, description = "a rate for each branch in the tree. Branch rates are assumed to be 1.0 otherwise.", optional = true) Value<Double[]> branchRates,
                              @ParameterInfo(name = LParamName, narrativeName="length", description = "length of the alignment", optional = true) Value<Integer> L,
                              @ParameterInfo(name = dataTypeParamName, description = "the data type used for simulations", optional = true) Value<String> dataType) {

        this.tree = tree;
        this.siteModel = siteModel;
        this.clockRate = mu;
        this.branchRates = branchRates;
        this.freq = rootFreq;
        this.L = L;
        numStates = siteModel.value().stateCount();
        this.random = Utils.getRandom();
        iexp = new double[numStates][numStates];

        this.dataType = dataType;
        siteCount = checkCompatibilities();
    }

    private int checkCompatibilities() {
        // check L and siteRates compatibility
        if (L != null && siteModel.value().hasSiteRates() && L.value() != siteModel.value().siteRates().length) {
            throw new RuntimeException(LParamName + " and " + siteModelParamName + " site rates have incompatible values!");
        }

        if (L != null) return L.value();
        if (siteModel.value().hasSiteRates()) return siteModel.value().siteRates().length;
        throw new RuntimeException("One of " + LParamName + " or " + siteModelParamName + " site rates must be specified.");
    }

    @Override
    public SortedMap<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        map.put(treeParamName, tree);
        if (clockRate != null) map.put(muParamName, clockRate);
        if (rootFreqs != null) map.put(rootFreqParamName, rootFreqs);
        map.put(siteModelParamName, siteModel);
        if (branchRates != null) map.put(branchRatesParamName, branchRates);
        if (L != null) map.put(LParamName, L);
        if (dataType != null) map.put(dataTypeParamName, dataType);
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        switch (paramName) {
            case treeParamName:
                tree = value;
                break;
            case rootFreqParamName:
                rootFreqs = value;
                break;
            case muParamName:
                clockRate = value;
                break;
            case siteModelParamName:
                siteModel = value;
                break;
            case branchRatesParamName:
                branchRates = value;
                break;
            case LParamName:
                L = value;
                break;
            case dataTypeParamName:
                dataType = value;
                break;
            default:
                throw new RuntimeException("Unrecognised parameter name: " + paramName);
        }
    }

    private void setup() {

        siteCount = checkCompatibilities();

        Double[][] Q = siteModel.value().getQ();

        idMap.clear();
        fillIdMap(tree.value().getRoot(), idMap);

        transProb = new double[numStates][numStates];

        double[][] primitive = new double[numStates][numStates];
        for (int i = 0; i < numStates; i++) {
            for (int j = 0; j < numStates; j++) {
                primitive[i][j] = Q[i][j];
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

        finalSiteRates = new double[siteCount];
        propInvariable = siteModel.value().getProportionInvariable();
    }

    @GeneratorInfo(name = "PhyloCTMC",
            verbClause = "is assumed to have evolved under",
            narrativeName = "phylogenetic continuous time Markov process",
            description = "The phylogenetic continuous-time Markov chain distribution. A sequence is simulated for every leaf node, and every direct ancestor node with an id." +
            "(The sampling distribution that the phylogenetic likelihood is derived from.)")
    public RandomVariable<Alignment> sample() {
        setup();

        for (int i = 0; i < finalSiteRates.length; i++) {
            if (propInvariable > 0 && random.nextDouble()<propInvariable) {
                finalSiteRates[i] = 0;
            } else if (siteModel.value().hasSiteRates()) {
                finalSiteRates[i] = siteModel.value().siteRates()[i];
            } else {
                finalSiteRates[i] = 1.0;
            }
        }

        SequenceType sequenceType = null;
        // TODO stateNames != null, how to pass states into Standard
        // dataType="standard", use numStates to create Standard
        if (dataType != null) {
            if (isStandardDataType())
                sequenceType = sequenceTypeFactory.getStandardDataType(numStates);
            else
                sequenceType = sequenceTypeFactory.getDataType(dataType.value());
        }

        if (sequenceType == null) {
            sequenceType = sequenceTypeFactory.getSequenceType(numStates);
            LoggerUtils.log.warning("Data type is unknown ! Assign data type (" + sequenceType +
                    ") to the sequences on the basis of " + numStates + " states !");
        }
        if (sequenceType == null)
            throw new UnsupportedOperationException("Cannot define sequence type, numStates = " + numStates);

        // validate num of states
        if (sequenceType.getCanonicalStateCount() != numStates)
            throw new UnsupportedOperationException("Sequence type " + sequenceType + " canonical state count = " +
                    sequenceType.getCanonicalStateCount() + "  !=  transProb.length = " + numStates);

        SimpleAlignment a = new SimpleAlignment(idMap, siteCount, sequenceType);

        double mu = (this.clockRate == null) ? 1.0 : doubleValue(clockRate);

        for (int i = 0; i < siteCount; i++) {

            int rootState = Categorical.sample(rootFreqs.value(), random);
            traverseTree(tree.value().getRoot(), rootState, a, i, transProb, mu,
                    finalSiteRates[i]);
        }

        return new RandomVariable<>(null, a, this);
    }

    public Value<SiteModel> getSiteModel() {
        return siteModel;
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

    public boolean isStandardDataType() {
        return dataType != null && DataType.isStandard(dataType.value());
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
