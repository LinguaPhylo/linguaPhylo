package lphy.evolution.birthdeath;

import beast.core.BEASTInterface;
import lphy.evolution.tree.TimeTree;
import lphy.graphicalModel.*;

import java.util.*;

/**
 * A Birth-death tree generative distribution
 */
public class BirthDeathTreeDT implements GenerativeDistribution<TimeTree> {

    private final String diversificationParamName;
    private final String turnoverParamName;

    private Value<Double> diversificationRate;
    private Value<Double> turnover;
    private Value<Double> rootAge;

    BirthDeathTree wrapped;

    public BirthDeathTreeDT(@ParameterInfo(name = "diversification", description = "diversification rate.") Value<Double> diversification,
                            @ParameterInfo(name = "turnover", description = "turnover.") Value<Double> turnover,
                            @ParameterInfo(name = "rootAge", description = "the number of taxa.") Value<Double> rootAge
                          ) {

        this.turnover = turnover;
        this.diversificationRate = diversification;
        this.rootAge = rootAge;

        diversificationParamName = getParamName(0);
        turnoverParamName = getParamName(1);
        setup();
    }


    @GeneratorInfo(name="BirthDeath", description="The Birth-death-sampling tree distribution over tip-labelled time trees.<br>" +
            "Conditioned on root age.")
    public RandomVariable<TimeTree> sample() {

        setup();
        RandomVariable<TimeTree> tree = wrapped.sample();
        return new RandomVariable<>("\u03C8", tree.value(), this);
    }

    private void setup() {
        double denom = Math.abs(1.0 - turnover.value());
        double birth_rate = diversificationRate.value() / denom;
        double death_rate = (turnover.value() * diversificationRate.value()) / denom;

        wrapped =
                new BirthDeathTree(
                        new Value<>("birthRate", birth_rate),
                        new Value<>("deathRate", death_rate),
                        rootAge);
    }

    @Override
    public double logDensity(TimeTree timeTree) {

        throw new UnsupportedOperationException("Not implemented!");
    }

    @Override
    public SortedMap<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        map.put(diversificationParamName, diversificationRate);
        map.put(turnoverParamName, turnover);
        map.put(wrapped.rootAgeParamName, rootAge);
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(diversificationParamName)) diversificationRate = value;
        else if (paramName.equals(turnoverParamName)) turnover = value;
        else if (paramName.equals(wrapped.rootAgeParamName)) rootAge = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);
    }

    public String toString() {
        return getName();
    }

    public BEASTInterface toBEAST(BEASTInterface value, Map beastObjects) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + ".toBEAST not implemented yet!");
    }
}
