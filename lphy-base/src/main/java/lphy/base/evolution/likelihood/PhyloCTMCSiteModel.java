package lphy.base.evolution.likelihood;

import jebl.evolution.sequences.SequenceType;
import lphy.base.distribution.Categorical;
import lphy.base.evolution.alignment.Alignment;
import lphy.base.evolution.alignment.SimpleAlignment;
import lphy.base.evolution.sitemodel.SiteModel;
import lphy.base.evolution.tree.TimeTree;
import lphy.core.model.GeneratorCategory;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.ValueUtils;
import lphy.core.model.annotation.Citation;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.parser.argument.ParameterInfo;

import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by Alexei Drummond on 2/02/20.
 */
@Citation(
        value="Felsenstein, J. (1981). Evolutionary trees from DNA sequences: a maximum likelihood approach. Journal of molecular evolution, 17(6), 368-376.",
        title = "Evolutionary trees from DNA sequences: a maximum likelihood approach",
        year = 1981,
        authors = {"Felsenstein"},
        DOI="https://doi.org/10.1007/BF01734359")
public class PhyloCTMCSiteModel extends AbstractPhyloCTMC {

    Value<SiteModel> siteModel;
    public static final String siteModelParamName = "siteModel";

    int siteCount;
    double[] finalSiteRates;
    double propInvariable;

    public PhyloCTMCSiteModel(@ParameterInfo(name = AbstractPhyloCTMC.treeParamName, verb = "on", narrativeName = "phylogenetic time tree", description = "the time tree.") Value<TimeTree> tree,
                              @ParameterInfo(name = AbstractPhyloCTMC.muParamName, narrativeName = "molecular clock rate", description = "the clock rate. Default value is 1.0.", optional = true) Value<Number> mu,
                              @ParameterInfo(name = AbstractPhyloCTMC.rootFreqParamName, description = "the root probabilities. Optional parameter. If not specified then first row of e^{100*Q) is used.", optional = true) Value<Double[]> rootFreq,
                              @ParameterInfo(name = siteModelParamName, narrativeName = "site model", description = "the site model") Value<SiteModel> siteModel,
                              @ParameterInfo(name = AbstractPhyloCTMC.branchRatesParamName, description = "a rate for each branch in the tree. Branch rates are assumed to be 1.0 otherwise.", optional = true) Value<Double[]> branchRates,
                              @ParameterInfo(name = AbstractPhyloCTMC.LParamName, narrativeName="alignment length", description = "length of the alignment", optional = true) Value<Integer> L,
                              @ParameterInfo(name = AbstractPhyloCTMC.dataTypeParamName, narrativeName="the data type used for simulations", description = "the data type used for simulations, default to nucleotide", optional = true) Value<SequenceType> dataType) {

        super(tree, mu, rootFreq, branchRates, L, dataType);
        this.siteModel = siteModel;

        checkCompatibilities();
    }

    @Override
    protected void checkCompatibilities() {
        // check L and siteRates compatibility
        if (L != null && siteModel.value().hasSiteRates() && L.value() != siteModel.value().siteRates().length)
            throw new RuntimeException(AbstractPhyloCTMC.LParamName + " and " + siteModelParamName + " site rates have incompatible values!");
    }

    protected int getSiteCount() {
        if (L != null) return L.value();
        if (siteModel.value().hasSiteRates()) return siteModel.value().siteRates().length;
        throw new RuntimeException("One of " + AbstractPhyloCTMC.LParamName + " or " + siteModelParamName + " site rates must be specified.");
    }

    @Override
    protected Double[][] getQ() {
        return Objects.requireNonNull(siteModel.value()).getQ();
    }

    @Override
    public SortedMap<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        map.put(AbstractPhyloCTMC.treeParamName, tree);
        if (clockRate != null) map.put(AbstractPhyloCTMC.muParamName, clockRate);
        if (freq != null) map.put(AbstractPhyloCTMC.rootFreqParamName, freq);
        map.put(siteModelParamName, siteModel);
        if (branchRates != null) map.put(AbstractPhyloCTMC.branchRatesParamName, branchRates);
        if (L != null) map.put(AbstractPhyloCTMC.LParamName, L);
        if (dataType != null) map.put(AbstractPhyloCTMC.dataTypeParamName, dataType);
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        switch (paramName) {
            case AbstractPhyloCTMC.treeParamName:
                tree = value;
                break;
            case AbstractPhyloCTMC.rootFreqParamName:
                freq = value;
                break;
            case AbstractPhyloCTMC.muParamName:
                clockRate = value;
                break;
            case siteModelParamName:
                siteModel = value;
                break;
            case AbstractPhyloCTMC.branchRatesParamName:
                branchRates = value;
                break;
            case AbstractPhyloCTMC.LParamName:
                L = value;
                break;
            case AbstractPhyloCTMC.dataTypeParamName:
                dataType = value;
                break;
            default:
                throw new RuntimeException("Unrecognised parameter name: " + paramName);
        }
    }

    protected void setup() {

        siteCount = getSiteCount();

        super.setup();

        finalSiteRates = new double[siteCount];
        propInvariable = siteModel.value().getProportionInvariable();

    }

    @GeneratorInfo(name = "PhyloCTMC", verbClause = "is assumed to have evolved under",
            narrativeName = "phylogenetic continuous time Markov process",
            category = GeneratorCategory.PHYLO_LIKELIHOOD, examples = {"simpleBModelTest.lphy"},
            description = "The phylogenetic continuous-time Markov chain distribution. A sequence is simulated for every leaf node, and every direct ancestor node with an id." +
            "(The sampling distribution that the phylogenetic likelihood is derived from.)")
    public RandomVariable<Alignment> sample() {
        this.setup();

        // default to nuc
        SequenceType dt = SequenceType.NUCLEOTIDE;

        if (dataType != null) dt = dataType.value();

        for (int i = 0; i < finalSiteRates.length; i++) {
            if (propInvariable > 0 && random.nextDouble()<propInvariable) {
                finalSiteRates[i] = 0;
            } else if (siteModel.value().hasSiteRates()) {
                finalSiteRates[i] = siteModel.value().siteRates()[i];
            } else {
                finalSiteRates[i] = 1.0;
            }
        }

        SimpleAlignment a = new SimpleAlignment(idMap, siteCount, dt);

        double mu = (this.clockRate == null) ? 1.0 : ValueUtils.doubleValue(clockRate);

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

}
