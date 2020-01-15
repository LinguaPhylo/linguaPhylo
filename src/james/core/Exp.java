package james.core;

import james.graphicalModel.GenerativeDistribution;
import james.graphicalModel.RandomVariable;
import james.graphicalModel.Value;

import javax.swing.*;
import java.util.*;

/**
 * Created by adru001 on 18/12/19.
 */
public class Exp implements GenerativeDistribution<Double> {

    static String rateParamName = "rate";
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
    public Map<String,Value> getParams() {
        return Collections.singletonMap(rateParamName, rate);
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(rateParamName)) {
            rate = value;
        } else {
            throw new RuntimeException("Only valid parameter name is " + rateParamName);
        }
    }

    public void setRate(double rate) {
        this.rate.setValue(rate);
    }

    public JComponent getViewer() {
        return new JLabel("<html>An exponential distribution, governed by a <small><font color=\"#808080\">" + rateParamName + "</font></small> parameter.</html>");
    }
}
