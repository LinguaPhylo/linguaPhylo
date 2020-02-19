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

/** anonymous container holding a DeterministicFunction **/
public class ExpressionNode<T> extends DeterministicFunction<T> implements Parameterized {
	String expression;
	Map<String, Value> params;
	GraphicalModelNode[] inputValues;
	BinaryOperator func;
	ElementWise elementWise;

	public ExpressionNode(String expression, BinaryOperator func, GraphicalModelNode... values) {
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
		
		elementWise = ElementWise.elementFactory(values);
	}

	@Override
	public String getName() {
		// Anonymous expression
		return null;
	}

	@Override
	public Value<T> apply() {
		return elementWise.apply(inputValues[0].currentValue(), inputValues[1].currentValue(), func);
	}

	@Override
	public Map<String, Value> getParams() {
		return new LinkedHashMap<>(params);
	}

	@Override
	public void setParam(String paramName, Value value) {
		params.get(paramName).setValue(value);
	}

	@Override
	public String codeString() {
		return expression;
	}

	@Override
	public List<GraphicalModelNode> getInputs() {
		return new ArrayList<>(params.values());
	}

	// binary operators
	static BinaryOperator<Double> plus() {
		return (a, b) -> a + b;
	}

	static BinaryOperator<Double> minus() {
		return (a, b) -> a - b;
	}

	static BinaryOperator<Double> times() {
		return (a, b) -> a * b;
	}

	static BinaryOperator<Double> divide() {
		return (a, b) -> a / b;
	}

	static BinaryOperator<Double> and() {
		return (a, b) -> a != 0.0 && b != 0.0 ? 1.0 : 0.0;
	}

	static BinaryOperator<Double> or() {
		return (a, b) -> a != 0.0 || b != 0.0 ? 1.0 : 0.0;
	}

	static BinaryOperator<Integer> bitwiseand() {
		return (a, b) -> (int) a & (int) b;
	}

	static BinaryOperator<Integer> bitwiseor() {
		return (a, b) -> (int) a | (int) b;
	}

	static BinaryOperator<Double> le() {
		return (a, b) -> a <= b ? 1.0 : 0.0;
	}

	static BinaryOperator<Double> less() {
		return (a, b) -> a < b ? 1.0 : 0.0;
	}

	static BinaryOperator<Double> ge() {
		return (a, b) -> a >= b ? 1.0 : 0.0;
	}

	static BinaryOperator<Double> greater() {
		return (a, b) -> a > b ? 1.0 : 0.0;
	}

	static BinaryOperator<Double> equals() {
		return (a, b) -> a == b ? 1.0 : 0.0;
	}

	static BinaryOperator<Double> ne() {
		return (a, b) -> a == b ? 1.0 : 0.0;
	}

	static BinaryOperator<Double> pow() {
		return (a, b) -> Math.pow(a, b);
	}

	static BinaryOperator<Double> mod() {
		return (a, b) -> a % b;
	}

	
	
	static BinaryOperator<Integer> plusI() {
		return (a, b) -> a + b;
	}
	static BinaryOperator<Integer> minusI() {
		return (a, b) -> a - b;
	}

	static BinaryOperator<Integer> timesI() {
		return (a, b) -> a * b;
	}

	static BinaryOperator<Integer> divideI() {
		return (a, b) -> a / b;
	}

	static BinaryOperator<Integer> andI() {
		return (a, b) -> a != 0.0 && b != 0.0 ? 1 : 0;
	}

	static BinaryOperator<Integer> orI() {
		return (a, b) -> a != 0.0 || b != 0.0 ? 1 : 0;
	}

	static BinaryOperator<Integer> bitwiseandI() {
		return (a, b) -> (int) a & (int) b;
	}

	static BinaryOperator<Integer> bitwiseorI() {
		return (a, b) -> (int) a | (int) b;
	}

	static BinaryOperator<Integer> leI() {
		return (a, b) -> a <= b ? 1 : 0;
	}

	static BinaryOperator<Integer> lessI() {
		return (a, b) -> a < b ? 1 : 0;
	}

	static BinaryOperator<Integer> geI() {
		return (a, b) -> a >= b ? 1 : 0;
	}

	static BinaryOperator<Integer> greaterI() {
		return (a, b) -> a > b ? 1 : 0;
	}

	static BinaryOperator<Integer> equalsI() {
		return (a, b) -> a == b ? 1 : 0;
	}

	static BinaryOperator<Integer> neI() {
		return (a, b) -> a == b ? 1 : 0;
	}

	static BinaryOperator<Integer> powI() {
		return (a, b) -> (Integer) ((int) Math.pow(a, b));
	}

	static BinaryOperator<Integer> modI() {
		return (a, b) -> a % b;
	}
	

}
