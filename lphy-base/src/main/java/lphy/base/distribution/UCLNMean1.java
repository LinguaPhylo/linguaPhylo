package lphy.base.distribution;

import lphy.base.evolution.tree.TimeTree;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.annotation.Citation;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import org.apache.commons.math3.distribution.LogNormalDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.TreeMap;

/**
 * Uncorrelated relaxed clock model Lognormal,
 * implemented by <url>https://github.com/jordandouglas/ORC</url>
 */
@Citation( value="Douglas, J., Zhang, R., & Bouckaert, R. (2021). Adaptive dating and fast proposals: Revisiting the phylogenetic relaxed clock model. PLoS computational biology, 17(2), e1008322.",
        title = "Adaptive dating and fast proposals: Revisiting the phylogenetic relaxed clock model", year = 2021,
        authors = {"Douglas et al."},
        DOI="https://doi.org/10.1371/journal.pcbi.1008322")
public class UCLNMean1 extends ParametricDistribution<Double[]> {

//    public final static String UCLN_MEAN = "uclnMean";
    public final static String UCLN_SIGMA = "uclnSigma";
    public static final String TREE = "tree";
//    private Value<Number> uclnMean;
    private Value<Number> uclnSigma;
    private Value<TimeTree> tree;

    public UCLNMean1(
//            @ParameterInfo(name = UCLN_MEAN, narrativeName = "the mean (real space) of the lognormal distribution",
//            description = "The mean (real space) of the expected lognormal distribution on branch rates.") Value<Number> uclnMean,
                @ParameterInfo(name = UCLN_SIGMA, narrativeName = "the standard deviation",
            description = "The standard deviation of the expected lognormal distribution on branch rates.") Value<Number> uclnSigma,
                @ParameterInfo(name = TREE, narrativeName = "the tree",
            description = "The tree.") Value<TimeTree> tree) {
        super();
//        this.uclnMean = uclnMean;
        this.uclnSigma = uclnSigma;
        this.tree = tree;

        constructDistribution(random);
    }

    @Override
    protected void constructDistribution(RandomGenerator random) { }

    @GeneratorInfo(name = "UCLN_Mean1", verbClause = "are sampled from",
            narrativeName = "uncorrelated lognormal (UCLN) relaxed clock model",
            category = GeneratorCategory.PRIOR,
            description = "The uncorrelated lognormal (UCLN) relaxed clock model, " +
                    "where the mean of log-normal distr on branch rates in real space must be fixed to 1. " +
                    "Use the clock rate (mu) in PhyloCTMC as the expected mean clock rate.")
    public RandomVariable<Double[]> sample() {
        // nBranches = 2 * nTaxa - 2
        int nBranches = 2 * tree.value().leafCount() - 2;
        Double[] rates = new Double[nBranches];

        LogNormalDistribution lND = createLogNormalDistribution();

        // Each branch-rate variable is drawn from the same lognormal distribution .
        for (int i = 0; i < rates.length; i++) {
            rates[i] = lND.sample();
        }
        return new RandomVariable<>("x", rates, this);
    }

    public double logDensity(Double[] x) {
        LogNormalDistribution lND = createLogNormalDistribution();

        double logDensity = 0;
        for (Double aDouble : x) {
            logDensity += lND.logDensity(aDouble);
        }
        return logDensity;
    }

    private LogNormalDistribution createLogNormalDistribution() {
        Double sd = uclnSigma.value().doubleValue();
//        double var = sd * sd;
        // the prior density of a branch rate is under a Log-normal(−0.5σ2, σ) distribution
        // (with its mean fixed at 1).
        // ucln_mu = ln(ucln_mean) - (ucln_sd * ucln_sd * 0.5)
//        double mu = Math.log(uclnMean.value().doubleValue()) - var * 0.5;

        return new LogNormalDistribution(random, - 0.5 * sd * sd, sd);
    }

    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
//            put(UCLN_MEAN, uclnMean);
            put(UCLN_SIGMA, uclnSigma);
            put(TREE, tree);
        }};
    }

    @Override
    public void setParam(String paramName, Value value) {
        switch (paramName) {
//            case UCLN_MEAN -> uclnMean = value;
            case UCLN_SIGMA -> uclnSigma = value;
            case TREE -> tree = value;
            default -> throw new RuntimeException("Unrecognised parameter name: " + paramName);
        }

        super.setParam(paramName, value); // constructDistribution
    }

//    public Value<Number> getUclnMean() {
//        return uclnMean;
//    }

    public Value<Number> getUclnSigma() {
        return uclnSigma;
    }

    public Value<TimeTree> getTree() {
        return tree;
    }
}