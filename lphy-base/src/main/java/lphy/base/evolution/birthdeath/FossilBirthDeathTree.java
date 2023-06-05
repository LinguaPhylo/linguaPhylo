package lphy.base.evolution.birthdeath;

import lphy.base.distribution.DistributionConstants;
import lphy.base.evolution.Taxa;
import lphy.base.evolution.tree.TaxaConditionedTreeGenerator;
import lphy.base.evolution.tree.TimeTree;
import lphy.base.function.tree.PruneTree;
import lphy.core.model.annotation.Citation;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.model.component.GeneratorCategory;
import lphy.core.model.component.RandomVariable;
import lphy.core.model.component.Value;

import java.util.Map;

import static lphy.base.evolution.birthdeath.BirthDeathConstants.*;

/**
 * The fossilized birth–death process.
 */
@Citation(value="Tracy A. Heath, John P. Huelsenbeck, and Tanja Stadler, " +
        "The fossilized birth–death process for coherent calibration of divergence-time estimates, " +
        "Proceedings of the National Academy of Sciences, 111 (29), 2014.",
        title = "The fossilized birth–death process for coherent calibration of divergence-time estimates",
        DOI="https://doi.org/10.1073/pnas.1319091111",
        authors = {"Heath","Huelsenbeck","Stadler"}, year=2014)
public class FossilBirthDeathTree extends TaxaConditionedTreeGenerator {

    private Value<Number> birthRate;
    private Value<Number> deathRate;
    private Value<Number> psiVal;
    private Value<Number> rhoVal;

    public FossilBirthDeathTree(@ParameterInfo(name = lambdaParamName, description = "per-lineage birth rate.") Value<Number> birthRate,
                                @ParameterInfo(name = muParamName, description = "per-lineage death rate.") Value<Number> deathRate,
                                @ParameterInfo(name = rhoParamName, description = "proportion of extant taxa sampled.") Value<Number> rhoVal,
                                @ParameterInfo(name = psiParamName, description = "per-lineage sampling-through-time rate.") Value<Number> psiVal,
                                @ParameterInfo(name = DistributionConstants.nParamName, description = "the number of taxa. optional.", optional = true) Value<Integer> n,
                                @ParameterInfo(name = TaxaConditionedTreeGenerator.taxaParamName, description = "Taxa object", optional = true) Value<Taxa> taxa) {

        super(n, taxa, null);

        this.birthRate = birthRate;
        this.deathRate = deathRate;
        this.rhoVal = rhoVal;
        this.psiVal = psiVal;

        checkTaxaParameters(false);
    }

    @GeneratorInfo(name = "FossilBirthDeathTree",
            category = GeneratorCategory.BD_TREE, examples = {"simFossilsCompact.lphy"},
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