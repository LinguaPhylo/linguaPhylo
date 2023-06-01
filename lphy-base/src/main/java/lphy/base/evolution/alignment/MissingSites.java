package lphy.base.evolution.alignment;

import lphy.core.model.components.*;
import lphy.core.util.RandomUtils;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by Alexei Drummond on 2/02/20.
 */
public class MissingSites implements GenerativeDistribution<Alignment> {

    Value<Number> prob;
    Value<Alignment> alignment;

    public final String probParamName = "prob";

    RandomGenerator random;

    public MissingSites(@ParameterInfo(name = probParamName, description = "the probability that the state in a site is changed to the unknown state.") Value<Number> prob,
                        @ParameterInfo(name = AlignmentUtils.ALIGNMENT_PARAM_NAME, description = "the alignment before data is missing.") Value<Alignment> alignment) {

        this.prob = prob;
        this.alignment = alignment;
        this.random = RandomUtils.getRandom();
    }

    @Override
    public SortedMap<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        map.put(probParamName, prob);
        map.put(AlignmentUtils.ALIGNMENT_PARAM_NAME, alignment);
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(probParamName)) {
            if (value.value() instanceof Double) {
                prob = value;
            } else throw new IllegalArgumentException("Expecting type double, but got " + value.value().getClass());
        }
        else if (paramName.equals(AlignmentUtils.ALIGNMENT_PARAM_NAME)) alignment = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);
    }

    @GeneratorInfo(name = "MissingSites", verbClause = "is created by",
            category = GeneratorCategory.TAXA_ALIGNMENT,
            description = "The missing data distribution for an alignment.")
    public RandomVariable<Alignment> sample() {

        Alignment original = alignment.value();
        SimpleAlignment newAlignment = new ErrorAlignment(original.nchar(), original);

        double p = ValueUtils.doubleValue(prob);

        for (int i = 0; i < newAlignment.ntaxa(); i++) {
            for (int j = 0; j < newAlignment.nchar(); j++) {
                newAlignment.setState(i, j, missing(original, original.getState(i, j), p));
            }
        }

        return new RandomVariable<>("D", newAlignment, this);
    }

    private int missing(Alignment a, int state, double p) {

        double U = random.nextDouble();
        if (U < p) return a.getSequenceType().getUnknownState().getIndex();
        return state;
    }
}
