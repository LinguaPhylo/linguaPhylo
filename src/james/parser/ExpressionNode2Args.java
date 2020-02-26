package james.parser;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.*;

import james.graphicalModel.DeterministicFunction;
import james.graphicalModel.GraphicalModelNode;
import james.graphicalModel.Parameterized;
import james.graphicalModel.Value;

/** anonymous container holding a DeterministicFunction with 2 arguments **/
public class ExpressionNode2Args<T> extends ExpressionNode {
	BinaryOperator func;
	ElementWise2Args elementWise;

	public ExpressionNode2Args(String expression, BinaryOperator func, GraphicalModelNode... values) {
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
		
		elementWise = ElementWise2Args.elementFactory(values);
	}

	@Override
	public Value<T> apply() {
		return elementWise.apply(inputValues[0], inputValues[1], func);
	}



	// binary operators
	static BinaryOperator<Number> plus() {
		return (a, b) -> a.doubleValue() + b.doubleValue();
	}

	static BinaryOperator<Number> minus() {
		return (a, b) -> a.doubleValue() - b.doubleValue();
	}

	static BinaryOperator<Number> times() {
		return (a, b) -> a.doubleValue() * b.doubleValue();
	}

	static BinaryOperator<Number> divide() {
		return (a, b) -> a.doubleValue() / b.doubleValue();
	}

	static BinaryOperator<Number> and() {
		return (a, b) -> a.doubleValue() != 0.0 && b.doubleValue() != 0.0 ? 1.0 : 0.0;
	}

	static BinaryOperator<Number> or() {
		return (a, b) -> a.doubleValue() != 0.0 || b.doubleValue() != 0.0 ? 1.0 : 0.0;
	}

	static BinaryOperator<Integer> bitwiseand() {
		return (a, b) -> (int) a.doubleValue() & (int) b.doubleValue();
	}

	static BinaryOperator<Integer> bitwiseor() {
		return (a, b) -> (int) a.doubleValue() | (int) b.doubleValue();
	}

	static BinaryOperator<Number> le() {
		return (a, b) -> a.doubleValue() <= b.doubleValue() ? 1.0 : 0.0;
	}

	static BinaryOperator<Number> less() {
		return (a, b) -> a.doubleValue() < b.doubleValue() ? 1.0 : 0.0;
	}

	static BinaryOperator<Number> ge() {
		return (a, b) -> a.doubleValue() >= b.doubleValue() ? 1.0 : 0.0;
	}

	static BinaryOperator<Number> greater() {
		return (a, b) -> a.doubleValue() > b.doubleValue() ? 1.0 : 0.0;
	}

	static BinaryOperator<Number> equals() {
		return (a, b) -> a.doubleValue() == b.doubleValue() ? 1.0 : 0.0;
	}

	static BinaryOperator<Number> ne() {
		return (a, b) -> a.doubleValue() == b.doubleValue() ? 1.0 : 0.0;
	}

	static BinaryOperator<Number> pow() {
		return (a, b) -> Math.pow(a.doubleValue(), b.doubleValue());
	}

	static BinaryOperator<Number> mod() {
		return (a, b) -> a.doubleValue() % b.doubleValue();
	}

	

}
