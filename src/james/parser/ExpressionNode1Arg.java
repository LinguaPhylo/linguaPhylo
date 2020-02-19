package james.parser;


import java.util.LinkedHashMap;
import java.util.function.*;

import org.apache.commons.math3.distribution.NormalDistribution;

import james.graphicalModel.GraphicalModelNode;
import james.graphicalModel.Value;

/** anonymous container holding a DeterministicFunction with 1 argument **/
public class ExpressionNode1Arg<T> extends ExpressionNode {
	Function func;
	ElementWise1Arg elementWise;

	public ExpressionNode1Arg(String expression, Function func, GraphicalModelNode... values) {
		this.expression = expression;
		this.func = func;
		params = new LinkedHashMap<>();
		for (GraphicalModelNode value : values) {
			if (value instanceof ExpressionNode) {
				for (Object o : ((ExpressionNode) value).getInputs()) {
					Value value2 = (Value) o;
					params.put(value2.getId(), value2);
				}
			} else if (value instanceof Value) {
				params.put(((Value) value).getId(), (Value) value);
			}
		}
		inputValues = values;
		
		elementWise = ElementWise1Arg.elementFactory(values);
	}

	@Override
	public Value<T> apply() {
		return elementWise.apply(inputValues[0].value(), func);
	}



	// unary operators
	static Function<Double, Double> not() {
		return (a) -> a == 0 ? 1.0 : 0.0;
	}
	static Function<Integer, Integer> notI() {
		return (a) -> a == 0 ? 1 : 0;
	}

	static Function<Double, Double> abs() {		return (a)-> Math.abs(a);	}

	static Function<Double, Double> acos() {		return (a)-> Math.acos(a);	}

	static Function<Double, Double> acosh() {		return (a)-> (Math.log(a + Math.sqrt(a + 1) * Math.sqrt(a - 1)));	}

	static Function<Double, Double> asin() {		return (a)-> Math.asin(a);	}

	static Function<Double, Double> asinh() {		return (a)-> (Math.log(a + Math.sqrt(a * a + 1)));	}

	static Function<Double, Double> atan() {		return (a)-> Math.atan(a);	}

	static Function<Double, Double> atanh() {		return (a)-> (0.5 * Math.log((1 + a) / (1 - a)));	}

	static Function<Double, Double> cLogLog() {		return (a)-> Math.log(-Math.log(1 - a));}

	static Function<Double, Double> cbrt() {		return (a)-> Math.cbrt(a);	}

	static Function<Double, Double> ceil() {		return (a)-> Math.ceil(a);	}

	static Function<Double, Double> cos() {		return (a)-> Math.cos(a);	}

	static Function<Double, Double> cosh() {		return (a)-> Math.cosh(a);	}

	static Function<Double, Double> exp() {		return (a)-> Math.exp(a);	}

	static Function<Double, Double> expm1() {		return (a)-> Math.expm1(a);	}

	static Function<Double, Double> floor() {		return (a)-> Math.floor(a);	}

	static Function<Double, Double> log() {		return (a)-> Math.log(a);	}

	static Function<Double, Double> log10() {		return (a)-> Math.log10(a);	}

	static Function<Double, Double> log1p() {		return (a)-> Math.log1p(a);	}

	static Function<Double, Double> logFact() {		return (a)-> { 
		double logFactorial = 0;
		for (int j = 2; j <= a; j++) {
		  logFactorial += Math.log(j);
		}
		return logFactorial;
	};
	}

	static Function<Double, Double> logGamma() {		return (a)-> org.apache.commons.math3.special.Gamma.logGamma(a);	}

	static Function<Double, Double> logit() {		return (a)-> Math.log(a) - Math.log(1 - a);	}

	static Function<Double, Double> phi() {		return (a)-> (new NormalDistribution()).cumulativeProbability(a);	}

	static Function<Double, Double> probit() {		return (a)-> Math.sqrt(2) * org.apache.commons.math3.special.Erf.erf(2*a - 1);	}

	static Function<Double, Double> round() {		return (a)-> (double) Math.round(a);	}

	static Function<Double, Double> signum() {		return (a)-> Math.signum(a);	}

	static Function<Double, Double> sin() {		return (a)-> Math.sin(a);	}

	static Function<Double, Double> sinh() {		return (a)-> Math.sinh(a);	}

	static Function<Double, Double> sqrt() {		return (a)-> Math.sqrt(a);	}

	static Function<Double, Double> step() {		return (a)-> a > 0.0 ? 1.0 : 0.0;	}

	static Function<Double, Double> tan() {		return (a)-> Math.tan(a);	}

	static Function<Double, Double> tanh() {		return (a)-> Math.tanh(a);	}


}
