package james.parser;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import james.graphicalModel.DeterministicFunction;
import james.graphicalModel.GraphicalModelNode;
import james.graphicalModel.Parameterized;
import james.graphicalModel.Value;
import james.graphicalModel.types.DoubleValue;

/** anonymous container holding a DeterministicFunction **/
public class ExpressionNode extends Value implements Parameterized {
	String expression;
	Map<String, Value> params;
	BinaryOperator func;
	
	public ExpressionNode(String expression, BinaryOperator func, Value ...values) {
		super(null, new DoubleValue(null, (Double) func.apply(values[0].value(), values[1].value())));
		this.expression = expression;
		this.func = func;
		params = new LinkedHashMap<>();
		for (Value value : values) {
			params.put(value.getId(), value);
		}
	}

	@Override
	public String getName() {
		// Anonymous expression
		return null;
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
        return (a, b) -> (int)a & (int)b ;
    }
	static BinaryOperator<Integer> bitwiseor() {
        return (a, b) -> (int)a | (int)b;
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
	

}
