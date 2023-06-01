package lphy.core.parser.functions;

import lphy.core.model.components.DeterministicFunction;
import lphy.core.model.components.Generator;
import lphy.core.model.components.GraphicalModelNode;
import lphy.core.model.components.Value;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

abstract public class ExpressionNode<T> extends DeterministicFunction<T> implements Generator {

	String expression;
	Map<String, Value> params;
	GraphicalModelNode[] inputValues;
	boolean isAnonymous = false;

	ExpressionNode() {
		
	}

	protected void addValue2Params(Value value) {
		String key = null;
		if (!value.isAnonymous()) {
			key = value.getId();
		} else if (value.isRandom() || value.getGenerator() != null) {
			// the or above is required to ensure that constant named parameter dependencies are displayed in graphical model
			key = value.codeString();
		}

		if (key != null) {
			// use value instead of func, look pretty inside red diamond button in GUI
			if ( value.getGenerator() != null && key.equals(value.getGenerator().codeString()) )
				key = value.toString();
			params.put(key, value);
		}
		value.addOutput(this);
	}

	@Override
	public String getName() {
		return expression;
	}

	@Override
	public Map<String, Value> getParams() {
		return new LinkedHashMap<>(params);
	}

	@Override
	public void setParam(String paramName, Value value) {
		//((Value) params.get(paramName)).setValue(value);
		params.put(paramName, value);
	}

	@Override
	public String codeString() {
		return expression;
	}

	@Override
	public List<GraphicalModelNode> getInputs() {
		return new ArrayList<>(params.values());
	}

	public boolean isAnonymous() {
		return isAnonymous;
	}

	/**
	 *
	 * @param b
	 */
	public void setAnonymous(boolean b) {
		isAnonymous = b;
	}

	public String toString() {
		return getName();
	}

	public Value updateInputsAndApply() {

		Value[] newInputValues = new Value[inputValues.length];
		for (int i = 0; i < inputValues.length; i++) {
			if (inputValues[i] instanceof Value) {
				Value v = (Value) inputValues[i];
				if (v.getGenerator() instanceof DeterministicFunction) {
					DeterministicFunction f = (DeterministicFunction) v.getGenerator();

					Value newValue = f.apply();
					if (!v.isAnonymous()) {
						newValue.setId(v.getId());
						paramMap.put(v.getId(), newValue);
					}
					inputValues[i] = newValue;
				}
			} else throw new RuntimeException("This code assumes all inputs are values!");
		}
		return apply();
	}
}
