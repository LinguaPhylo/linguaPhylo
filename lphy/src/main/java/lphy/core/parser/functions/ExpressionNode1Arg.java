package lphy.core.parser.functions;


import lphy.core.graphicalmodel.components.DeterministicFunction;
import lphy.core.graphicalmodel.components.GraphicalModelNode;
import lphy.core.graphicalmodel.components.Value;
import org.apache.commons.math3.distribution.NormalDistribution;

import java.util.LinkedHashMap;
import java.util.function.Function;

/**
 * anonymous container holding a DeterministicFunction with 1 argument.
 * The functions containing more than 1 argument are implemented by
 * extending {@link DeterministicFunction}.
 **/
public class ExpressionNode1Arg<T> extends ExpressionNode {
    Function func;
    ElementWise1Arg elementWise;

    public ExpressionNode1Arg(String expression, Function func, GraphicalModelNode... values) {
        this.expression = expression;
        this.func = func;
        params = new LinkedHashMap<>();
        for (GraphicalModelNode node : values) {
            if (node instanceof ExpressionNode) {
                for (Object o : ((ExpressionNode) node).getInputs()) {
                    Value value2 = (Value) o;
                    value2.addOutput(this);
                    params.put(value2.getId(), value2);
                }
            } else if (node instanceof Value val) {
                addValue2Params(val);
            }
        }
        inputValues = values;

        elementWise = ElementWise1Arg.elementFactory(values);
    }

    @Override
    public Value<T> apply() {
        Value value = elementWise.apply(inputValues[0], func);
        value.setFunction(this);
        return value;
    }

    // unary operators
    public static Function<Number, Number> not() {
        return (a) -> a.doubleValue() == 0 ? 1.0 : 0.0;
    }

    public static Function<Number, Double> abs() {
        return (a) -> Math.abs(a.doubleValue());
    }

    public static Function<Number, Double> acos() {
        return (a) -> Math.acos(a.doubleValue());
    }

    public static Function<Number, Double> acosh() {
        return (a) -> (Math.log(a.doubleValue() + Math.sqrt(a.doubleValue() + 1) * Math.sqrt(a.doubleValue() - 1)));
    }

    public static Function<Number, Double> asin() {
        return (a) -> Math.asin(a.doubleValue());
    }

    public static Function<Number, Double> asinh() {
        return (a) -> (Math.log(a.doubleValue() + Math.sqrt(a.doubleValue() * a.doubleValue() + 1)));
    }

    public static Function<Number, Double> atan() {
        return (a) -> Math.atan(a.doubleValue());
    }

    public static Function<Number, Double> atanh() {
        return (a) -> (0.5 * Math.log((1 + a.doubleValue()) / (1 - a.doubleValue())));
    }

    public static Function<Number, Double> cLogLog() {
        return (a) -> Math.log(-Math.log(1 - a.doubleValue()));
    }

    public static Function<Number, Double> cbrt() {
        return (a) -> Math.cbrt(a.doubleValue());
    }

    public static Function<Number, Double> ceil() {
        return (a) -> Math.ceil(a.doubleValue());
    }

    public static Function<Number, Double> cos() {
        return (a) -> Math.cos(a.doubleValue());
    }

    public static Function<Number, Double> cosh() {
        return (a) -> Math.cosh(a.doubleValue());
    }

    public static Function<Number, Double> exp() {
        return (a) -> Math.exp(a.doubleValue());
    }

    public static Function<Number, Double> expm1() {
        return (a) -> Math.expm1(a.doubleValue());
    }

    public static Function<Number, Double> floor() {
        return (a) -> Math.floor(a.doubleValue());
    }

    public static Function<Number, Double> log() {
        return (a) -> Math.log(a.doubleValue());
    }

    public static Function<Number, Double> log10() {
        return (a) -> Math.log10(a.doubleValue());
    }

    public static Function<Number, Double> log1p() {
        return (a) -> Math.log1p(a.doubleValue());
    }

    public static Function<Number, Double> logFact() {
        return (a) -> {
            double logFactorial = 0;
            for (int j = 2; j <= a.doubleValue(); j++) {
                logFactorial += Math.log(j);
            }
            return logFactorial;
        };
    }

    public static Function<Number, Double> logGamma() {
        return (a) -> org.apache.commons.math3.special.Gamma.logGamma(a.doubleValue());
    }

	public static Function<Number, Double> logit() {
        return (a) -> Math.log(a.doubleValue()) - Math.log(1 - a.doubleValue());
    }

	public static Function<Number, Double> phi() {
        return (a) -> (new NormalDistribution()).cumulativeProbability(a.doubleValue());
    }

	public static Function<Number, Double> probit() {
        return (a) -> Math.sqrt(2) * org.apache.commons.math3.special.Erf.erfInv(2 * a.doubleValue() - 1);
    }

	public static Function<Number, Integer> round() {
        return (a) -> (int) Math.round(a.doubleValue());
    }

	public static Function<Number, Double> signum() {
        return (a) -> Math.signum(a.doubleValue());
    }

	public static Function<Number, Double> sin() {
        return (a) -> Math.sin(a.doubleValue());
    }

	public static Function<Number, Double> sinh() {
        return (a) -> Math.sinh(a.doubleValue());
    }

	public static Function<Number, Double> sqrt() {
        return (a) -> Math.sqrt(a.doubleValue());
    }

	public static Function<Number, Double> step() {
        return (a) -> a.doubleValue() > 0.0 ? 1.0 : 0.0;
    }

	public static Function<Number, Double> tan() {
        return (a) -> Math.tan(a.doubleValue());
    }

	public static Function<Number, Double> tanh() {
        return (a) -> Math.tanh(a.doubleValue());
    }
}
