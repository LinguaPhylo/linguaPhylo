package lphy.base.evolution.birthdeath;

import lphy.base.distribution.DistributionConstants;
import lphy.base.evolution.Taxa;
import lphy.base.evolution.tree.TimeTree;
import lphy.core.model.GenerativeDistribution;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.ValueUtils;
import lphy.core.model.annotation.Citation;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

import java.util.Map;
import java.util.TreeMap;

import static lphy.base.evolution.birthdeath.BirthDeathConstants.*;
import static lphy.base.evolution.tree.TaxaConditionedTreeGenerator.taxaParamName;

/**
 * The fossilized birth–death process, parameterised by diversification rate,
 * turnover and sampling proportion.
 */
@Citation(value="Tracy A. Heath, John P. Huelsenbeck, and Tanja Stadler, " +
        "The fossilized birth–death process for coherent calibration of divergence-time estimates, " +
        "Proceedings of the National Academy of Sciences, 111 (29), 2014.",
        title = "The fossilized birth–death process for coherent calibration of divergence-time estimates",
        DOI="https://doi.org/10.1073/pnas.1319091111",
        authors = {"Heath","Huelsenbeck","Stadler"}, year=2014)
public class FossilBirthDeathTreeDT implements GenerativeDistribution<TimeTree> {

    private Value<Number> diversificationRate;
    private Value<Number> turnover;
    private Value<Number> rhoVal;
    private Value<Number> samplingProportion;
    private Value<Integer> n;
    private Value<Taxa> taxa;

    FossilBirthDeathTree wrapped;

    public FossilBirthDeathTreeDT(
            @ParameterInfo(name = diversificationParamName, narrativeName = "diversification rate",
                    description = "diversification rate (birth rate - death rate).") Value<Number> diversification,
            @ParameterInfo(name = turnoverParamName,
                    description = "turnover (death rate / birth rate).") Value<Number> turnover,
            @ParameterInfo(name = rhoParamName, narrativeName = "sampling proportion",
                    description = "proportion of extant taxa sampled.") Value<Number> rhoVal,
            @ParameterInfo(name = samplingProportionParamName,
                    description = "the probability of sampling prior to death: psi / (psi + mu).") Value<Number> samplingProportion,
            @ParameterInfo(name = DistributionConstants.nParamName,
                    description = "the number of taxa. optional.", optional = true) Value<Integer> n,
            @ParameterInfo(name = taxaParamName,
                    description = "Taxa object", optional = true) Value<Taxa> taxa) {

        this.diversificationRate = diversification;
        this.turnover = turnover;
        this.rhoVal = rhoVal;
        this.samplingProportion = samplingProportion;
        this.n = n;
        this.taxa = taxa;
        setup();
    }

    @GeneratorInfo(name = "FossilBirthDeathTreeDT",
            category = GeneratorCategory.BD_TREE,
            description = "A tree of extant species and those sampled through time, which is conceptually embedded in a full species tree produced by a speciation-extinction (birth-death) branching process.<br>" +
                    "Parameterised by diversification rate, turnover and sampling proportion. Conditioned on root age and (optionally) on number of extant taxa.")
    public RandomVariable<TimeTree> sample() {
        setup();
        RandomVariable<TimeTree> tree = wrapped.sample();
        return new RandomVariable<>(null, tree.value(), this);
    }

    private void setup() {
        double turno = ValueUtils.doubleValue(turnover);
        double divers = ValueUtils.doubleValue(diversificationRate);
        double s = ValueUtils.doubleValue(samplingProportion);

        double denom = Math.abs(1.0 - turno);
        double birth_rate = divers / denom;
        double death_rate = (turno * divers) / denom;
        // samplingProportion = psi / (psi + mu), so psi = s * mu / (1 - s)
        double psi = s * death_rate / (1.0 - s);

        wrapped = new FossilBirthDeathTree(
                new Value<>("lambda", birth_rate),
                new Value<>("mu", death_rate),
                rhoVal,
                new Value<>("psi", psi),
                n, taxa);
    }

    @Override
    public double logDensity(TimeTree timeTree) {
        throw new UnsupportedOperationException("Not implemented!");
    }

    @Override
    public Map<String, Value> getParams() {
        TreeMap<String, Value> map = new TreeMap<>();
        map.put(diversificationParamName, diversificationRate);
        map.put(turnoverParamName, turnover);
        map.put(rhoParamName, rhoVal);
        map.put(samplingProportionParamName, samplingProportion);
        if (n != null) map.put(DistributionConstants.nParamName, n);
        if (taxa != null) map.put(taxaParamName, taxa);
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        switch (paramName) {
            case diversificationParamName:
                diversificationRate = value;
                break;
            case turnoverParamName:
                turnover = value;
                break;
            case rhoParamName:
                rhoVal = value;
                break;
            case samplingProportionParamName:
                samplingProportion = value;
                break;
            case DistributionConstants.nParamName:
                n = value;
                break;
            case taxaParamName:
                taxa = value;
                break;
            default:
                throw new RuntimeException("Unrecognised parameter name: " + paramName);
        }
    }

    public Value<Number> getDiversificationRate() {
        return diversificationRate;
    }

    public Value<Number> getTurnover() {
        return turnover;
    }

    public Value<Number> getRho() {
        return rhoVal;
    }

    public Value<Number> getSamplingProportion() {
        return samplingProportion;
    }
}
