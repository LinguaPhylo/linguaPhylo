package lphy.evolution.birthdeath;

import lphy.evolution.tree.TimeTree;
import lphy.graphicalModel.*;

import java.util.Map;
import java.util.TreeMap;

import static lphy.evolution.birthdeath.BirthDeathConstants.*;
import static lphy.graphicalModel.ValueUtils.doubleValue;

/**
 * A Birth-death-sampling tree generative distribution
 */
public class BirthDeathSamplingTreeDT implements GenerativeDistribution<TimeTree> {

    private Value<Number> diversificationRate;
    private Value<Number> turnover;
    private Value<Number> rho;
    private Value<Number> rootAge;

    BirthDeathSamplingTree wrapped;

    public BirthDeathSamplingTreeDT(@ParameterInfo(name = diversificationParamName, description = "diversification rate.") Value<Number> diversification,
                                    @ParameterInfo(name = turnoverParamName, description = "turnover.") Value<Number> turnover,
                                    @ParameterInfo(name = rhoParamName, description = "the sampling proportion.") Value<Number> rho,
                                    @ParameterInfo(name = rootAgeParamName, description = "the age of the root node.") Value<Number> rootAge) {

        this.turnover = turnover;
        this.diversificationRate = diversification;
        this.rho = rho;
        this.rootAge = rootAge;
        setup();
    }

    @GeneratorInfo(name = "BirthDeathSampling", description = "The Birth-death-sampling tree distribution over tip-labelled time trees.<br>" +
            "Conditioned on root age.")
    public RandomVariable<TimeTree> sample() {

        setup();
        RandomVariable<TimeTree> tree = wrapped.sample();
        return new RandomVariable<>("\u03C8", tree.value(), this);
    }

    private void setup() {
        double turno = doubleValue(turnover);
        double divers = doubleValue(diversificationRate);

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
