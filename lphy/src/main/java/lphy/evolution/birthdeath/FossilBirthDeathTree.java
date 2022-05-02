package lphy.evolution.birthdeath;

import lphy.core.distributions.Utils;
import lphy.evolution.Taxa;
import lphy.evolution.tree.PruneTree;
import lphy.evolution.tree.TaxaConditionedTreeGenerator;
import lphy.evolution.tree.TimeTree;
import lphy.graphicalModel.*;

import java.util.Map;

import static lphy.core.distributions.DistributionConstants.nParamName;
import static lphy.evolution.birthdeath.BirthDeathConstants.*;

/**
 * A Birth-death tree generative distribution
 */
public class FossilBirthDeathTree extends TaxaConditionedTreeGenerator {

    private Value<Number> birthRate;
    private Value<Number> deathRate;
    private Value<Number> psiVal;
    private Value<Number> rhoVal;

    public FossilBirthDeathTree(@ParameterInfo(name = lambdaParamName, description = "per-lineage birth rate.") Value<Number> birthRate,
                                @ParameterInfo(name = muParamName, description = "per-lineage death rate.") Value<Number> deathRate,
                                @ParameterInfo(name = rhoParamName, description = "proportion of extant taxa sampled.") Value<Number> rhoVal,
                                @ParameterInfo(name = psiParamName, description = "per-lineage sampling-through-time rate.") Value<Number> psiVal,
                                @ParameterInfo(name = nParamName, description = "the number of taxa. optional.", optional = true) Value<Integer> n,
                                @ParameterInfo(name = taxaParamName, description = "Taxa object", optional = true) Value<Taxa> taxa) {

        super(n, taxa, null);

        this.birthRate = birthRate;
        this.deathRate = deathRate;
        this.rhoVal = rhoVal;
        this.psiVal = psiVal;
        this.random = Utils.getRandom();

        checkTaxaParameters(false);
    }

    @GeneratorInfo(name = "FossilBirthDeathTree",
            category = GeneratorCategory.BIRTH_DEATH_TREE, examples = {"simFossilsCompact.lphy"},
            description = "A tree of extant species and those sampled through time, which is conceptually embedded in a full species tree produced by a speciation-extinction (birth-death) branching process.<br>" +
            "Conditioned on root age and (optionally) on number of *extant* taxa.")
    public RandomVariable<TimeTree> sample() {

        SimBDReverse simBDReverse = new SimBDReverse(birthRate, deathRate, taxaValue, rhoVal);
        Value<TimeTree> bdReverseTree = simBDReverse.sample();

        SimFossilsPoisson simFossilsPoisson = new SimFossilsPoisson(bdReverseTree, psiVal);

        Value<TimeTree> fullTreeWithFossils = simFossilsPoisson.sample();

        PruneTree pruneTree = new PruneTree(fullTreeWithFossils);

        TimeTree tree = pruneTree.apply().value();

        return new RandomVariable<>(null, tree, this);
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
        map.put(rhoParamName, rhoVal);
        map.put(psiParamName, psiVal);
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
            case rhoParamName:
                rhoVal = value;
                break;
            case psiParamName:
                psiVal = value;
                break;
            default:
                super.setParam(paramName, value);
                break;
        }
    }

    public Value<Number> getBirthRate() {
        return birthRate;
    }

    public Value<Number> getDeathRate() {
        return deathRate;
    }

    public Value<Number> getRho() {
        return rhoVal;
    }

    public Value<Number> getPsi() {
        return psiVal;
    }
}