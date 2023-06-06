package lphy.base.evolution.birthdeath;

import lphy.base.evolution.tree.TimeTree;
import lphy.core.model.*;
import lphy.core.model.annotation.Citation;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.parser.argument.ParameterInfo;

import java.util.Map;
import java.util.TreeMap;

import static lphy.base.evolution.birthdeath.BirthDeathConstants.*;

/**
 * The Birth-Death-Shift process.
 */
@Citation(value="Tanja Stadler, Mammalian phylogeny reveals recent diversification rate shifts, " +
        "Proceedings of the National Academy of Sciences, 108 (15), 2011.",
        title = "Mammalian phylogeny reveals recent diversification rate shifts",
        DOI="https://doi.org/10.1073/pnas.1016876108",
        authors = {"Stadler"}, year=2011)
public class BirthDeathSamplingTreeDT implements GenerativeDistribution<TimeTree> {

    private Value<Number> diversificationRate;
    private Value<Number> turnover;
    private Value<Number> rho;
    private Value<Number> rootAge;

    BirthDeathSamplingTree wrapped;

    public BirthDeathSamplingTreeDT(@ParameterInfo(name = diversificationParamName, narrativeName = "diversification rate", description = "diversification rate.") Value<Number> diversification,
                                    @ParameterInfo(name = turnoverParamName, description = "turnover.") Value<Number> turnover,
                                    @ParameterInfo(name = rhoParamName, narrativeName="sampling proportion", description = "the sampling proportion.") Value<Number> rho,
                                    @ParameterInfo(name = rootAgeParamName, narrativeName="root age", description = "the age of the root node.") Value<Number> rootAge) {

        this.turnover = turnover;
        this.diversificationRate = diversification;
        this.rho = rho;
        this.rootAge = rootAge;
        setup();
    }

    @GeneratorInfo(name = "BirthDeathSampling", verbClause = "is assumed to have evolved according to",
            narrativeName = "birth-death-sampling tree process",
            category = GeneratorCategory.BD_TREE, examples = {"birthDeathRhoSampling.lphy"},
            description = "The Birth-death-sampling tree distribution over tip-labelled time trees.<br>" +
            "Conditioned on root age.")
    public RandomVariable<TimeTree> sample() {

        setup();
        RandomVariable<TimeTree> tree = wrapped.sample();
        return new RandomVariable<>("\u03C8", tree.value(), this);
    }

    private void setup() {
        double turno = ValueUtils.doubleValue(turnover);
        double divers = ValueUtils.doubleValue(diversificationRate);

        double denom = Math.abs(1.0 - turno);
        double birth_rate = divers / denom;
        double death_rate = (turno * divers) / denom;

        wrapped = new BirthDeathSamplingTree(
                new Value<>("birthRate", birth_rate),
                new Value<>("deathRate", death_rate),
                rho, rootAge);
    }

    @Override
    public double logDensity(TimeTree timeTree) {

        throw new UnsupportedOperationException("Not implemented!");
    }

    @Override
    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(diversificationParamName, diversificationRate);
            put(turnoverParamName, turnover);
            put(rhoParamName, rho);
            put(rootAgeParamName, rootAge);
        }};
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
                rho = value;
                break;
            case rootAgeParamName:
                rootAge = value;
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
        return rho;
    }

    public Value<Number> getRootAge() {
        return rootAge;
    }
}
