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
 * A fossilized birth-death tree conditioned on origin age,
 * parameterised by diversification rate, turnover and sampling proportion.
 */
public class SimFBDAgeDT implements GenerativeDistribution<TimeTree> {

    private Value<Number> diversificationRate;
    private Value<Number> turnover;
    private Value<Number> samplingProportion;
    private Value<Double> fracVal;
    private Value<Number> originAge;

    SimFBDAge wrapped;

    public SimFBDAgeDT(
            @ParameterInfo(name = diversificationParamName, narrativeName = "diversification rate",
                    description = "diversification rate (birth rate - death rate).") Value<Number> diversification,
            @ParameterInfo(name = turnoverParamName,
                    description = "turnover (death rate / birth rate).") Value<Number> turnover,
            @ParameterInfo(name = fracParamName,
                    description = "fraction of extant taxa sampled.") Value<Double> fracVal,
            @ParameterInfo(name = samplingProportionParamName,
                    description = "the probability of sampling prior to death: psi / (psi + mu).") Value<Number> samplingProportion,
            @ParameterInfo(name = originAgeParamName,
                    description = "the age of the origin.") Value<Number> originAge) {

        this.diversificationRate = diversification;
        this.turnover = turnover;
        this.fracVal = fracVal;
        this.samplingProportion = samplingProportion;
        this.originAge = originAge;
        setup();
    }

    @GeneratorInfo(name = "SimFBDAge",
            category = GeneratorCategory.BD_TREE,
            description = "A tree of extant species and those sampled through time, which is conceptually embedded in a full species tree produced by a speciation-extinction (birth-death) branching process.<br>" +
                    "Parameterised by diversification rate, turnover and sampling proportion. Conditioned on origin age.")
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

        wrapped = new SimFBDAge(
                new Value<>("lambda", birth_rate),
                new Value<>("mu", death_rate),
                fracVal,
                new Value<>("psi", psi),
                originAge);
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
            put(fracParamName, fracVal);
            put(samplingProportionParamName, samplingProportion);
            put(originAgeParamName, originAge);
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
            case fracParamName:
                fracVal = value;
                break;
            case samplingProportionParamName:
                samplingProportion = value;
                break;
            case originAgeParamName:
                originAge = value;
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

    public Value<Double> getFrac() {
        return fracVal;
    }

    public Value<Number> getSamplingProportion() {
        return samplingProportion;
    }

    public Value<Number> getOriginAge() {
        return originAge;
    }
}
