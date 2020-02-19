package james.core;

import james.TimeTree;
import james.TimeTreeNode;
import james.core.distributions.Utils;
import james.graphicalModel.*;
import james.graphicalModel.types.IntegerValue;
import org.apache.commons.math3.linear.*;

import java.util.*;
import java.util.stream.Stream;

/**
 * Created by adru001 on 2/02/20.
 */
public class PhyloCTMC implements GenerativeDistribution<Alignment> {

    Value<TimeTree> tree;
    Value<Double> clockRate;
    Value<Double[]> freq;
    Value<Double[][]> Q;
    Value<Double[]> siteRates;
    Value<Integer> L;
    Random random;

    String treeParamName;
    String muParamName;
    String rootFreqParamName;
    String QParamName;
    String siteRatesParamName;
    String LParamName;

    int numStates;

    // these are all initialized in setup method.
    private EigenDecomposition decomposition;
    private double[][] Ievc;
    private double[][] Evec;
    private Value<Double[]> rootFreqs;
    private SortedMap<String, Integer> idMap = new TreeMap<>();
    private double[][] transProb;

    public PhyloCTMC(@ParameterInfo(name = "tree", description = "the time tree.") Value<TimeTree> tree,
                     @ParameterInfo(name = "mu", description = "the clock rate.") Value<Double> mu,
                     @ParameterInfo(name = "freq", description = "the root probabilities. Optional parameter. If not specified then first row of e^{100*Q) is used.", optional = true) Value<Double[]> rootFreq,
                     @ParameterInfo(name = "Q", description = "the instantaneous rate matrix.") Value<Double[][]> Q,
                     @ParameterInfo(name = "siteRates", description = "a rate for each site in the alignment. Site rates are assumed to be 1.0 otherwise.", optional = true) Value<Double[]> siteRates,
                     @ParameterInfo(name = "L", description = "length of the alignment", optional = true) Value<Integer> L) {

        this.tree = tree;
        this.Q = Q;
        this.freq = rootFreq;
        this.clockRate = mu;
        this.siteRates = siteRates;
        this.L = L;
        numStates = Q.value().length;
        this.random = Utils.getRandom();

        treeParamName = getParamName(0);
        muParamName = getParamName(1);
        rootFreqParamName = getParamName(2);
        QParamName = getParamName(3);
        siteRatesParamName = getParamName(4);
        LParamName = getParamName(5);
    }

    @Override
    public SortedMap<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        map.put(treeParamName, tree);
        map.put(muParamName, clockRate);
        if (freq != null) map.put(rootFreqParamName, freq);
        map.put(QParamName, Q);
        if (siteRates != null) map.put(siteRatesParamName, siteRates);
        if (L != null) map.put(LParamName, L);
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(treeParamName)) tree = value;
        else if (paramName.equals(muParamName)) clockRate = value;
        else if (paramName.equals(rootFreqParamName)) freq = value;
        else if (paramName.equals(QParamName)) Q = value;
        else if (paramName.equals(siteRatesParamName)) siteRates = value;
        else if (paramName.equals(LParamName)) L = value;
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
        Ievc = new double[numStates][numStates];

        // Eigen vectors

        Evec = new double[numStates][numStates];
        for (int i = 0; i < numStates; i++) {
            RealVector evec = decomposition.getEigenvector(i);
            for (int j = 0; j < numStates; j++) {
                Evec[j][i] = evec.getEntry(j);
            }
        }

        // TODO this should not be done on every branch. Can be done just once!!
        luinverse(Evec, Ievc, numStates);

        rootFreqs = freq;
        if (rootFreqs == null) {
            rootFreqs = computeEquilibrium(transProb);
        }
    }

    public RandomVariable<Alignment> sample() {

        setup();

        GenerativeDistribution<Integer> rootDistribution = new DiscreteDistribution(rootFreqs, random);

        int length = 0;
        if (L != null) length = L.value();
        if (length == 0 && siteRates != null) length = siteRates.value().length;
        if (L != null && siteRates != null && L.value() != siteRates.value().length) {
            throw new RuntimeException(LParamName + " and " + siteRatesParamName + " have incompatible values!");
        }

        Alignment alignment = new Alignment(tree.value().n(), length, idMap);

        for (int i = 0; i < length; i++) {
            Value<Integer> rootState = rootDistribution.sample();
            traverseTree(tree.value().getRoot(), rootState, alignment, i, decomposition, transProb, (siteRates == null) ? 1.0 : siteRates.value()[i]);
        }

        return new RandomVariable<>("D", alignment, this);
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
        System.out.println("  Computed equilibrium (from e^{100*Q}) is " + Arrays.toString(freqs));

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
            }
        } else {
            for (TimeTreeNode child : node.getChildren()) {
                fillIdMap(child, idMap);
            }
        }
    }

    private void traverseTree(TimeTreeNode node, Value<Integer> nodeState, Alignment alignment, int pos, EigenDecomposition eigenDecomposition, double[][] transProb, double siteRate) {
        if (node.isLeaf()) {
            alignment.setState(node.getId(), pos, nodeState.value());
        } else {
            for (TimeTreeNode child : node.getChildren()) {
                double rate = clockRate.value();

                double branchLength = siteRate * rate * (node.getAge() - child.getAge());

                getTransitionProbabilities(branchLength, transProb);
                int state = drawState(transProb[nodeState.value()]);

                traverseTree(child, new IntegerValue("x", state), alignment, pos, eigenDecomposition, transProb, siteRate);
            }
        }
    }

    private int drawState(double[] p) {
        double U = random.nextDouble();
        double totalP = 0.0;
        for (int i = 0; i < p.length; i++) {
            totalP += p[i];
            if (U <= totalP) return i;
        }
        throw new RuntimeException("p vector doesn't add to 1.0!");
    }

    private void getTransitionProbabilities(double branchLength, double[][] transProbs) {

        int i, j, k;
        double temp;

        int numStates = transProbs.length;

        double[][] iexp = new double[numStates][numStates];

        // inverse Eigen vectors
        // Eigen values
        double[] Eval = decomposition.getRealEigenvalues();
        for (i = 0; i < numStates; i++) {
            temp = Math.exp(branchLength * Eval[i]);
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
