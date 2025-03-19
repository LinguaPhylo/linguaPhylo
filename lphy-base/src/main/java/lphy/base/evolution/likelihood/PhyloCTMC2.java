package lphy.base.evolution.likelihood;

import jebl.evolution.sequences.SequenceType;
import lphy.base.distribution.Categorical;
import lphy.base.distribution.Poisson;
import lphy.base.evolution.alignment.Alignment;
import lphy.base.evolution.alignment.SimpleAlignment;
import lphy.base.evolution.tree.TimeTree;
import lphy.base.evolution.tree.TimeTreeNode;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.ValueUtils;
import lphy.core.model.annotation.Citation;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author Walter Xie
 */
@Citation(
        value="Felsenstein, J. (1981). Evolutionary trees from DNA sequences: a maximum likelihood approach. Journal of molecular evolution, 17(6), 368-376.",
        title = "Evolutionary trees from DNA sequences: a maximum likelihood approach",
        year = 1981,
        authors = {"Felsenstein"},
        DOI="https://doi.org/10.1007/BF01734359")
public class PhyloCTMC2 extends AbstractPhyloCTMC {
    Value<Double[][]> Q; // to keep the input Value<Double[][]>
    Value<Double[]> siteRates;
    Value<SimpleAlignment> rootSeq;

    public static final String QParamName = "Q";
    public static final String siteRatesParamName = "siteRates";


    public PhyloCTMC2(@ParameterInfo(name = AbstractPhyloCTMC.treeParamName, verb = "on", narrativeName = "phylogenetic time tree", description = "the time tree.") Value<TimeTree> tree,
                      @ParameterInfo(name = AbstractPhyloCTMC.muParamName, narrativeName = "molecular clock rate", description = "the clock rate. Default value is 1.0.", optional = true) Value<Number> mu,
                      @ParameterInfo(name = AbstractPhyloCTMC.rootFreqParamName, verb = "are", narrativeName = "root frequencies", description = "the root probabilities. Optional parameter. If not specified then first row of e^{100*Q) is used.", optional = true) Value<Double[]> rootFreq,
                      @ParameterInfo(name = QParamName, narrativeName= "instantaneous rate matrix", description = "the instantaneous rate matrix.") Value<Double[][]> Q,
                      @ParameterInfo(name = siteRatesParamName, description = "a rate for each site in the alignment. Site rates are assumed to be 1.0 otherwise.",  optional = true) Value<Double[]> siteRates,
                      @ParameterInfo(name = AbstractPhyloCTMC.branchRatesParamName, description = "a rate for each branch in the tree. Original branch rates are used if rates not given. Branch rates are assumed to be 1.0 otherwise.", optional = true) Value<Double[]> branchRates,
                      @ParameterInfo(name = AbstractPhyloCTMC.LParamName, narrativeName= "alignment length",
                             description = "length of the alignment", optional = true) Value<Integer> L,
                      @ParameterInfo(name = AbstractPhyloCTMC.dataTypeParamName, description = "the data type used for simulations, default to nucleotide",
                             narrativeName = "data type used for simulations", optional = true) Value<SequenceType> dataType,
                      @ParameterInfo(name = AbstractPhyloCTMC.rootSeqParamName, narrativeName="root sequence", description = "root sequence, defaults to root sequence generated from equilibrium frequencies.", optional = true) Value<SimpleAlignment> rootSeq) {

        super(tree, mu, rootFreq, branchRates, L, dataType);
        this.Q = Q;
        this.siteRates = siteRates;

        if (rootSeq != null) {
            this.rootSeq = rootSeq;
        }

        checkCompatibilities();
    }

    @Override
    protected void checkCompatibilities() {
        // check L and siteRates compatibility
        if (L != null && siteRates != null && L.value() != siteRates.value().length) {
            throw new RuntimeException(AbstractPhyloCTMC.LParamName + " and " + siteRatesParamName + " have incompatible values!");
        }
        // check root sequence and alignment compatibility
        if (this.rootSeq != null) {
            int rootSeqLength = rootSeq.value().nchar();
            int alignmentLength = L.value();
            if (rootSeq != null && rootSeqLength != alignmentLength) {
                throw new RuntimeException("Length of root sequence " + AbstractPhyloCTMC.rootSeqParamName + " = " + rootSeqLength +
                        " is not equal to alignment length " + AbstractPhyloCTMC.LParamName + " = " + alignmentLength);
            }
        }
    }

    @Override
    protected int getSiteCount() {
        if (L != null) return L.value();
        if (siteRates != null) return siteRates.value().length;
        throw new RuntimeException("One of " + AbstractPhyloCTMC.LParamName + " or " + siteRatesParamName + " must be specified.");
    }

    @Override
    protected Double[][] getQ() {
        return Objects.requireNonNull(Q).value();
    }

    @Override
    public SortedMap<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        map.put(AbstractPhyloCTMC.treeParamName, tree);
        if (clockRate != null) map.put(AbstractPhyloCTMC.muParamName, clockRate);
        if (freq != null) map.put(AbstractPhyloCTMC.rootFreqParamName, freq);
        map.put(QParamName, Q);
        if (siteRates != null) map.put(siteRatesParamName, siteRates);
        if (branchRates != null) map.put(AbstractPhyloCTMC.branchRatesParamName, branchRates);
        if (L != null) map.put(AbstractPhyloCTMC.LParamName, L);
        if (dataType != null) map.put(AbstractPhyloCTMC.dataTypeParamName, dataType);
        if (rootSeq != null) map.put(AbstractPhyloCTMC.rootSeqParamName, rootSeq);
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(AbstractPhyloCTMC.treeParamName)) tree = value;
        else if (paramName.equals(AbstractPhyloCTMC.muParamName)) clockRate = value;
        else if (paramName.equals(AbstractPhyloCTMC.rootFreqParamName)) freq = value;
        else if (paramName.equals(QParamName)) Q = value;
        else if (paramName.equals(siteRatesParamName)) siteRates = value;
        else if (paramName.equals(AbstractPhyloCTMC.branchRatesParamName)) branchRates = value;
        else if (paramName.equals(AbstractPhyloCTMC.LParamName)) L = value;
            //        else if (paramName.equals(stateNamesParamName)) stateNames = value;
        else if (paramName.equals(AbstractPhyloCTMC.dataTypeParamName)) dataType = value;
        else if (paramName.equals(AbstractPhyloCTMC.rootSeqParamName)) rootSeq = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);
    }

    // use default setup()

    @GeneratorInfo(name = "PhyloCTMC2", verbClause = "is assumed to have evolved under",
            narrativeName = "phylogenetic continuous time Markov process",
            category = GeneratorCategory.PHYLO_LIKELIHOOD,
            description = "The phylogenetic continuous-time Markov chain distribution. A sequence is simulated for every leaf node, and every direct ancestor node with an id." +
                    "(The sampling distribution that the phylogenetic likelihood is derived from.)")
    public RandomVariable<Alignment> sample() {
        setup();

        // default to nuc
        SequenceType dt = SequenceType.NUCLEOTIDE;

        if (dataType != null) dt = dataType.value();

        int length = getSiteCount();

        SimpleAlignment a = new SimpleAlignment(idMap, length, dt);

        double mu = (this.clockRate == null) ? 1.0 : ValueUtils.doubleValue(clockRate);

        TimeTree timeTree = tree.value();
        double tl = timeTree.treeLength();

        int expectedChanges = (int) (mu * tl * length);
        // estimate num of mutations
        Poisson poisson = new Poisson(new Value<>(null, expectedChanges));
        int nMut = poisson.sample().value();

        // Random (0, TL)
        double thr = random.nextDouble() * tl;



        for (int i = 0; i < length; i++) {
            if (rootSeq != null) {
                // use simulated or user specified root sequence
                int rootState = rootSeq.value().getState(0, i); // root taxon is 0



//                traverseTree(tree.value().getRoot(), rootState, a, i, transProb, mu,
//                        (siteRates == null) ? 1.0 : siteRates.value()[i]);
            } else {
                int rootState = Categorical.sample(rootFreqs.value(), random);


//                traverseTree(tree.value().getRoot(), rootState, a, i, transProb, mu,
//                        (siteRates == null) ? 1.0 : siteRates.value()[i]);
            }

        }

        return new RandomVariable<>("D", a, this);

    }

    protected void traverseTree(TimeTreeNode node, int nodeState, double tlSum, double blThreshold,
                                SimpleAlignment alignment, int pos,
                                double[][] transProb, double clockRate, double siteRate) {

        if (tlSum > blThreshold) {
            // set state from here to all children

// TODO           alignment.setState(node.getLeafIndex(), pos, nodeState); // no ambiguous state
        }
        // start from root
        List<TimeTreeNode> children = node.getChildren();
        for (TimeTreeNode child : children) {
            double branchLength = siteRate * clockRate * (node.getAge() - child.getAge());

            if (branchRates != null) {
                branchLength *= branchRates.value()[child.getIndex()];
            }

            getTransitionProbabilities(branchLength, transProb);
            // draw state from Q
            int state = drawState(transProb[nodeState]);

            tlSum += branchLength;

            traverseTree(child, state, tlSum, blThreshold, alignment, pos,
                    transProb, clockRate, siteRate);
        }
    }

    // TODO
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

    public Value<Double[]> getSiteRates() {
        return siteRates;
    }

    public Value<Double[][]> getQValue() {
        return Q;
    }

}
