package lphy.evolution.likelihood;

import lphy.evolution.DataFrame;
import lphy.evolution.alignment.SimpleAlignment;
import lphy.evolution.tree.TimeTree;
import lphy.evolution.tree.TimeTreeNode;
import lphy.evolution.alignment.Alignment;
import lphy.core.distributions.Categorical;
import lphy.core.distributions.Utils;
import lphy.graphicalModel.*;
import org.apache.commons.math3.linear.*;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;

import java.util.*;

/**
 * Created by adru001 on 2/02/20.
 */
public class PhyloCTMC implements GenerativeDistribution<Alignment> {

    Value<TimeTree> tree;
    Value<Double> clockRate;
    Value<Double[]> freq;
    Value<Double[][]> Q;
    Value<Double[]> siteRates;
    Value<Double[]> branchRates;
    Value<Integer> L;
    Value<DataFrame> frame;
    RandomGenerator random;
    Value<Boolean> toClamp;

    public final String treeParamName;
    public final String muParamName;
    public final String rootFreqParamName;
    public final String QParamName;
    public final String siteRatesParamName;
    public final String branchRatesParamName;
    public final String LParamName;
    public final String frameParamName;
    public final String clampParamName;

    int numStates;

    // these are all initialized in setup method.
    private EigenDecomposition decomposition;
    private double[][] Ievc;
    private double[][] Evec;
    private Value<Double[]> rootFreqs;
    private SortedMap<String, Integer> idMap = new TreeMap<>();
    private double[][] transProb;
    private double[][] iexp;
    private double[] Eval;

    public PhyloCTMC(@ParameterInfo(name = "tree", description = "the time tree.") Value<TimeTree> tree,
                     @ParameterInfo(name = "mu", description = "the clock rate. Default value is 1.0.", optional = true) Value<Double> mu,
                     @ParameterInfo(name = "freq", description = "the root probabilities. Optional parameter. If not specified then first row of e^{100*Q) is used.", optional = true) Value<Double[]> rootFreq,
                     @ParameterInfo(name = "Q", description = "the instantaneous rate matrix.") Value<Double[][]> Q,
                     @ParameterInfo(name = "siteRates", description = "a rate for each site in the alignment. Site rates are assumed to be 1.0 otherwise.", optional = true) Value<Double[]> siteRates,
                     @ParameterInfo(name = "branchRates", description = "a rate for each branch in the tree. Branch rates are assumed to be 1.0 otherwise.", optional = true) Value<Double[]> branchRates,
                     @ParameterInfo(name = "L", description = "length of the alignment", optional = true) Value<Integer> L,
                     @ParameterInfo(name = "frame", description = "data frame, either specify the length or a data frame but not both.", optional = true) Value<DataFrame> frame,
                     @ParameterInfo(name = "clamp", description = "clamp data if frame is observation", optional = true) Value<Boolean> toClamp) {

        this.tree = tree;
        this.Q = Q;
        this.freq = rootFreq;
        this.clockRate = mu;
        this.siteRates = siteRates;
        this.branchRates = branchRates;
        this.L = L;
        this.frame = frame;
        this.toClamp = toClamp;

        numStates = Q.value().length;
        this.random = Utils.getRandom();
        iexp = new double[numStates][numStates];

        treeParamName = getParamName(0);
        muParamName = getParamName(1);
        rootFreqParamName = getParamName(2);
        QParamName = getParamName(3);
        siteRatesParamName = getParamName(4);
        branchRatesParamName = getParamName(5);
        LParamName = getParamName(6);
        frameParamName = getParamName(7);
        clampParamName = getParamName(8);

        checkCompatibilities();
    }

    private int checkCompatibilities() {
        // check L and alignment compatibility
        if (L != null && frame != null) {
            if (L.value() != frame.value().nchar()) {
                throw new IllegalArgumentException(LParamName + " and " + frameParamName + " do not match!");
            }
        }

        // check L and siteRates compatibility
        if (L != null && siteRates != null && L.value() != siteRates.value().length) {
            throw new RuntimeException(LParamName + " and " + siteRatesParamName + " have incompatible values!");
        }

        // check alignment and siteRates compatibility
        if (frame != null && siteRates != null && frame.value().nchar() != siteRates.value().length) {
            throw new RuntimeException(frameParamName + " and " + siteRatesParamName + " have incompatible values!");
        }

        if (frame != null) {
            if (frame.value().ntaxa() != tree.value().ntaxa()) {

                String treeName = tree.isAnonymous() ? "tree" : tree.getId();
                String frameName = frame.isAnonymous() ? "frame" : frame.getId();

                throw new IllegalArgumentException(treeParamName + " and " + frameParamName +
                        " have different ntaxa! " + treeName + ".ntaxa=" +tree.value().ntaxa() + ", " + frameName +
                        ".ntaxa=" + frame.value().ntaxa() );
            }
        }

        if (L != null) return L.value();
        if (frame != null) return frame.value().nchar();
        if (siteRates != null) return siteRates.value().length;
        throw new RuntimeException("One of " + LParamName + ", " + frame + " or " + siteRatesParamName + " must be specified.");
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
        if (frame != null) map.put(frameParamName, frame);
        if (toClamp != null) map.put(clampParamName, toClamp);
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
        else if (paramName.equals(frameParamName)) frame = value;
        else if (paramName.equals(clampParamName)) toClamp = value;
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

    @GeneratorInfo(name = "PhyloCTMC", description = "The phylogenetic continuous-time Markov chain distribution. " +
            "(The sampling distribution that the phylogenetic likelihood is derived from.)")
    public RandomVariable<Alignment> sample() {
        setup();

        int length = checkCompatibilities();

        Alignment a = new Alignment(tree.value().n(), length, idMap, transProb.length);

        double mu = (this.clockRate == null) ? 1.0 : this.clockRate.value();

        for (int i = 0; i < length; i++) {
            int rootState = Categorical.sample(rootFreqs.value(), random);
            traverseTree(tree.value().getRoot(), rootState, a, i, transProb, mu,
                    (siteRates == null) ? 1.0 : siteRates.value()[i]);
        }

        if (toClamp != null && toClamp.value() == Boolean.TRUE)
            clamp(a);

        return new RandomVariable<>("D", a, this);
    }

    // TODO need smarter replacing method
    // replace alg and taxa in tre into those retrieved from frame
    public void clamp(Alignment alg) {
        if (frame == null || ! (frame.value() instanceof SimpleAlignment) )
            throw new IllegalArgumentException("Clamping needs SimpleAlignment in dataframe ! " + frame);

        alg = (SimpleAlignment) frame.value();
    }


    public Value<Double[]> getSiteRates() {
        return siteRates;
    }

    public Value<Double[]> getBranchRates() {
        return branchRates;
    }

    public Value<Double> getClockRate() {
        return clockRate;
    }

    public Value<Double[][]> getQ() {
        return Q;
    }

    public Value<TimeTree> getTree() {
        return tree;
    }

    private Value<Double[]> computeEquilibrium(double[][] transProb) {
        getTransitionProbabilities(100, transProb);
        Double[] freqs = new Double[transProb.length];
        for (int i = 0; i < freqs.length; i++) {
            freqs[i] = transProb[0][i];
            for (int j = 1; j < freqs.length; j++) {
                if (Math.abs(transProb[0][i] - transProb[j][i]) > 1e-6) {
                    System.out.println("WARNING: branch length used to get equilibrium distribution was not long enough!");
                }

            }
        }

        return new Value<>("freq", freqs);
    }

    private void fillIdMap(TimeTreeNode node, SortedMap<String, Integer> idMap) {
        if (node.isLeaf()) {
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
        } else {
            for (TimeTreeNode child : node.getChildren()) {
                fillIdMap(child, idMap);
            }
        }
    }

    private void traverseTree(TimeTreeNode node, int nodeState, Alignment alignment, int pos, double[][] transProb, double clockRate, double siteRate) {

        if (node.isLeaf()) {
            alignment.setState(node.getLeafIndex(), pos, nodeState);
        } else {
            List<TimeTreeNode> children = node.getChildren();
            for (int i = 0; i < children.size(); i++) {
                TimeTreeNode child = children.get(i);
                double branchLength = siteRate * clockRate * (node.getAge() - child.getAge());

                if (branchRates != null) {
                    branchLength *= branchRates.value()[child.getIndex()];
                }

                getTransitionProbabilities(branchLength, transProb);
                int state = drawState(transProb[nodeState]);

                traverseTree(child, state, alignment, pos, transProb, clockRate, siteRate);
            }
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
