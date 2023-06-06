package lphy.base.evolution.birthdeath;

import lphy.base.distribution.DistributionConstants;
import lphy.base.evolution.tree.TaxaConditionedTreeGenerator;
import lphy.base.evolution.tree.TimeTree;
import lphy.base.evolution.tree.TimeTreeNode;
import lphy.core.model.GeneratorCategory;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.ValueUtils;
import lphy.core.model.annotation.Citation;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.parser.argument.ParameterInfo;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static lphy.base.evolution.birthdeath.BirthDeathConstants.*;

/**
 * A Birth-death tree generative distribution
 */
@Citation(value="Joseph Heled, Alexei J. Drummond, Calibrated Birth–Death Phylogenetic Time-Tree Priors for Bayesian Inference, " +
        "Systematic Biology, Volume 64, Issue 3, May 2015.",
        title = "Calibrated Birth–Death Phylogenetic Time-Tree Priors for Bayesian Inference",
        DOI="https://doi.org/10.1093/sysbio/syu089",
        authors = {"Heled", "Drummond"}, year=2015)
public class BirthDeathTree extends TaxaConditionedTreeGenerator {

    private Value<Number> birthRate;
    private Value<Number> deathRate;
    private Value<Number> rootAge;

    public BirthDeathTree(@ParameterInfo(name = lambdaParamName, description = "per-lineage birth rate.") Value<Number> birthRate,
                          @ParameterInfo(name = muParamName, description = "per-lineage death rate.") Value<Number> deathRate,
                          @ParameterInfo(name = DistributionConstants.nParamName, description = "the number of taxa. optional.", optional = true) Value<Integer> n,
                          @ParameterInfo(name = TaxaConditionedTreeGenerator.taxaParamName, description = "a string array of taxa id or a taxa object (e.g. dataframe, alignment or tree), optional.", optional = true) Value taxa,
                          @ParameterInfo(name = rootAgeParamName, description = "the age of the root.") Value<Number> rootAge) {

        super(n, taxa, null);

        this.birthRate = birthRate;
        this.deathRate = deathRate;
        this.rootAge = rootAge;

        checkTaxaParameters(true);
    }

    @GeneratorInfo(name = "BirthDeath",
            category = GeneratorCategory.BD_TREE, examples = {"simpleCalibratedBirthDeath.lphy","simpleExtantBirthDeath.lphy"},
            description = "A tree of only extant species, which is conceptually embedded<br>" +
                    "in a full species tree produced by a speciation-extinction (birth-death) branching process.<br>" +
            "Conditioned on root age and on number of taxa.")
    public RandomVariable<TimeTree> sample() {

        TimeTree tree = new TimeTree(getTaxa());
        List<TimeTreeNode> activeNodes = createLeafTaxa(tree);

        double lambda = ValueUtils.doubleValue(birthRate);
        double mu = ValueUtils.doubleValue(deathRate);

        double t = ValueUtils.doubleValue(rootAge);

        double[] times = new double[activeNodes.size() - 1];

        for (int i = 0; i < times.length - 1; i++) {
            times[i] = birthDeathInternalQ(random.nextDouble(), lambda, mu, t);
        }
        times[times.length - 1] = t;
        Arrays.sort(times);

        for (int i = 0; i < times.length; i++) {
            TimeTreeNode a = drawRandomNode(activeNodes);
            TimeTreeNode b = drawRandomNode(activeNodes);
            TimeTreeNode parent = new TimeTreeNode(times[i], new TimeTreeNode[]{a, b});
            activeNodes.add(parent);
        }

        tree.setRoot(activeNodes.get(0));

        return new RandomVariable<>("\u03C8", tree, this);
    }

    private double birthDeathInternalQ(double p, double lambda, double mu, double rootHeight) {

        if (lambda == mu) return p * rootHeight / (1 + lambda * rootHeight * (1 - p));
        double h = lambda - mu;
        double e1 = Math.exp(-h * rootHeight);
        double a1 = lambda - mu * e1;
        double a2 = p * (1 - e1);
        return (1 / h) * Math.log((a1 - mu * a2) / (a1 - lambda * a2));
    }

    @Override
    public double logDensity(TimeTree timeTree) {
        throw new UnsupportedOperationException("Not implemented!");
    }

    @Override
    public Map<String, Value> getParams() {
        Map<String, Value> map = super.getParams();
        map.put(lambdaParamName, birthRate);
        map.put(muParamName, deathRate);
        map.put(rootAgeParamName, rootAge);
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        switch (paramName) {
            case lambdaParamName:
                birthRate = value;
                break;
            case muParamName:
                deathRate = value;
                break;
            case rootAgeParamName:
                rootAge = value;
                break;
            default:
                super.setParam(paramName, value);
                break;
        }
    }
}
