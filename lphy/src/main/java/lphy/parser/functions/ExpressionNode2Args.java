package lphy.parser.functions;

import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.GraphicalModelNode;
import lphy.graphicalModel.Value;
import lphy.util.LoggerUtils;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * container holding a DeterministicFunction with 2 arguments.
 **/
public class ExpressionNode2Args<T> extends ExpressionNode {
    BiFunction func;
    ElementWise2Args elementWise;

    public ExpressionNode2Args(String expression, BiFunction func, GraphicalModelNode... values) {
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
    public static BiFunction<Number, Number, Number> plus() {
        return (a, b) -> a.doubleValue() + b.doubleValue();
    }

    public static BiFunction<Number, Number, Number> minus() {
        return (a, b) -> a.doubleValue() - b.doubleValue();
    }

    public static BiFunction<Number, Number, Number> times() {
        return (a, b) -> a.doubleValue() * b.doubleValue();
    }

    public static BiFunction<Number, Number, Number> divide() {
        return (a, b) -> a.doubleValue() / b.doubleValue();
    }

    public static BiFunction<Number, Number, Number> pow() {
        return (a, b) -> Math.pow(a.doubleValue(), b.doubleValue());
    }

    public static BiFunction<Number, Number, Number> mod() {
        return (a, b) -> a.doubleValue() % b.doubleValue();
    }

    //*** logical ***//

    public static BiFunction<Boolean, Boolean, Boolean> and() {
        return (a, b) -> a && b;
    }

    public static BiFunction<Boolean, Boolean, Boolean> or() {
        return (a, b) -> a || b;
    }

    //*** the first two needs to consider all Comparable ***//

    public static BiFunction<Object, Object, Boolean> equals() {
        return (a, b) -> {
            if (a instanceof Number aN && b instanceof Number bN)
                return aN.doubleValue() == bN.doubleValue();
            else if (a instanceof Object[] aA && b instanceof Object[] bA)
                return Arrays.deepEquals(aA, bA);
            else {
                return a.equals(b);
            }
        };
    }

    public static BiFunction<Object, Object, Boolean> ne() {
        return (a, b) -> {
            if (a instanceof Number aN && b instanceof Number bN)
                return aN.doubleValue() != bN.doubleValue() ;
            else if (a instanceof Object[] aA && b instanceof Object[] bA)
                    return !Arrays.deepEquals(aA, bA);
            else
                return !a.equals(b);
        };
    }

    public static BiFunction<Number, Number, Boolean> le() {
        return (a, b) -> a.doubleValue() <= b.doubleValue() ;
    }

    public static BiFunction<Number, Number, Boolean> less() {
        return (a, b) -> a.doubleValue() < b.doubleValue() ;
    }

    public static BiFunction<Number, Number, Boolean> ge() {
        return (a, b) -> a.doubleValue() >= b.doubleValue() ;
    }

    public static BiFunction<Number, Number, Boolean> greater() {
        return (a, b) -> a.doubleValue() > b.doubleValue() ;
    }

//TODO check
    public static BiFunction<Integer, Integer, Integer> bitwiseand() {
        return (a, b) -> (int) a.doubleValue() & (int) b.doubleValue();
    }

    public static BiFunction<Integer, Integer, Integer> bitwiseor() {
        return (a, b) -> (int) a.doubleValue() | (int) b.doubleValue();
    }

    public static void main(String[] args) {

        Object a = new Integer[]{1, 2, 3};
        Object b = new Integer[]{1, 2, 3};

        if ((a instanceof Number) && (b instanceof Number)) {
            System.out.println("1. a == b = " + (((Number)a).doubleValue() == ((Number)b).doubleValue()));

        } else if ((a instanceof Object[]) && (b instanceof Object[])) {
            System.out.println("2. a == b = " + Arrays.equals((Object[])a, (Object[])b));
        } else
            System.out.println(" a == b = " + a.equals(b));

    }

}
