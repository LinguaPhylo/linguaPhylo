package james.core;

import james.graphicalModel.GenerativeDistribution;
import james.graphicalModel.RandomVariable;
import james.graphicalModel.Value;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by adru001 on 18/12/19.
 */
public class Exp implements GenerativeDistribution<Double> {

    private Value<Double> rate;

    private Random random;

    public Exp(Value<Double> rate, Random random) {
        this.rate = rate;
        this.random = random;
    }

    public RandomVariable<Double> sample() {

        double x = -rate.value() * Math.log(1.0 - random.nextDouble());
        return new RandomVariable<>("x", x, this);
    }

    @Override
    public double density(Double aDouble) {
        return 0;
    }

    @Override
    public List<Value> getParams() {
        return Arrays.asList(rate);
    }

    public void setRate(double rate) {
        this.rate.setValue(rate);
    }
}
