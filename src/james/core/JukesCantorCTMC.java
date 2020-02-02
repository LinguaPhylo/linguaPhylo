package james.core;

import james.graphicalModel.GenerativeDistribution;
import james.graphicalModel.ParameterInfo;
import james.graphicalModel.RandomVariable;
import james.graphicalModel.Value;

import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

public class JukesCantorCTMC implements GenerativeDistribution<Integer> {

    Value<Integer> X;
    Value<Double> d;
    Value<Integer> dim;
    Random random;

    String XParamName;
    String dParamName;
    String dimParamName;

    public JukesCantorCTMC(@ParameterInfo(name = "X", description = "the starting state.") Value<Integer> X,
                           @ParameterInfo(name = "d", description = "the genetic distance.") Value<Double> d,
                           @ParameterInfo(name = "dim", description = "the number of dimensions.") Value<Integer> dim,
                           Random random) {
        this.X = X;
        this.d = d;
        this.dim = dim;
        this.random = random;

        XParamName = getParamName(0);
        dParamName = getParamName(1);
        dimParamName = getParamName(2);
    }

    @Override
    public RandomVariable<Integer> sample() {

        int dim = this.dim.value();

        // the probability that the state is the same after time t.
        double probSame = (1.0/dim) + ((dim-1.0)/dim) * Math.exp(-(dim/(dim-1.0))*d.value());

        if (random.nextDouble() < probSame) return new RandomVariable<>("Y", X.value(), this);

        // if not the same state then equally likely to be any of the others
        int newState = random.nextInt(dim-1);
        if (newState >= X.value()) newState += 1;
        return new RandomVariable<>("Y", newState, this);

    }

    @Override
    public SortedMap<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        map.put(dParamName, d);
        map.put(dimParamName, dim);
        map.put(XParamName, X);
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(dParamName)) d = value;
        else if (paramName.equals(dimParamName)) dim = value;
        else if (paramName.equals(XParamName)) X = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);
    }
}
