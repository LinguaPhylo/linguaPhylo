package lphy.core.vectorization.operation;

import lphy.core.logger.LoggerUtils;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.GraphicalModelNode;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.datatype.IntegerArrayValue;
import org.phylospec.types.Int;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RangeList extends DeterministicFunction<Int[]> {

    List<GraphicalModelNode> rangeElements = new ArrayList<>();

    public RangeList(GraphicalModelNode... rangeElements) {
        int arg = 0;
        for (GraphicalModelNode node : rangeElements) {
            Object value = node.value();
            if (value instanceof Int || value instanceof Int[]) {
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
    @GeneratorInfo(name = "rangeList", description = "The range of integers from start to end using the format 'start:end'. " +
            "Boundaries are included.")
    public IntegerArrayValue apply() {

        List<Int> indices = new ArrayList<>();
        for (GraphicalModelNode node : rangeElements) {
            Object value = node.value();
            if (value instanceof Int intVal) {
                indices.add(intVal);
            } else if (value instanceof Int[] intArr) {
                indices.addAll(Arrays.asList(intArr));
            }
        }
        return new IntegerArrayValue(null, indices.toArray(new Int[0]), this);
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
