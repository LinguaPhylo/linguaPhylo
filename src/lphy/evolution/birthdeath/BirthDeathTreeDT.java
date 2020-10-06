package lphy.evolution.birthdeath;

import lphy.evolution.tree.TimeTree;
import lphy.graphicalModel.*;

import java.util.*;

import static lphy.evolution.birthdeath.BirthDeathConstants.*;
import static lphy.graphicalModel.ValueUtils.doubleValue;

/**
 * A Birth-death tree generative distribution
 */
public class BirthDeathTreeDT implements GenerativeDistribution<TimeTree> {

    private Value<Number> diversificationRate;
    private Value<Number> turnover;
    private Value<Number> rootAge;

    FullBirthDeathTree wrapped;

    public BirthDeathTreeDT(@ParameterInfo(name = diversificationParamName, description = "diversification rate.") Value<Number> diversification,
                            @ParameterInfo(name = turnoverParamName, description = "turnover.") Value<Number> turnover,
                            @ParameterInfo(name = rootAgeParamName, description = "the number of taxa.") Value<Number> rootAge
    ) {

        this.turnover = turnover;
        this.diversificationRate = diversification;
        this.rootAge = rootAge;

        setup();
    }


    @GeneratorInfo(name = "BirthDeath", description = "The Birth-death-sampling tree distribution over tip-labelled time trees.<br>" +
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

        wrapped =
                new FullBirthDeathTree(
                        new Value<>(lambdaParamName, birth_rate),
                        new Value<>(muParamName, death_rate),
                        rootAge);
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
