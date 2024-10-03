package lphy.base.distribution;

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

import static lphy.base.distribution.DistributionConstants.nParamName;

/**
 * Uncorrelated Lognormal Rates Model
 */
@Citation( value="Drummond et al. Relaxed Phylogenetics and Dating with Confidence, PLoS biology, 4(5), 2006, e88.",
        title = "Relaxed Phylogenetics and Dating with Confidence", year = 2006,
        authors = {"Drummond, A.J., Ho, S.Y.W., Phillips, M.J. and Rambaut, A."},
        DOI="https://doi.org/10.1371/journal.pbio.0040088")
public class UCLN extends ParametricDistribution<Double[]> {

    public final static String UCLN_MEAN = "uclnMean";
    public final static String UCLN_SIGMA = "uclnSigma";
    private Value<Number> uclnMean;
    private Value<Number> uclnSigma;
    private Value<Integer> nBranches;

    public UCLN(@ParameterInfo(name = UCLN_MEAN, narrativeName = "the mean (in real space) of the expected lognormal distribution on branch rates.",
            description = "The mean (in real space) of the lognormal distribution.") Value<Number> uclnMean,
                @ParameterInfo(name = UCLN_SIGMA, narrativeName = "the standard deviation of the expected lognormal distribution on branch rates",
            description = "The standard deviation of the lognormal distribution.") Value<Number> uclnSigma,
                @ParameterInfo(name = nParamName, narrativeName = "number of branches",
            description = "The number of branches.") Value<Integer> nBranches) {
        super();
        this.uclnMean = uclnMean;
        this.uclnSigma = uclnSigma;
        this.nBranches = nBranches;

        constructDistribution(random);
    }

    @Override
    protected void constructDistribution(RandomGenerator random) { }

    @GeneratorInfo(name = "UCLN", verbClause = "are sampled from",
            narrativeName = "uncorrelated lognormal (UCLN) relaxed clock model",
            category = GeneratorCategory.PRIOR,
            description = "The uncorrelated lognormal (UCLN) relaxed clock model")
    public RandomVariable<Double[]> sample() {

        Double[] rates = new Double[nBranches.value()];

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
        double var = sd * sd;
        // ucln_mu = ln(ucln_mean) - (ucln_var * 0.5)
        double mu = Math.log(uclnMean.value().doubleValue()) - var * 0.5;

        return new LogNormalDistribution(random, mu, sd);
    }

    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(UCLN_MEAN, uclnMean);
            put(UCLN_SIGMA, uclnSigma);
            put(nParamName, nBranches);
        }};
    }

    @Override
    public void setParam(String paramName, Value value) {
        switch (paramName) {
            case UCLN_MEAN -> uclnMean = value;
            case UCLN_SIGMA -> uclnSigma = value;
            case nParamName -> nBranches = value;
            default -> throw new RuntimeException("Unrecognised parameter name: " + paramName);
        }

        super.setParam(paramName, value); // constructDistribution
    }
}