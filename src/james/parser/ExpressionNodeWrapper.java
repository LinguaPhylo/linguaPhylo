package james.parser;

import james.graphicalModel.DeterministicFunction;
import james.graphicalModel.GraphicalModelNode;
import james.graphicalModel.Parameterized;
import james.graphicalModel.Value;
import james.utils.LoggerUtils;

import java.util.List;
import java.util.Map;

public class ExpressionNodeWrapper extends DeterministicFunction {

    ExpressionNode nodeToWrap;

    public ExpressionNodeWrapper(ExpressionNode nodeToWrap) {
        this.nodeToWrap = nodeToWrap;

        extractAllParams(nodeToWrap);

        LoggerUtils.log.info("expression node wrapper keys:" + paramMap.keySet());

        rewireAllOutputs(nodeToWrap, false);
    }

    /**
     * Returns size of this expression (i.e. how many expression nodes are involved, including expressionNodes that produce the inputs to this one).
     * @param eNode
     * @return
     */
    public static int expressionSubtreeSize(ExpressionNode eNode) {

        int size = 1;
        for (GraphicalModelNode childNode : (List<GraphicalModelNode>) eNode.getInputs()) {
            if (childNode instanceof Value) {
                Value v = (Value) childNode;
                if (v.getFunction() instanceof ExpressionNode) {
                    size += expressionSubtreeSize((ExpressionNode) v.getFunction());
                }
            }
        }
       return size;
    }

    private void rewireAllOutputs(ExpressionNode expressionNode, boolean makeAnonymous) {
        for (GraphicalModelNode childNode : (List<GraphicalModelNode>) expressionNode.getInputs()) {
            if (childNode instanceof Value) {
                Value v = (Value) childNode;
                if (v.getFunction() instanceof ExpressionNode) {
                    rewireAllOutputs((ExpressionNode) v.getFunction(), true);
                }
            }
        }
        expressionNode.getParams().forEach((key, value) -> {
            ((Value) value).removeOutput((Parameterized) ((Value) value).getOutputs().get(0));
            ((Value) value).addOutput(this);
        });
        if (makeAnonymous) expressionNode.setAnonymous(true);
    }

    private void extractAllParams(ExpressionNode expressionNode) {
        for (GraphicalModelNode childNode : (List<GraphicalModelNode>) expressionNode.getInputs()) {
            if (childNode instanceof Value) {
                Value v = (Value) childNode;
                if (v.getFunction() instanceof ExpressionNode) {
                    extractAllParams((ExpressionNode) v.getFunction());
                }
            }
        }
        expressionNode.getParams().forEach((key, value) -> {
            if (!(((Value) value).isAnonymous())) {
                paramMap.put((String) key, (Value) value);
            }
        });
    }

    @Override
    public void setParam(String paramName, Value<?> value) {
        paramMap.put(paramName, value);
        setParamRecursively(paramName, value, nodeToWrap);
    }

    public Map<String, Value> getParams() {
        return paramMap;

    }

    @Override
    public String getName() {
        return nodeToWrap.getName();
    }

    private void setParamRecursively(String paramName, Value<?> value, ExpressionNode expressionNode) {
        if (expressionNode.getParams().containsKey(paramName)) {
            expressionNode.setParam(paramName, value);
        } else {
            for (GraphicalModelNode childNode : expressionNode.inputValues) {
                if (childNode instanceof Value) {
                    Value v = (Value) childNode;
                    if (v.getFunction() instanceof ExpressionNode) {
                        setParamRecursively(paramName, value, (ExpressionNode) v.getFunction());
                    }
                } else throw new RuntimeException("This code assumes all inputs are values!");
            }
        }
    }

    @Override
    public Value apply() {

        // this is a bit inefficient!
        // It ensures that the Sampler works, but if random variables haven't changed it is unnecessary.
        return applyRecursively();
//        Value v = nodeToWrap.apply();
//        v.setFunction(this);
//        return v;
    }

    public Value applyRecursively() {
        Value v =  applyRecursively(nodeToWrap);
        v.setFunction(this);
        return v;
    }

    private Value applyRecursively(ExpressionNode expressionNode) {

        Value[] newInputValues = new Value[expressionNode.inputValues.length];
        for (int i = 0; i < expressionNode.inputValues.length; i++) {
            if (expressionNode.inputValues[i] instanceof Value) {
                Value v = (Value) expressionNode.inputValues[i];
                if (v.getFunction() instanceof ExpressionNode) {
                    ExpressionNode childExpressionNode = (ExpressionNode) v.getFunction();

                    Value newValue = applyRecursively(childExpressionNode);
                    if (!v.isAnonymous()) {
                        newValue.setId(v.getId());
                        paramMap.put(v.getId(), newValue);
                    }
                    expressionNode.inputValues[i] = newValue;
                }
            } else throw new RuntimeException("This code assumes all inputs are values!");
        }
        return expressionNode.apply();
    }

    public String codeString() {
        return nodeToWrap.codeString();
    }
}
