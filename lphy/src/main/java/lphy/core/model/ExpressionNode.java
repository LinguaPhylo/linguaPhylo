package lphy.core.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * For some operators equivalent to {@link DeterministicFunction}, such as ln(), e, or, + - * /, ...
 */
public abstract class ExpressionNode<T> extends DeterministicFunction<T> {

	protected String expression;
//	Map<String, Value> params;
    protected GraphicalModelNode[] inputValues;
	boolean isAnonymous = false; //TODO where to use ? Also why not use ID instead, or in DeterministicFunction ?

	public ExpressionNode() { }

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
			paramMap.put(key, value);
		}
		value.addOutput(this);
	}

	@Override
	public String getName() {
		return expression;
	}

	@Override
	public Map<String, Value> getParams() {
		return new LinkedHashMap<>(paramMap);
	}

	@Override
	public void setParam(String paramName, Value value) {
		//((Value) params.get(paramName)).setValue(value);
		paramMap.put(paramName, value);
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public GraphicalModelNode[] getInputValues() {
		return inputValues;
	}

	@Override
	public String codeString() {
		return expression;
	}

	@Override
	public List<GraphicalModelNode> getInputs() {
		return new ArrayList<>(paramMap.values());
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
