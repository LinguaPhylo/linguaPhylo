package james.core;

import james.graphicalModel.GenerativeDistribution;
import james.graphicalModel.RandomVariable;
import james.graphicalModel.Value;
import org.apache.commons.math3.distribution.LogNormalDistribution;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by adru001 on 18/12/19.
 */
public class LogNormal implements GenerativeDistribution<Double> {

    private Value<Double> M;
    private Value<Double> S;

    private Random random;

    LogNormalDistribution logNormalDistribution;

    public LogNormal(Value<Double> M, Value<Double> S, Random random) {
        this.M = M;
        this.S = S;
        this.random = random;

        logNormalDistribution = new LogNormalDistribution(M.value(), S.value());
    }

    public RandomVariable<Double> sample() {

        double x = logNormalDistribution.sample();
        return new RandomVariable<>("x", x, this);
    }

    @Override
    public double density(Double x) {

        return logNormalDistribution.density(x);
    }

    @Override
    public List<Value> getParams() {
        return Arrays.asList(M,S);
    }
}
