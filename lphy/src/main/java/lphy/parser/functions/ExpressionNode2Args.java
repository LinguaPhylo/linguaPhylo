package lphy.parser.functions;

import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.GraphicalModelNode;
import lphy.graphicalModel.Value;
import lphy.util.LoggerUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BinaryOperator;

/**
 * container holding a DeterministicFunction with 2 arguments.
 * TODO: this only handles BinaryOperator but not BiFunction.
 **/
public class ExpressionNode2Args<T> extends ExpressionNode {
    BinaryOperator func;
    ElementWise2Args elementWise;

    public ExpressionNode2Args(String expression, BinaryOperator func, GraphicalModelNode... values) {
        this.expression = expression;
        this.func = func;
        params = new LinkedHashMap<>();

        for (GraphicalModelNode node : values) {
            if (node instanceof ExpressionNode) {
                throw new RuntimeException();
//				for (Object o : ((ExpressionNode) node).getInputs()) {
//					Value value2 = (Value) o;
//					params.put(value2.getId(), value2);
//					ids.add(value2.getId());
//				}
            } else if (node instanceof Value val) {
                addValue2Params(val);
            }
        }
        inputValues = values;

        elementWise = ElementWise2Args.elementFactory(values);
    }

    @GeneratorInfo(name="expression", verbClause = "is calculated by", description = "expression")
    public Map<String, Value> getParams() {
        return params;
    }

    public void setParam(String paramName, Value value) {
        params.put(paramName, value);

        for (int i = 0; i < inputValues.length; i++) {
        	Value v = (Value)inputValues[i];
            if (!v.isAnonymous() && v.getId().equals(paramName)) {
                inputValues[i] = value;
                LoggerUtils.log.fine("Setting input value " + i + " to " + value);
            } else if (v.isAnonymous() && v.codeString().equals(paramName)) {
                inputValues[i] = value;
                LoggerUtils.log.fine("Setting input value " + i + " to " + value + " based on code string.");
            }
        }
    }


    public Value<T> apply() {

        Value value = elementWise.apply(inputValues[0], inputValues[1], func);
        value.setFunction(this);
        return value;
    }

    // binary operators
    public static BinaryOperator<Number> plus() {
        return (a, b) -> a.doubleValue() + b.doubleValue();
    }

    public static BinaryOperator<Number> minus() {
        return (a, b) -> a.doubleValue() - b.doubleValue();
    }

    public static BinaryOperator<Number> times() {
        return (a, b) -> a.doubleValue() * b.doubleValue();
    }

    public static BinaryOperator<Number> divide() {
        return (a, b) -> a.doubleValue() / b.doubleValue();
    }

    public static BinaryOperator<Number> and() {
        return (a, b) -> a.doubleValue() != 0.0 && b.doubleValue() != 0.0 ? 1.0 : 0.0;
    }

    public static BinaryOperator<Number> or() {
        return (a, b) -> a.doubleValue() != 0.0 || b.doubleValue() != 0.0 ? 1.0 : 0.0;
    }

    public static BinaryOperator<Integer> bitwiseand() {
        return (a, b) -> (int) a.doubleValue() & (int) b.doubleValue();
    }

    public static BinaryOperator<Integer> bitwiseor() {
        return (a, b) -> (int) a.doubleValue() | (int) b.doubleValue();
    }

    public static BinaryOperator<Number> le() {
        return (a, b) -> a.doubleValue() <= b.doubleValue() ? 1.0 : 0.0;
    }

    public static BinaryOperator<Number> less() {
        return (a, b) -> a.doubleValue() < b.doubleValue() ? 1.0 : 0.0;
    }

    public static BinaryOperator<Number> ge() {
        return (a, b) -> a.doubleValue() >= b.doubleValue() ? 1.0 : 0.0;
    }

    public static BinaryOperator<Number> greater() {
        return (a, b) -> a.doubleValue() > b.doubleValue() ? 1.0 : 0.0;
    }

    public static BinaryOperator<Number> equals() {
        return (a, b) -> a.doubleValue() == b.doubleValue() ? 1.0 : 0.0;
    }

    public static BinaryOperator<Number> ne() {
        return (a, b) -> a.doubleValue() == b.doubleValue() ? 1.0 : 0.0;
    }

    public static BinaryOperator<Number> pow() {
        return (a, b) -> Math.pow(a.doubleValue(), b.doubleValue());
    }

    public static BinaryOperator<Number> mod() {
        return (a, b) -> a.doubleValue() % b.doubleValue();
    }
}
