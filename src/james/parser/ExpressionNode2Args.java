package james.parser;

import java.util.*;
import java.util.function.*;

import james.graphicalModel.GraphicalModelNode;
import james.graphicalModel.Value;

/** anonymous container holding a DeterministicFunction with 2 arguments **/
public class ExpressionNode2Args<T> extends ExpressionNode {
	BinaryOperator func;
	ElementWise2Args elementWise;

	List<Set<String>> valuesToIds = new ArrayList();

	public ExpressionNode2Args(String expression, BinaryOperator func, GraphicalModelNode... values) {
		this.expression = expression;
		this.func = func;
		params = new LinkedHashMap<>();

		for (GraphicalModelNode value : values) {
			Set<String> ids = new HashSet<>();
			if (value instanceof ExpressionNode) {
//				for (Object o : ((ExpressionNode) value).getInputs()) {
//					Value value2 = (Value) o;
//					params.put(value2.getId(), value2);
//					ids.add(value2.getId());
//				}
			} else if (value instanceof Value) {
				String id = ((Value) value).getId();
				params.put(id, (Value) value);
				ids.add(id);
				((Value) value).addOutput(this);
			}
			valuesToIds.add(ids);
		}
		inputValues = values;
		
		elementWise = ElementWise2Args.elementFactory(values);
	}

	public Map<String, Value> getParams() {
		return params;
	}

	public void setParam(String paramName, Value value) {
		params.put(paramName, value);

		for (int i = 0; i < valuesToIds.size(); i++) {
			if (valuesToIds.get(i).contains(paramName)) {
				if (inputValues[i] instanceof Value) {
					inputValues[i] = value;
				}
			}
		}
	}


	@Override
	public Value<T> apply() {
		Value value = elementWise.apply(inputValues[0], inputValues[1], func);
		value.setFunction(this);
		return value;
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
