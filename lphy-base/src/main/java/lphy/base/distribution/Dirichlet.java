package lphy.base.distribution;

import lphy.base.math.MathUtils;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Collections;
import java.util.Map;

import static lphy.base.distribution.DistributionConstants.concParamName;

/**
 * Dirichlet distribution prior.
 * @author Alexei Drummond
 * @author Walter Xie
 */
public class Dirichlet extends ParametricDistribution<Double[]> {

    private Value<Number[]> concentration;

    public Dirichlet(@ParameterInfo(name=concParamName, narrativeName = "concentration", description="the concentration parameters of a Dirichlet distribution.") Value<Number[]> concentration) {
        super();
        this.concentration = concentration;
    }

    @Override
    protected void constructDistribution(RandomGenerator random) {  }

    @GeneratorInfo(name="Dirichlet", verbClause = "have", narrativeName = "Dirichlet distribution prior",
            category = GeneratorCategory.PRIOR,
            examples = {"birthDeathRhoSampling.lphy","dirichlet.lphy","https://linguaphylo.github.io/tutorials/time-stamped-data/"},
            description="The dirichlet probability distribution.")
    public RandomVariable<Double[]> sample() {

        Double[] dirichlet = new Double[concentration.value().length];
        double sum = 0.0;
        for (int i = 0; i < dirichlet.length; i++) {
            double val = MathUtils.randomGamma(concentration.value()[i].doubleValue(), 1.0, random);
            dirichlet[i] = val;
            sum += val;
        }
        for (int i = 0; i < dirichlet.length; i++) {
            dirichlet[i] /= sum;
        }

        return new RandomVariable<>("x", dirichlet, this);
    }

    public double density(Double[] d) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Map<String,Value> getParams() {
        return Collections.singletonMap(concParamName, concentration);
    }

    public void setConcentration(Value<Number[]> concentration) {
        this.concentration = concentration;
    }

    public Value<Number[]> getConcentration() {
        return concentration;
    }
}