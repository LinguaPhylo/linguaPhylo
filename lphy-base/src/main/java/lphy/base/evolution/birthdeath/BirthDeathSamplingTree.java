package lphy.base.evolution.birthdeath;

import lphy.base.evolution.tree.AgeConditionedTreeGenerator;
import lphy.base.evolution.tree.TimeTree;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.annotation.Citation;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

import java.util.Map;

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
public class BirthDeathSamplingTree extends AgeConditionedTreeGenerator {

    private Value<Number> birthRate;
    private Value<Number> deathRate;
    private Value<Number> rho;

    public BirthDeathSamplingTree(@ParameterInfo(name = lambdaParamName, description = "per-lineage birth rate.") Value<Number> birthRate,
                                  @ParameterInfo(name = muParamName, description = "per-lineage death rate.") Value<Number> deathRate,
                                  @ParameterInfo(name = rhoParamName, description = "the sampling proportion.") Value<Number> rho,
                                  @ParameterInfo(name = rootAgeParamName, description = "the age of the root of the tree.") Value<Number> rootAge) {

        super(rootAge, null);
        this.birthRate = birthRate;
        this.deathRate = deathRate;
        this.rho = rho;
    }


    @GeneratorInfo(name = "BirthDeath", aliases = {"BirthDeathSampling"}, verbClause = "is assumed to have evolved according to",
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
        Map<String, Value> map = super.getParams(); // rootAge
        map.put(lambdaParamName, birthRate);
        map.put(muParamName, deathRate);
        map.put(rhoParamName, rho);
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (setAgeParam(paramName, value)) return; // rootAge
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
            default:
                throw new RuntimeException("Unrecognised parameter name: " + paramName);
        }
    }
}
