package james.core.distributions;

import james.graphicalModel.*;

import java.util.*;

/**
 * Created by adru001 on 18/12/19.
 */
public class Dirichlet implements GenerativeDistribution<List<Double>> {

    private final String concParamName;
    private Value<List<Double>> concentration;

    public Dirichlet(@ParameterInfo(name="concentration", description="the concentration parameters of a Dirichlet distribution.") Value<List<Double>> concentration) {
        this.concentration = concentration;
        concParamName = getParamName(0);
    }

    @GenerativeDistributionInfo(description="The dirichlet probability distribution.")
    public RandomVariable<List<Double>> sample() {

        List<Double> dirichlet = new ArrayList<>();
        double sum = 0.0;
        for (int i = 0; i < concentration.value().size(); i++) {
            double val = Utils.randomGamma(concentration.value().get(i), 1.0);
            dirichlet.add(val);
            sum += val;
        }
        for (int i = 0; i < concentration.value().size(); i++) {
            dirichlet.set(i, dirichlet.get(i)/sum);
        }

        return new RandomVariable<>("x", dirichlet, this);
    }

    public double density(Double d) {
        // TODO
        return 0;
    }

    @Override
    public Map<String,Value> getParams() {
        return Collections.singletonMap(getParamName(0), concentration);
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(concParamName)) {
            concentration = value;
        } else {
            throw new RuntimeException("Only valid parameter name is " + concParamName);
        }
    }

    public String toString() {
        return getName();
    }
}
