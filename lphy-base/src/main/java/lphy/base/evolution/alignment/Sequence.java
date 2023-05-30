package lphy.base.evolution.alignment;

import jebl.evolution.sequences.SequenceType;
import jebl.evolution.sequences.State;
import lphy.base.distributions.DistributionConstants;
import lphy.base.distributions.ParametricDistribution;
import lphy.base.evolution.Taxa;
import lphy.core.graphicalmodel.components.ParameterInfo;
import lphy.core.graphicalmodel.components.RandomVariable;
import lphy.core.graphicalmodel.components.Value;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.TreeMap;

import static lphy.base.evolution.likelihood.AbstractPhyloCTMC.LParamName;
import static lphy.base.evolution.likelihood.AbstractPhyloCTMC.dataTypeParamName;

public class Sequence extends ParametricDistribution<SimpleAlignment> {

    Value<SequenceType> sequenceType;

    Value<Double[]> probs;

    Value<Integer> nchar;

    public Sequence(@ParameterInfo(name = DistributionConstants.pParamName, narrativeName = "probability distribution", description = "the probability distribution over integer states 1 to K.") Value<Double[]> probs,
                    @ParameterInfo(name = LParamName, narrativeName="length", description = "length of the alignment") Value<Integer> nchar) {
        super();
        this.probs = probs; // check probs is same dimension as nucleotide datatype
        this.nchar = nchar;
        this.sequenceType =  new Value<>(null, SequenceType.NUCLEOTIDE); // default data type is nucleotide
    }

    public Sequence(@ParameterInfo(name = DistributionConstants.pParamName, description = "the probability distribution over integer states 1 to K.") Value<Double[]> probs,
                    @ParameterInfo(name = LParamName, narrativeName="length", description = "length of the alignment") Value<Integer> nchar,
                    @ParameterInfo(name = dataTypeParamName, narrativeName="data type", description = "the sequence type of the alignment") Value<SequenceType> sequenceType) {
        super();
        this.probs = probs; // check probs is same dimension as datatype
        this.nchar = nchar;
        this.sequenceType = sequenceType;
    }

    @Override
    protected void constructDistribution(RandomGenerator random) {

    }

    public RandomVariable<SimpleAlignment> sample() {
        Taxa t = Taxa.createTaxa(1);
        SimpleAlignment alignment = new SimpleAlignment(t, nchar.value(), sequenceType.value());

        for (int i = 0; i < nchar.value(); i++) {
            int value = sample(probs.value(), random);
            alignment.setState(0, i, value);
        }
        return new RandomVariable<>("S", alignment, this);
    }

    private State translate(int i) {
        return sequenceType.value().getState(i);
    }

    private int sample(Double[] p, RandomGenerator random) {
        double U = random.nextDouble();
        double sum = p[0];
        int i = 0;
        while (U > sum) {
            sum += p[i+1];
            i += 1;
        }
        return i;
    }

    @Override
    public Map<String,Value> getParams() {
        return new TreeMap<>() {{
            put(DistributionConstants.pParamName, probs);
            put(LParamName, nchar);
            put(dataTypeParamName, sequenceType);
        }};
    }

}
