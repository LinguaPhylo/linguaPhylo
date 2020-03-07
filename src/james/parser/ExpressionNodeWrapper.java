package james.parser;

import james.graphicalModel.DeterministicFunction;
import james.graphicalModel.GraphicalModelNode;
import james.graphicalModel.Parameterized;
import james.graphicalModel.Value;
import james.utils.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExpressionNodeWrapper extends DeterministicFunction {

    ExpressionNode nodeToWrap;

    public ExpressionNodeWrapper(ExpressionNode nodeToWrap) {
        this.nodeToWrap = nodeToWrap;

        extractAllParams(nodeToWrap);

        Message.info("expression node wrapper keys:" + paramMap.keySet(), this);

        rewireAllOutputs(nodeToWrap, false);
    }

    private void rewireAllOutputs(ExpressionNode expressionNode, boolean makeAnonymous) {
        for (GraphicalModelNode childNode : (List<GraphicalModelNode>)expressionNode.getInputs()) {
            if (childNode instanceof Value) {
                Value v = (Value)childNode;
                if (v.getFunction() instanceof ExpressionNode) {
                    rewireAllOutputs((ExpressionNode) v.getFunction(), true);
                }
            }
        }
        expressionNode.getParams().forEach((key, value) -> {
            ((Value)value).removeOutput((Parameterized)((Value) value).getOutputs().get(0));
            ((Value)value).addOutput(this);
        });
        if (makeAnonymous) expressionNode.setAnonymous(true);
    }

    private void extractAllParams(ExpressionNode expressionNode) {
        for (GraphicalModelNode childNode : (List<GraphicalModelNode>)expressionNode.getInputs()) {
            if (childNode instanceof Value) {
                Value v = (Value)childNode;
                if (v.getFunction() instanceof ExpressionNode) {
                    extractAllParams((ExpressionNode) v.getFunction());
                }
            }
        }
        expressionNode.getParams().forEach((key, value) -> {
            if (!(((Value)value).isAnonymous())) {
                paramMap.put((String)key, (Value)value);
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
            for (GraphicalModelNode childNode : (List<GraphicalModelNode>)expressionNode.getInputs()) {
                if (childNode instanceof Value) {
                    Value v = (Value)childNode;
                    if (v.getFunction() instanceof ExpressionNode) {
                        setParamRecursively(paramName, value, (ExpressionNode)v.getFunction());
                    }
                }
            }
        }
    }

    @Override
    public Value apply() {

        // can't just call apply here for a sample.
        // because after wrapping the traversal will not include the substructre of this wrapped expression node

        Value v = nodeToWrap.apply();
        v.setFunction(this);
        return v;
    }

    public String codeString() {
        return nodeToWrap.codeString();
    }
}
