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
import james.graphicalModel.types.DoubleArray2DValue;
import james.graphicalModel.types.DoubleArrayValue;
import james.graphicalModel.types.DoubleValue;
import james.graphicalModel.types.IntegerValue;

/** anonymous container holding a DeterministicFunction **/
public class ExpressionNode<T> extends DeterministicFunction<T> implements Parameterized {
	String expression;
	Map<String, Value> params;
	Value [] inputValues;
	BinaryOperator func;
	
	public ExpressionNode(String expression, BinaryOperator func, Value ...values) {
		this.expression = expression;
		this.func = func;
		params = new LinkedHashMap<>();
		for (Value value : values) {
			params.put(value.getId(), value);
		}
		inputValues = values;
	}

	@Override
	public String getName() {
		// Anonymous expression
		return null;
	}

	@Override
	public Value<T> apply() {
		return new Value(null, func.apply(inputValues[0].value(), inputValues[1].value()));
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
	// OPTION 0: make plus() type specific and let parser pick the right one
	static BinaryOperator<Double> plusDD() {
        return (a, b) -> a + b;
    }
	// TODO: implement many more operators 

	
	// OPTION 1: make plus() a BinaryOperator<double> and make ExpressionNode deal with types
	// TODO: requires re-engineering of apply()

	// OPTION 2: make plus() a BinaryOperator<Value> and deal with types afterwards
	static BinaryOperator<Value> plus() {
      return (a, b) -> {
    	  if (a instanceof DoubleArray2DValue && b instanceof DoubleArray2DValue) {
    		  Double [][] va = (Double [][]) a.value();
    		  Double [][] vb = (Double [][]) b.value();
    		  Double [][] r = new Double [va.length][va[0].length];
    		  for (int i = 0; i < va.length; i++) {
    			  for (int j = 0; j < va.length; j++) {
    				  
    				  r[i][j] = va[i][j] + vb[i][j];
    			  }
    		  }
    		  return new DoubleArray2DValue("", r);
    	  }
    	  if (a instanceof DoubleArrayValue && b instanceof DoubleArrayValue) {
    		  Double [] va = (Double []) a.value();
    		  Double [] vb = (Double []) b.value();
    		  Double [] r = new Double [va.length];
    		  for (int i = 0; i < va.length; i++) {
    			  r[i] = va[i] + vb[i];
    		  }
    		  return new DoubleArrayValue("", r);
    	  }
    	  if (a instanceof DoubleValue && b instanceof DoubleValue) {
    		  Double va = (Double) a.value();
    		  Double vb = (Double) b.value();
    		  Double r = va + vb;
    		  return new DoubleValue("", r);
    	  }
    	  if (a instanceof IntegerValue && b instanceof IntegerValue) {
    		  Integer va = (Integer) a.value();
    		  Integer vb = (Integer) b.value();
    		  Integer r = va + vb;
    		  return new IntegerValue("", r);
    	  }
    	  // TODO: etc. etc. 32 more combinations to go
    	  return null;
      };
   }	
	static BinaryOperator<Double[]> plusDADA() {
        return (a, b) -> {
        	Double [] result = new Double[a.length];
        	for (int i = 0; i < a.length; i++) {
        		result[i] = a[i] + b[i % b.length];
        	}
        	return result;
        };
    }
//	static BiFunction<Double[], Double, Double[]> plusDAD() {
//        return (a, b) -> {
//        	Double [] result = new Double[a.length];
//        	for (int i = 0; i < a.length; i++) {
//        		result[i] = a[i] + b;
//        	}
//        	return result;
//        };
//    }
//	static BiFunction<Double, Integer, Double> plusDI() {
//        return (a, b) -> a + b;
//    }
//	static BinaryOperator<Integer> plusII() {
//        return (a, b) -> a + b;
//    }
	static BinaryOperator<Integer[]> plusIAIA() {
        return (a, b) -> {
        	Integer [] result = new Integer[a.length];
        	for (int i = 0; i < a.length; i++) {
        		result[i] = a[i] + b[i % b.length];
        	}
        	return result;
        };
    }
	static BiFunction<Double[], Integer[], Double[]> plusDAIA() {
        return (a, b) -> {
        	Double [] result = new Double[a.length];
        	for (int i = 0; i < a.length; i++) {
        		result[i] = a[i] + b[i % b.length];
        	}
        	return result;
        };
    }
//	static BiFunction<Integer[], Integer, Integer[]> plusIAI() {
//        return (a, b) -> {
//        	Integer [] result = new Integer[a.length];
//        	for (int i = 0; i < a.length; i++) {
//        		result[i] = a[i] + b;
//        	}
//        	return result;
//        };
//    }
//	static BiFunction<Integer[], Double, Double[]> plusIAD() {
//        return (a, b) -> {
//        	Double [] result = new Double[a.length];
//        	for (int i = 0; i < a.length; i++) {
//        		result[i] = a[i] + b;
//        	}
//        	return result;
//        };
//    }
//	static BiFunction<Double, DeterministicFunction<Double>, Double> plusDF() {
//        return (a, b) -> a + b.apply().value();
//    }
//	static BiFunction<Integer, DeterministicFunction<Double>, Double> plusIF() {
//        return (a, b) -> a + (double) b.apply().value();
//    }
//	static BiFunction<DeterministicFunction<Double>, DeterministicFunction<Double>, Double> plusFF() {
//        return (a, b) -> a.apply().value() + b.apply().value();
//    }

	
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
