package lphy.base.evolution.birthdeath;

import lphy.base.evolution.tree.TimeTree;
import lphy.core.model.GenerativeDistribution;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.ValueUtils;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

import java.util.Map;
import java.util.TreeMap;

import static lphy.base.evolution.birthdeath.BirthDeathConstants.*;

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


    @GeneratorInfo(name = "BirthDeath",
            category = GeneratorCategory.BD_TREE,
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

        wrapped =
                new FullBirthDeathTree(
                        new Value<>(lambdaParamName, birth_rate),
                        new Value<>(muParamName, death_rate),
                        rootAge, null);
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
