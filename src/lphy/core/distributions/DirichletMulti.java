package lphy.core.distributions;

import lphy.core.functions.IntegerArray;
import lphy.graphicalModel.*;

import java.util.*;

import static lphy.core.distributions.DistributionConstants.*;
import static lphy.core.distributions.DistributionConstants.sdParamName;

/**
 * Created by adru001 on 18/12/19.
 */
@Deprecated()
public class DirichletMulti implements GenerativeDistribution<Double[][]> {

    private Value<Number[]> concentration;
    private Value<Integer> n;

    public DirichletMulti(@ParameterInfo(name=concParamName, narrativeName = "concentration parameter", description="the concentration parameters of a Dirichlet distribution.") Value<Number[]> concentration,
                          @ParameterInfo(name=nParamName, narrativeName = "number of i.i.d. samples", description = "the number of iid samples from this Dirichlet.") Value<Integer> n) {
        this.concentration = concentration;
        this.n = n;
    }

    @GeneratorInfo(name="Dirichlet", verbClause = "has", narrativeName = "Dirichlet prior", description="The dirichlet probability distribution.")
    public RandomVariable<Double[][]> sample() {

        List<RandomVariable> dirichletVariables = new ArrayList<>();

        for (int rep = 0; rep < n.value(); rep++) {
            Double[] dirichlet = new Double[concentration.value().length];

            double sum = 0.0;

            for (int i = 0; i < dirichlet.length; i++) {
                double val = Utils.randomGamma(concentration.value()[i].doubleValue(), 1.0);
                dirichlet[i] = val;
                sum += val;
            }
            for (int i = 0; i < dirichlet.length; i++) {
                dirichlet[i] /= sum;
            }
            dirichletVariables.add(new RandomVariable(null, dirichlet, this));
        }

        return new VectorizedRandomVariable<>(null, dirichletVariables, this);
    }

    public double density(Double[][] d) {
        // TODO
        return 0;
    }

    @Override
    public Map<String,Value> getParams() {
        return new TreeMap<>() {{
            put(concParamName, concentration);
            put(nParamName, n);
        }};

    }

    public Value<Number[]> getConcentration() {
        return concentration;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(concParamName)) {
            concentration = value;
        } else if (paramName.equals(nParamName)) {
            n = value;
        } else {
            throw new RuntimeException("Only valid parameter name is " + concParamName);
        }
    }

    public String toString() {
        return getName();
    }
}