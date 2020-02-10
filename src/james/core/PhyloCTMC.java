package james.core;

import james.TimeTree;
import james.TimeTreeNode;
import james.core.distributions.Utils;
import james.graphicalModel.*;
import james.graphicalModel.types.IntegerValue;
import org.apache.commons.math3.linear.*;

import java.util.*;

/**
 * Created by adru001 on 2/02/20.
 */
public class PhyloCTMC implements GenerativeDistribution<Alignment> {

    Value<TimeTree> tree;
    Value<Double> clockRate;
    Value<RealMatrix> Q;
    Value<Integer> L;
    Random random;

    String treeParamName;
    String muParamName;
    String QParamName;
    String LParamName;

    int numStates;

    public PhyloCTMC(@ParameterInfo(name = "tree", description = "the time tree.") Value<TimeTree> tree,
                     @ParameterInfo(name = "mu", description = "the clock rate.") Value<Double> mu,
                     @ParameterInfo(name = "Q", description = "the instantaneous rate matrix.") Value<RealMatrix> Q,
                     @ParameterInfo(name = "L", description = "the length of the alignment to generate.") Value<Integer> L) {
        this.tree = tree;
        this.Q = Q;
        this.clockRate = mu;
        this.L = L;
        numStates = Q.value().getColumnDimension();
        this.random = Utils.getRandom();

        treeParamName = getParamName(0);
        muParamName = getParamName(1);
        QParamName = getParamName(2);
        LParamName = getParamName(3);
    }

    @Override
    public SortedMap<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        map.put(treeParamName, tree);
        map.put(muParamName, clockRate);
        map.put(QParamName, Q);
        map.put(LParamName, L);
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(treeParamName)) tree = value;
        else if (paramName.equals(muParamName)) clockRate = value;
        else if (paramName.equals(QParamName)) Q = value;
        else if (paramName.equals(LParamName)) L = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);
    }

    public RandomVariable<Alignment> sample() {

        SortedMap<String, Integer> idMap = new TreeMap<>();
        fillIdMap(tree.value().getRoot(), idMap);


        //TODO need to use explicit root distribution
        GenerativeDistribution<Integer> rootDistribution =
                new DiscreteDistribution(new Value<>("rootProb", new double[]{0.25, 0.25, 0.25, 0.25}), random);

        Alignment alignment = new Alignment(tree.value().n(), L.value(), idMap);

        double[][] transProb = new double[numStates][numStates];
        EigenDecomposition decomposition = new EigenDecomposition(Q.value());

        for (int i = 0; i < L.value(); i++) {
            Value<Integer> rootState = rootDistribution.sample();
            traverseTree(tree.value().getRoot(), rootState, alignment, i, decomposition, transProb);
        }

        return new RandomVariable<>("D", alignment, this);
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

    private void traverseTree(TimeTreeNode node, Value<Integer> nodeState, Alignment alignment, int pos, EigenDecomposition eigenDecomposition, double[][] transProb) {
        if (node.isLeaf()) {
            alignment.setState(node.getId(), pos, nodeState.value());
        } else {
            for (TimeTreeNode child : node.getChildren()) {
                double rate = clockRate.value();

                double branchLength = rate * (node.getAge() - child.getAge());

                getTransitionProbabilities(branchLength, eigenDecomposition, transProb);
                int state = drawState(transProb[nodeState.value()]);

                traverseTree(child, new IntegerValue("x", state), alignment, pos, eigenDecomposition, transProb);
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

    public static void getTransitionProbabilities(double branchLength, EigenDecomposition eigenDecomposition, double[][] transProbs) {

        int i, j, k;
        double temp;

        int numStates = transProbs.length;

        // Eigen vectors

        double[][] Evec = new double[numStates][numStates];
        for (i = 0; i < numStates; i++) {
            RealVector evec = eigenDecomposition.getEigenvector(i);
            for (j = 0; j < numStates; j++) {
                Evec[j][i] = evec.getEntry(j);
            }
        }
        double[][] Ievc = new double[numStates][numStates];

        luinverse(Evec, Ievc, numStates);

        double[][] iexp = new double[numStates][numStates];

        // inverse Eigen vectors
        // Eigen values
        double[] Eval = eigenDecomposition.getRealEigenvalues();
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

    public static double EPSILON = 2.220446049250313E-16;

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

    public static void main(String[] args) {

        double third = 1.0 / 3.0;

        double[][] Qarray = {
                {-1, third, third, third},
                {third, -1, third, third},
                {third, third, -1, third},
                {third, third, third, -1}
        };

        RealMatrix Qmatrix = new Array2DRowRealMatrix(Qarray);

        EigenDecomposition eigenDecomposition = new EigenDecomposition(Qmatrix);

        double[][] transProbs = new double[4][4];

        double[] branchLengths = {0, 0.1, 10};

        for (double dist : branchLengths) {

            getTransitionProbabilities(dist, eigenDecomposition, transProbs);
            System.out.println("branch length = " + dist + ":");
            for (int i = 0; i < transProbs.length; i++) {
                System.out.println(Arrays.toString(transProbs[i]));
            }
        }
    }
}
