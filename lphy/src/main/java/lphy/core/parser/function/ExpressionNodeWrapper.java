package lphy.core.parser.function;

import lphy.core.model.*;

import java.util.List;
import java.util.Map;

public class ExpressionNodeWrapper extends DeterministicFunction {

    ExpressionNode nodeToWrap;

    public ExpressionNodeWrapper(ExpressionNode nodeToWrap) {
        this.nodeToWrap = nodeToWrap;

        extractAllParams(nodeToWrap);

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
                if (v.getGenerator() instanceof ExpressionNode) {
                    size += expressionSubtreeSize((ExpressionNode) v.getGenerator());
                }
            }
        }
       return size;
    }

    private void rewireAllOutputs(ExpressionNode expressionNode, boolean makeAnonymous) {
        for (GraphicalModelNode childNode : (List<GraphicalModelNode>) expressionNode.getInputs()) {
            if (childNode instanceof Value) {
                Value v = (Value) childNode;
                if (v.getGenerator() instanceof ExpressionNode) {
                    rewireAllOutputs((ExpressionNode) v.getGenerator(), true);
                }
            }
        }
        expressionNode.getParams().forEach((key, value) -> {
            ((Value) value).removeOutput((Generator) ((Value) value).getOutputs().get(0));
            ((Value) value).addOutput(this);
        });
        if (makeAnonymous) expressionNode.setAnonymous(true);
    }

    private void extractAllParams(ExpressionNode expressionNode) {
        for (GraphicalModelNode childNode : (List<GraphicalModelNode>) expressionNode.getInputs()) {
            if (childNode instanceof Value) {
                Value v = (Value) childNode;
                if (v.getGenerator() instanceof ExpressionNode) {
                    extractAllParams((ExpressionNode) v.getGenerator());
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
    public void setParam(String paramName, Value value) {
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
            for (GraphicalModelNode childNode : expressionNode.getInputValues()) {
                if (childNode instanceof Value) {
                    Value v = (Value) childNode;
                    if (v.getGenerator() instanceof ExpressionNode) {
                        setParamRecursively(paramName, value, (ExpressionNode) v.getGenerator());
                    }
                } else throw new RuntimeException("This code assumes all inputs are values!");
            }
        }
    }

    @Override
    public Value apply() {
        return applyRecursively();
    }

    public Value applyRecursively() {
        Value v =  applyRecursively(nodeToWrap);
        v.setFunction(this);
        return v;
    }

    private Value applyRecursively(ExpressionNode expressionNode) {

        for (int i = 0; i < expressionNode.getInputValues().length; i++) {
            if (expressionNode.getInputValues()[i] instanceof Value) {
                Value v = (Value) expressionNode.getInputValues()[i];
                if (v.getGenerator() instanceof ExpressionNode) {
                    ExpressionNode childExpressionNode = (ExpressionNode) v.getGenerator();

                    Value newValue = applyRecursively(childExpressionNode);
                    if (!v.isAnonymous()) {
                        newValue.setId(v.getId());
                        paramMap.put(v.getId(), newValue);
                    }
                    expressionNode.getInputValues()[i] = newValue;
                }
            } else throw new RuntimeException("This code assumes all inputs are values!");
        }
        return expressionNode.apply();
    }

    public String codeString() {
        return nodeToWrap.codeString();
    }
}
