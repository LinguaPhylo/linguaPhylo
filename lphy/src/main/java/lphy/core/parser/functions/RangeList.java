package lphy.core.parser.functions;

import lphy.core.model.components.DeterministicFunction;
import lphy.core.model.components.GraphicalModelNode;
import lphy.core.model.components.Value;
import lphy.core.model.types.IntegerArrayValue;
import lphy.core.util.LoggerUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RangeList extends DeterministicFunction<Integer[]> {

    List<GraphicalModelNode> rangeElements = new ArrayList<>();

    public RangeList(GraphicalModelNode... rangeElements) {
        int arg = 0;
        for (GraphicalModelNode node : rangeElements) {
            Object value = node.value();
            if (value instanceof Integer || value instanceof Integer[]) {
                this.rangeElements.add(node);
                if (node instanceof Value) {
                    setInput(arg + "", (Value)node );
                } else if (node instanceof DeterministicFunction) {
                    setInput(arg+"", ((DeterministicFunction)node).apply());
                }
                arg += 1;
            } else {
                LoggerUtils.log.severe("Non integer in RangeList: " + value);
            }
        }
    }

    @Override
    public IntegerArrayValue apply() {

        List<Integer> indices = new ArrayList<>();
        for (GraphicalModelNode node : rangeElements) {
            Object value = node.value();
            if (value instanceof Integer) {
                indices.add((Integer)value);
            } else {
                indices.addAll(Arrays.asList((Integer[])value));
            }
        }
        return new IntegerArrayValue(null, indices.toArray(new Integer[0]), this);
    }

    public boolean isRange() {
        return (rangeElements.size() == 1 && rangeElements.get(0) instanceof Range);
    }

    public boolean isSingle() {
        return (rangeElements.size() == 1 && rangeElements.get(0).value() instanceof Integer);
    }

    public GraphicalModelNode getRangeElement(int i) {
        return rangeElements.get(i);
    }

    public int size() {
        return rangeElements.size();
    }

    public String codeString() {
        StringBuilder builder = new StringBuilder();
        int count = 0;
        for (GraphicalModelNode node : rangeElements) {
            if (count > 0) builder.append(",");
            if (node instanceof Value) {
                if (((Value<?>) node).isAnonymous()) {
                    builder.append(((Value)node).codeString());
                } else {
                    builder.append(((Value)node).getId());
                }
            } else if (node instanceof DeterministicFunction) {
                builder.append(((DeterministicFunction)node).codeString());
            }
            count += 1;
        }
        return builder.toString();
    }
}
