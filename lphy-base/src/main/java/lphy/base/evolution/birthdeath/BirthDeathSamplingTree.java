package lphy.base.evolution.birthdeath;

import lphy.base.evolution.tree.TimeTree;
import lphy.core.model.GenerativeDistribution;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.annotation.Citation;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.simulator.RandomUtils;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.TreeMap;

import static lphy.base.evolution.birthdeath.BirthDeathConstants.*;

/**
 * A Birth-death tree generative distribution
 */
@Citation(value="Tanja Stadler, Roger Kouyos, ..., Sebastian Bonhoeffer, " +
        "Estimating the Basic Reproductive Number from Viral Sequence Data, " +
        "Molecular Biology and Evolution, Volume 29, Issue 1, January 2012.",
        title = "Estimating the Basic Reproductive Number from Viral Sequence Data",
        DOI="https://doi.org/10.1093/molbev/msr217",
        authors = {"Stadler", "Kouyos", "...", "Bonhoeffer"}, year=2012)
public class BirthDeathSamplingTree implements GenerativeDistribution<TimeTree> {

    private Value<Number> birthRate;
    private Value<Number> deathRate;
    private Value<Number> rho;
    private Value<Number> rootAge;

    RandomGenerator random;

    public BirthDeathSamplingTree(@ParameterInfo(name = lambdaParamName, description = "per-lineage birth rate.") Value<Number> birthRate,
                                  @ParameterInfo(name = muParamName, description = "per-lineage death rate.") Value<Number> deathRate,
                                  @ParameterInfo(name = rhoParamName, description = "the sampling proportion.") Value<Number> rho,
                                  @ParameterInfo(name = rootAgeParamName, description = "the age of the root of the tree.") Value<Number> rootAge) {

        this.birthRate = birthRate;
        this.deathRate = deathRate;
        this.rho = rho;
        this.rootAge = rootAge;
        this.random = RandomUtils.getRandom();
    }


    @GeneratorInfo(name = "BirthDeathSampling", verbClause = "is assumed to have evolved according to",
            narrativeName = "birth-death-sampling tree process",
            category = GeneratorCategory.BD_TREE, examples = {"simpleBirthDeath.lphy"},
            description = "The Birth-death-sampling tree distribution over tip-labelled time trees.<br>" +
            "Conditioned on root age.")
    public RandomVariable<TimeTree> sample() {

        FullBirthDeathTree birthDeathTree = new FullBirthDeathTree(birthRate, deathRate, rootAge, null);
        RandomVariable<TimeTree> fullTree = birthDeathTree.sample();

        RhoSampleTree rhoSampleTree = new RhoSampleTree(fullTree, rho);
        RandomVariable<TimeTree> rtree = rhoSampleTree.sample();
        // The random variable must be re-wrapped to ensure correct behaviour downstream.
        return new RandomVariable<>(rtree.getId(), rtree.value(), this);
    }

    @Override
    public double logDensity(TimeTree timeTree) {

        throw new UnsupportedOperationException("Not implemented!");
    }

    @Override
    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(lambdaParamName, birthRate);
            put(muParamName, deathRate);
            put(rhoParamName, rho);
            put(rootAgeParamName, rootAge);
        }};
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
            case rhoParamName:
                rho = value;
                break;
            case rootAgeParamName:
                rootAge = value;
                break;
            default:
                throw new RuntimeException("Unrecognised parameter name: " + paramName);
        }
    }

    public String toString() {
        return getName();
    }
}
